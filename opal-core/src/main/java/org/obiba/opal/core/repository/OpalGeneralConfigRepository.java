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

import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * A single row: there has never been anything to be unique about, so the first one is the one.
 */
public interface OpalGeneralConfigRepository extends JpaRepository<OpalGeneralConfig, Long> {

  default Optional<OpalGeneralConfig> findConfig() {
    return findAll().stream().findFirst();
  }

  default OpalGeneralConfig upsert(OpalGeneralConfig config) {
    return save(EntityKeys.reuseKey(findConfig().orElse(null), config));
  }
}
