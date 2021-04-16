/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import org.obiba.magma.DatasourceCopierProgressListener;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.MultithreadedDatasourceCopier;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.identifiers.IdentifiersMapping;
import org.obiba.opal.core.magma.IdentifiersMappingView;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.obiba.opal.core.magma.concurrent.LockingActionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadFactory;




/**
 *
 */
class CopyValueTablesLockingAction extends LockingActionTemplate {

  private static final Logger log = LoggerFactory.getLogger(CopyValueTablesLockingAction.class);

  private final CopyValueTablesOptions copyValueTablesOptions;

  private final IdentifiersTableService identifiersTableService;

  private final IdentifierService identifierService;

  private final IdentifierGenerator identifierGenerator;

  private final TransactionTemplate txTemplate;

  private final DatasourceCopierProgressListener progressListener;

  @SuppressWarnings("PMD.ExcessiveParameterList")
  CopyValueTablesLockingAction(IdentifiersTableService identifiersTableService, IdentifierService identifierService, IdentifierGenerator identifierGenerator,
                               TransactionTemplate txTemplate, CopyValueTablesOptions copyValueTablesOptions,
                               DatasourceCopierProgressListener progressListener) {
    this.identifiersTableService = identifiersTableService;
    this.identifierService = identifierService;
    this.identifierGenerator = identifierGenerator;
    this.txTemplate = txTemplate;
    this.copyValueTablesOptions = copyValueTablesOptions;
    this.progressListener = progressListener;
  }

  @Override
  protected Set<String> getLockNames() {
    return getTablesToLock();
  }

  private Set<String> getTablesToLock() {
    Set<String> tablesToLock = new TreeSet<>();

    for (ValueTable valueTable : copyValueTablesOptions.getSourceTables()) {
      tablesToLock.add(valueTable.getDatasource() + "." + valueTable.getName());
      if (identifiersTableService.hasIdentifiersTable(valueTable.getEntityType())) {
        String ref = identifiersTableService.getTableReference(valueTable.getEntityType());
        tablesToLock.add(ref);
      }
    }

    return tablesToLock;
  }

  @Override
  protected TransactionTemplate getTransactionTemplate() {
    return txTemplate;
  }

  @Override
  protected Action getAction() {
    return new CopyAction();
  }

  private class CopyAction implements Action {
    @Override
    public boolean isTransactional() {
      return copyValueTablesOptions.getDestination().isTransactional();
    }

    @Override
    public void execute() throws Exception {
      for (final ValueTable valueTable : copyValueTablesOptions.getSourceTables()) {
        if (Thread.interrupted()) {
          throw new InterruptedException("Thread interrupted");
        }

        ValueTable tableToCopy = valueTable;
        if (!Strings.isNullOrEmpty(copyValueTablesOptions.getIdMapping()) && identifiersTableService.hasIdentifiersTable(valueTable.getEntityType())) {
          tableToCopy = importUnitIdentifiers(valueTable);
        }

        MultithreadedDatasourceCopier.Builder.newCopier() //
            .withThreads(new ThreadFactory() {
              @NotNull
              @Override
              public Thread newThread(@NotNull Runnable r) {
                return new TransactionalThread(r);
              }
            }) //
            .withProgressListener(progressListener) //
            .withCopier(newCopier()) //
            .from(tableToCopy) //
            .to(copyValueTablesOptions.getDestination()).build() //
            .copy();
      }
    }

    private class TransactionalThread extends Thread {

      private final Runnable runnable;

      TransactionalThread(Runnable runnable) {
        this.runnable = runnable;
      }

      @Override
      public void run() {
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
          @Override
          protected void doInTransactionWithoutResult(TransactionStatus status) {
            runnable.run();
          }
        });
      }
    }

    private ValueTable importUnitIdentifiers(ValueTable table) throws IOException {
      log.info("Preparing identifiers for mapping [{}]", copyValueTablesOptions.getIdMapping());
      identifiersTableService.ensureIdentifiersMapping(new IdentifiersMapping(copyValueTablesOptions.getIdMapping(), table.getEntityType()));

      IdentifiersMappingView identifiersMappingView = new IdentifiersMappingView(copyValueTablesOptions.getIdMapping(),
          IdentifiersMappingView.Policy.UNIT_IDENTIFIERS_ARE_PRIVATE,
          table, identifiersTableService,
          copyValueTablesOptions.isAllowIdentifierGeneration() ? identifierGenerator : null,
          copyValueTablesOptions.isIgnoreUnknownIdentifier());

      if (copyValueTablesOptions.isAllowIdentifierGeneration()) {
        log.info("Saving generated identifiers");
        PrivateVariableEntityMap entityMap = identifiersMappingView.getPrivateVariableEntityMap();
        // force entities listing to both generate and persist missing IDs
        identifiersMappingView.getVariableEntities();
        Disposables.dispose(entityMap); // finalize the persistence of the IDs
      }
      return identifiersMappingView;
    }

    /**
     * This listener will insert all participant identifiers in the keys datasource prior to copying the valueSet to the
     * data datasource. It will also generate the public variable entity if it does not exist yet. As such, it must be
     * executed before the ValueSet is copied to the data datasource otherwise, it will not have an associated entity.
     */
    private DatasourceCopier.DatasourceCopyEventListener createKeysListener(final ValueTable privateView,
                                                                            final PrivateVariableEntityMap entityMap, final ValueTableWriter keysTableWriter) {
      return new DatasourceCopier.DatasourceCopyValueSetEventListener() {
        @Override
        public void onValueSetCopied(ValueTable source, ValueSet valueSet, @SuppressWarnings(
            "ParameterHidesMemberVariable") String... destination) {
        }

        @Override
        public void onValueSetCopy(ValueTable source, ValueSet valueSet) {
          identifierService
              .copyParticipantIdentifiers(valueSet.getVariableEntity(), privateView, entityMap, keysTableWriter);
        }
      };
    }

    private DatasourceCopier.Builder newCopier() {
      return DatasourceCopier.Builder.newCopier() //
          .withLoggingListener() //
          .withThroughtputListener();
    }
  }
}
