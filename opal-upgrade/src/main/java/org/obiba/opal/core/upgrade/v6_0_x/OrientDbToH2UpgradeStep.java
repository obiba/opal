/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.upgrade.v6_0_x;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.obiba.opal.core.domain.*;
import org.obiba.opal.core.domain.converter.DomainGson;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.core.domain.security.*;
import org.obiba.opal.core.repository.*;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.repository.DataShieldProfileRepository;
import org.obiba.opal.r.repository.RSessionActivityRepository;
import org.obiba.opal.r.service.RSessionActivity;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Moves the configuration out of OrientDB and into the configuration database.
 * <p>
 * The old store is read directly: a {@code plocal} database opens without a server, so none of OrientDB's server, its
 * configuration file or its security file are involved. The folder is left exactly as it is found - an administrator
 * removes it once the upgraded installation has been verified, and until then it is the way back.
 * <p>
 * Every row is migrated. Nothing is truncated, sampled or summarised, including the R session activity, which is the
 * one table that can hold hundreds of thousands of rows. An administrator who wants to prune old activity can do so
 * afterwards, deliberately.
 * <p>
 * The step is idempotent, which it has to be: the upgrade manager runs a step whenever the installed version is below
 * the one it applies to, with no upper bound, so a later upgrade can reach it a second time. A class whose table
 * already holds rows is reported and skipped rather than migrated twice.
 */
public class OrientDbToH2UpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(OrientDbToH2UpgradeStep.class);

  /**
   * How many records are read and written before a line of progress is logged. Below this, a class is quiet between
   * its start and its finish: a table of three rows does not need a progress bar.
   */
  private static final int BATCH_SIZE = 500;

  @Value("${OPAL_HOME}/data/orientdb/opal-config")
  private String orientDbPath;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private DatabaseRepository databaseRepository;

  @Autowired
  private SubjectAclRepository subjectAclRepository;

  @Autowired
  private SubjectCredentialsRepository subjectCredentialsRepository;

  @Autowired
  private SubjectProfileRepository subjectProfileRepository;

  @Autowired
  private SubjectTokenRepository subjectTokenRepository;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private ResourceReferenceRepository resourceReferenceRepository;

  @Autowired
  private VCFSamplesMappingRepository vcfSamplesMappingRepository;

  @Autowired
  private OpalAnalysisRepository opalAnalysisRepository;

  @Autowired
  private OpalAnalysisResultRepository opalAnalysisResultRepository;

  @Autowired
  private OpalGeneralConfigRepository opalGeneralConfigRepository;

  @Autowired
  private AppsConfigRepository appsConfigRepository;

  @Autowired
  private AppRepository appRepository;

  @Autowired
  private PodSpecRepository podSpecRepository;

  @Autowired
  private KeyStoreStateRepository keyStoreStateRepository;

  @Autowired
  private DataShieldProfileRepository dataShieldProfileRepository;

  @Autowired
  private RSessionActivityRepository rSessionActivityRepository;

  @Override
  public void execute(Version currentVersion) {
    File source = new File(orientDbPath);
    if(!hasOrientDbDatabase(source)) {
      log.info("No OrientDB configuration database at {}: nothing to migrate", source.getAbsolutePath());
      return;
    }

    List<Migration<?>> migrations = migrations();
    long started = System.currentTimeMillis();

    try(ODatabaseDocument db = new ODatabaseDocumentTx("plocal:" + orientDbPath)) {
      db.open(ORIENTDB_USER, ORIENTDB_PASSWORD);

      logPlan(source, db, migrations);
      for(Migration<?> migration : migrations) {
        migrate(db, migration);
      }
      logSummary(migrations, System.currentTimeMillis() - started, source);
    }
  }

  private <T> void migrate(ODatabaseDocument db, Migration<T> migration) {
    String className = migration.type.getSimpleName();
    migration.read = countInSource(db, className);

    if(migration.read == 0) {
      // Half of these are empty on a typical installation, and eighteen lines saying so would bury the ones that
      // matter.
      log.debug("Migrating {}: nothing to migrate", className);
      return;
    }
    long alreadyThere = migration.repository.count();
    if(alreadyThere > 0) {
      // Only reachable when the step runs a second time. Migrating again would duplicate every row that has no
      // unique key to collide on, so say so and leave it alone.
      log.warn("Migrating {}: skipped, the configuration database already holds {} of them", className, alreadyThere);
      migration.skipped = true;
      return;
    }

    log.info("Migrating {}: {} records", className, migration.read);
    long started = System.currentTimeMillis();
    List<T> batch = new ArrayList<>(BATCH_SIZE);
    ODocument current = null;
    try {
      for(ODocument document : db.browseClass(className)) {
        current = document;
        batch.add(migration.read(document));
        if(batch.size() == BATCH_SIZE) {
          migration.written += write(migration, batch);
          log.info("{}: {}/{} ({}%)", className, migration.written, migration.read,
              migration.read == 0 ? 100 : migration.written * 100 / migration.read);
        }
      }
      migration.written += write(migration, batch);
    } catch(RuntimeException e) {
      // Name the record that was in flight: without it the failure is a stack trace with no way back to the data.
      log.error("Migrating {} failed after {} of {} records, on {}", className, migration.written, migration.read,
          current == null ? "no record" : current.getIdentity(), e);
      throw e;
    }

    migration.elapsed = System.currentTimeMillis() - started;
    if(migration.written == migration.read) {
      log.info("Migrated {}: {} records in {} ms", className, migration.written, migration.elapsed);
    } else {
      log.warn("Migrated {}: {} records written of {} read, in {} ms", className, migration.written, migration.read,
          migration.elapsed);
    }
  }

  private <T> int write(Migration<T> migration, List<T> batch) {
    if(batch.isEmpty()) return 0;
    int size = batch.size();
    // One transaction per batch. The entities carry the state they had in OrientDB, timestamps included: the JPA
    // callbacks only fill a creation date in when there is none, so a migrated row keeps the date it was created on.
    migration.repository.saveAll(batch);
    batch.clear();
    return size;
  }

  private long countInSource(ODatabaseDocument db, String className) {
    if(db.getMetadata().getSchema().getClass(className) == null) return 0;
    return db.countClass(className);
  }

  private boolean hasOrientDbDatabase(File source) {
    if(!source.getParentFile().exists()) return false;
    try(ODatabaseDocument db = new ODatabaseDocumentTx("plocal:" + orientDbPath)) {
      return db.exists();
    } catch(RuntimeException e) {
      log.warn("Cannot open the OrientDB configuration database at {}", source.getAbsolutePath(), e);
      return false;
    }
  }

  private void logPlan(File source, ODatabaseDocument db, List<Migration<?>> migrations) {
    log.info("Migrating the Opal configuration out of OrientDB");
    log.info("  from: {}", source.getAbsolutePath());
    long total = 0;
    for(Migration<?> migration : migrations) {
      long count = countInSource(db, migration.type.getSimpleName());
      total += count;
      if(count > 0) log.info("  {}: {} records", migration.type.getSimpleName(), count);
    }
    log.info("  {} records in total", total);
  }

  private void logSummary(List<Migration<?>> migrations, long elapsed, File source) {
    log.info("Configuration migration complete, in {} ms", elapsed);
    for(Migration<?> migration : migrations) {
      if(migration.skipped) {
        log.info("  {}: skipped, already migrated", migration.type.getSimpleName());
      } else if(migration.read > 0) {
        log.info("  {}: {} read, {} written, {} ms", migration.type.getSimpleName(), migration.read,
            migration.written, migration.elapsed);
      }
    }
    log.info("The OrientDB database has been left untouched at {}.", source.getAbsolutePath());
    log.info("Remove that folder once this installation has been verified; until then it is the way back.");
  }

  private List<Migration<?>> migrations() {
    return Lists.newArrayList(
        new Migration<>(Project.class, projectRepository),
        new Migration<>(Database.class, databaseRepository),
        new Migration<>(SubjectAcl.class, subjectAclRepository),
        new Migration<>(SubjectCredentials.class, subjectCredentialsRepository),
        new Migration<>(SubjectProfile.class, subjectProfileRepository),
        new Migration<>(SubjectToken.class, subjectTokenRepository),
        new Migration<>(Group.class, groupRepository),
        new Migration<>(ResourceReference.class, resourceReferenceRepository),
        new Migration<>(VCFSamplesMapping.class, vcfSamplesMappingRepository),
        new Migration<>(OpalAnalysis.class, opalAnalysisRepository),
        new Migration<>(OpalAnalysisResult.class, opalAnalysisResultRepository),
        new Migration<>(OpalGeneralConfig.class, opalGeneralConfigRepository),
        new Migration<>(AppsConfig.class, appsConfigRepository),
        new Migration<>(App.class, appRepository),
        new Migration<>(PodSpec.class, podSpecRepository),
        new Migration<>(KeyStoreState.class, keyStoreStateRepository),
        new Migration<>(DataShieldProfile.class, dataShieldProfileRepository),
        new Migration<>(RSessionActivity.class, rSessionActivityRepository));
  }

  /**
   * OrientDB was always opened with these, hardcoded, and its security was switched off in the server configuration
   * it no longer has. Reading the folder needs the same credentials it was written with.
   */
  private static final String ORIENTDB_USER = "admin";

  private static final String ORIENTDB_PASSWORD = "admin";

  /**
   * One OrientDB class and where its records go, with the counts and timings the log reports.
   */
  private static class Migration<T> {

    private final Class<T> type;

    private final JpaRepository<T, ?> repository;

    private long read;

    private long written;

    private long elapsed;

    private boolean skipped;

    private Migration(Class<T> type, JpaRepository<T, ?> repository) {
      this.type = type;
      this.repository = repository;
    }

    /**
     * The document as the configuration model reads it. This is what OrientDbService.fromDocument did, with the same
     * Gson: the store held JSON written by that configuration, so it is read back by it.
     */
    private T read(ODocument document) {
      return DomainGson.get().fromJson(document.toJSON(), type);
    }
  }
}
