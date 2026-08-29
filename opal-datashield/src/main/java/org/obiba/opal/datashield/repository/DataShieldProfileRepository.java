/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield.repository;

import org.obiba.opal.core.repository.EntityKeys;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DataShieldProfileRepository extends JpaRepository<DataShieldProfile, Long> {

  Optional<DataShieldProfile> findByName(String name);

  /**
   * Delete the stored profile identified by its name, whether or not the object handed in is the one that was loaded.
   */
  default void deleteByKey(DataShieldProfile profile) {
    findByName(profile.getName()).ifPresent(this::delete);
  }

  default DataShieldProfile upsert(DataShieldProfile profile) {
    return save(EntityKeys.reuseKey(findByName(profile.getName()).orElse(null), profile));
  }
}
