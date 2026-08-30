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

import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PodSpecRepository extends JpaRepository<PodSpec, String> {

  /**
   * The specification is stored under the identifier it was created with, so it is already the key.
   */
  default PodSpec upsert(PodSpec spec) {
    return save(spec);
  }
}
