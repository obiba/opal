/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.impl;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.MultithreadedDatasourceCopier;
import org.obiba.magma.views.View;
import org.obiba.opal.core.magma.FunctionalUnitView;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.obiba.opal.core.magma.concurrent.LockingActionTemplate;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Sets;

/**
 *
 */
class CopyValueTablesLockingAction extends LockingActionTemplate {

  private final Set<ValueTable> sourceTables;

  private final Datasource destination;

  private final boolean allowIdentifierGeneration;

  private final boolean ignoreUnknownIdentifier;

  private final IdentifiersTableService identifiersTableService;

  private final IdentifierService identifierService;

  private final TransactionTemplate txTemplate;

  @SuppressWarnings("PMD.ExcessiveParameterList")
  CopyValueTablesLockingAction(IdentifiersTableService identifiersTableService, IdentifierService identifierService,
      TransactionTemplate txTemplate, Set<ValueTable> sourceTables, Datasource destination,
      boolean allowIdentifierGeneration, boolean ignoreUnknownIdentifier) {
    this.identifiersTableService = identifiersTableService;
    this.identifierService = identifierService;
    this.txTemplate = txTemplate;
    this.sourceTables = sourceTables;
    this.destination = destination;
    this.allowIdentifierGeneration = allowIdentifierGeneration;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;
  }

  @Override
  protected Set<String> getLockNames() {
    return getTablesToLock();
  }

  private Set<String> getTablesToLock() {
    Set<String> tablesToLock = new TreeSet<String>();

    boolean needToLockKeysTable = false;

    for(ValueTable valueTable : sourceTables) {
      tablesToLock.add(valueTable.getDatasource() + "." + valueTable.getName());
      if(valueTable.getEntityType().equals(identifiersTableService.getEntityType())) {
        needToLockKeysTable = true;
      }
    }

    if(needToLockKeysTable) {
      tablesToLock.add(identifiersTableService.getTableReference());
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
    public void execute() throws Exception {
      for(ValueTable valueTable : sourceTables) {
        if(Thread.interrupted()) {
          throw new InterruptedException("Thread interrupted");
        }

        if(valueTable.isForEntityType(identifiersTableService.getEntityType())) {

          if(valueTable instanceof FunctionalUnitView) {
            importUnitData((FunctionalUnitView) valueTable);
          } else {
            addMissingEntitiesToKeysTable(valueTable);
            MultithreadedDatasourceCopier.Builder.newCopier() //
                .withThreads(new ThreadFactory() {
                  @Nonnull
                  @Override
                  public Thread newThread(@Nonnull Runnable r) {
                    return new TransactionalThread(r);
                  }
                }) //
                .withCopier(newCopierForParticipants()) //
                .from(valueTable) //
                .to(destination).build() //
                .copy();
          }

        } else {
          DatasourceCopier.Builder.newCopier() //
              .dontCopyNullValues() //
              .withLoggingListener().build() //
              .copy(valueTable, destination);
        }
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

    private Set<VariableEntity> addMissingEntitiesToKeysTable(ValueTable valueTable) {
      Set<VariableEntity> nonExistentVariableEntities = Sets.newHashSet(valueTable.getVariableEntities());

      if(identifiersTableService.hasValueTable()) {
        // Remove all entities that exist in the keys table. Whatever is left are the ones that don't exist...
        Set<VariableEntity> entitiesInKeysTable = identifiersTableService.getValueTable().getVariableEntities();
        nonExistentVariableEntities.removeAll(entitiesInKeysTable);
      }

      if(nonExistentVariableEntities.size() > 0) {
        ValueTableWriter keysTableWriter = identifiersTableService.createValueTableWriter();
        try {
          for(VariableEntity ve : nonExistentVariableEntities) {
            keysTableWriter.writeValueSet(ve).close();
          }
        } catch(IOException e) {
          throw new RuntimeException(e);
        } finally {
          Closeables.closeQuietly(keysTableWriter);
        }
      }

      return nonExistentVariableEntities;
    }

    private void importUnitData(FunctionalUnitView functionalUnitView) throws IOException {
      String keyVariableName = functionalUnitView.getUnit().getKeyVariableName();

      View privateView = identifierService
          .createPrivateView(functionalUnitView.getName(), functionalUnitView.getWrappedValueTable(),
              functionalUnitView.getUnit(), null);
      identifierService.createKeyVariable(privateView, keyVariableName);

      FunctionalUnitView publicView = identifierService
          .createPublicView(functionalUnitView, allowIdentifierGeneration, ignoreUnknownIdentifier);
      PrivateVariableEntityMap entityMap = publicView.getPrivateVariableEntityMap();

      // prepare for copying participant data
      ValueTableWriter keysTableWriter = identifiersTableService.createValueTableWriter();
      try {
        DatasourceCopier.DatasourceCopyEventListener keysListener = createKeysListener(privateView, entityMap,
            keysTableWriter);
        // Copy participant's non-identifiable variables and data
        DatasourceCopier datasourceCopier = newCopierForParticipants().withListener(keysListener).build();
        datasourceCopier.copy(publicView, destination);
      } finally {
        Closeables.closeQuietly(keysTableWriter);
      }
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

    private DatasourceCopier.Builder newCopierForParticipants() {
      return DatasourceCopier.Builder.newCopier() //
          .withLoggingListener() //
          .withThroughtputListener();
    }
  }
}
