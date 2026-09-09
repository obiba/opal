/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import jakarta.validation.constraints.NotNull;
import org.obiba.magma.datasource.jdbc.JdbcDatasourceSettings;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.repository.DatabaseRepository;
import org.obiba.opal.core.runtime.jdbc.H2DatabaseUrls;
import org.obiba.opal.core.runtime.jdbc.H2ProjectFolders;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class ProjectDatabaseServiceImpl implements ProjectDatabaseService {

  private static final Logger log = LoggerFactory.getLogger(ProjectDatabaseServiceImpl.class);

  /**
   * H2 creates the user on first connection. The password is what stops a second process from opening the file
   * casually; it is stored with the rest of the settings, the same exposure as a registered database's password.
   */
  private static final String USERNAME = "opal";

  private static final int PASSWORD_LENGTH = 32;

  private static final String PASSWORD_ALPHABET =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  private static final SecureRandom RANDOM = new SecureRandom();

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private DatabaseRepository databaseRepository;

  @Value("${OPAL_HOME}/data/h2")
  private File h2Root;

  @Override
  public boolean isInternal(@NotNull Project project) {
    // the prefix settles the common case without a query, and the owner column settles the rest: a database an
    // operator registered before that prefix was reserved may carry it and belong to no project at all
    if(!project.hasDatabase() || !project.getDatabase().startsWith(INTERNAL_PREFIX)) return false;
    return getInternalDatabase(project.getName()) //
        .map(database -> database.getName().equals(project.getDatabase())) //
        .orElse(false);
  }

  @Override
  public Optional<Database> getInternalDatabase(@NotNull String projectName) {
    return databaseRepository.findByOwnerProject(projectName);
  }

  @NotNull
  @Override
  public Database ensureInternalDatabase(@NotNull String projectName) {
    Optional<Database> existing = getInternalDatabase(projectName);
    if(existing.isPresent()) return existing.get();

    assertNoNameCollision(projectName);

    Database database = Database.Builder.create() //
        .name(INTERNAL_PREFIX + projectName) //
        .ownerProject(projectName) //
        .usage(Database.Usage.STORAGE) //
        .defaultStorage(false) //
        .usedForIdentifiers(false) //
        .sqlSettings(SqlSettings.Builder.create() //
            .sqlSchema(SqlSettings.SqlSchema.JDBC) //
            .driverClass(H2DatabaseUrls.DRIVER_CLASS) //
            .url(H2DatabaseUrls.projectUrl(projectName)) //
            .username(USERNAME) //
            .password(generatedPassword()) //
            .jdbcDatasourceSettings(defaultJdbcDatasourceSettings())) //
        .build();
    databaseRegistry.createProjectOwned(database);
    return database;
  }

  @Override
  public void deleteInternalDatabase(@NotNull String projectName) {
    Optional<Database> database = getInternalDatabase(projectName);
    if(database.isEmpty()) return;

    // dropping the registration is what invalidates the pool and closes the store, which is what releases the file
    databaseRegistry.unregister(database.get().getName(), projectName);
    databaseRegistry.deleteProjectOwned(database.get());
  }

  @Override
  public void deletePendingFolders() {
    for(File folder : listFolders()) {
      if(!H2ProjectFolders.isPendingDeletion(folder)) continue;
      try {
        H2ProjectFolders.deletePending(folder);
        log.info("Deleted the leftovers of a project database: {}", folder.getAbsolutePath());
      } catch(RuntimeException e) {
        log.warn("Cannot delete the leftovers of a project database, will try again at the next start: {} ({})",
            folder.getAbsolutePath(), e.getMessage());
      }
    }
  }

  @Override
  public Collection<File> listOrphanFolders() {
    Set<String> owners = new HashSet<>();
    databaseRepository.findAll().forEach(database -> {
      if(database.getOwnerProject() != null) owners.add(database.getOwnerProject());
    });

    Collection<File> orphans = new ArrayList<>();
    for(File folder : listFolders()) {
      if(H2ProjectFolders.isPendingDeletion(folder)) continue;
      if(!owners.contains(folder.getName())) orphans.add(folder);
    }
    return orphans;
  }

  /**
   * The folders under the H2 folder. A registered H2 database is a file there, so a folder is either a project's
   * database or something somebody put there.
   */
  private List<File> listFolders() {
    File[] files = h2Root.listFiles(File::isDirectory);
    return files == null ? List.of() : List.of(files);
  }

  /**
   * {@code uk_projects_name} is case sensitive on H2 and on PostgreSQL, so projects 'Foo' and 'foo' can both exist.
   * Their folders cannot: on macOS or Windows {@code data/h2/Foo} and {@code data/h2/foo} are one folder, and the
   * second project would open the first one's database.
   * <p>
   * The refusal stands on every platform, not only where the file system would actually collide. The rule is about the
   * name, not about the disk: a pair of projects that worked on Linux and broke when {@code OPAL_HOME} was copied to a
   * Mac would be a far worse failure than a refusal at creation. It is the same reasoning, and the same comparison,
   * that {@code validUniqueH2DatabaseFile} already applies to registered H2 databases.
   */
  private void assertNoNameCollision(String projectName) {
    // on the rows, because the folder does not exist yet: it is created at the first connection, so a project created
    // a second ago has a database row and nothing on disk
    databaseRepository.findByOwnerProjectIgnoreCase(projectName).ifPresent(other -> {
      throw new ConflictingProjectDatabaseException(projectName, other.getOwnerProject(),
          H2DatabaseUrls.projectFolder(other.getOwnerProject(), h2Root).getAbsolutePath());
    });

    // and on the folders, for a directory somebody put there by hand
    for(File folder : listFolders()) {
      if(folder.getName().equalsIgnoreCase(projectName) && !folder.getName().equals(projectName)) {
        throw new ConflictingProjectDatabaseException(projectName, folder.getName(), folder.getAbsolutePath());
      }
    }
  }

  /**
   * What the administration UI sends when an operator registers a SQL database, so that both paths use one definition.
   * {@code multipleDatasources} is on although a project database holds exactly one datasource: it keeps the schema
   * byte identical to the one Opal creates in a registered database, so the copy and backup commands, opal-sql and any
   * future migration see one schema and not two. The saving would be one narrow column in four metadata tables.
   */
  private JdbcDatasourceSettings defaultJdbcDatasourceSettings() {
    return JdbcDatasourceSettings.newSettings("Participant") //
        .entityIdentifierColumn("opal_id") //
        .createdTimestampColumn("opal_created") //
        .updatedTimestampColumn("opal_updated") //
        .useMetadataTables(true) //
        .multipleDatasources(true) //
        .multilines(false) //
        .batchSize(100) //
        .build();
  }

  private String generatedPassword() {
    StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
    for(int i = 0; i < PASSWORD_LENGTH; i++) {
      password.append(PASSWORD_ALPHABET.charAt(RANDOM.nextInt(PASSWORD_ALPHABET.length())));
    }
    return password.toString();
  }
}
