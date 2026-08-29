/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.repository;

import org.obiba.opal.core.domain.database.Database;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatabaseRepository extends JpaRepository<Database, Long> {

  Optional<Database> findByName(String name);

  List<Database> findByUsedForIdentifiersOrderByName(boolean usedForIdentifiers);

  List<Database> findByUsedForIdentifiersAndUsageOrderByName(boolean usedForIdentifiers, Database.Usage usage);

  List<Database> findByUsedForIdentifiersAndSqlSettingsIsNotNullOrderByName(boolean usedForIdentifiers);

  List<Database> findByUsedForIdentifiersAndMongoDbSettingsIsNotNullOrderByName(boolean usedForIdentifiers);

  List<Database> findBySqlSettingsIsNotNull();

  Optional<Database> findByUsedForIdentifiersAndDefaultStorage(boolean usedForIdentifiers, boolean defaultStorage);

  Optional<Database> findByUsedForIdentifiers(boolean usedForIdentifiers);

  /**
   * Delete the stored Database identified by its natural key, whether or not the object handed in is the one that was
   * loaded. {@code delete} alone would not: given an object built by a caller, it has no primary key to delete by and
   * silently does nothing, where the document store this replaces always resolved the unique properties first.
   */
  default void deleteByKey(Database database) {
    findByName(database.getName()).ifPresent(this::delete);
  }

  default Database upsert(Database database) {
    return save(EntityKeys.reuseKey(findByName(database.getName()).orElse(null), database));
  }
}
