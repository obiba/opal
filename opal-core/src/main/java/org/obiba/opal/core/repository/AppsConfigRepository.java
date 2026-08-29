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

import org.obiba.opal.core.domain.AppsConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Also a single row, its identifier fixed at "1" since it was written.
 */
public interface AppsConfigRepository extends JpaRepository<AppsConfig, String> {

  default Optional<AppsConfig> findConfig() {
    return findAll().stream().findFirst();
  }

  default AppsConfig upsert(AppsConfig config) {
    return save(config);
  }
}
