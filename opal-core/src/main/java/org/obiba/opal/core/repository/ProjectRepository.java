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

import org.obiba.opal.core.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  Optional<Project> findByName(String name);

  /**
   * Delete the stored Project identified by its natural key, whether or not the object handed in is the one that was
   * loaded. {@code delete} alone would not: given an object built by a caller, it has no primary key to delete by and
   * silently does nothing, where the document store this replaces always resolved the unique properties first.
   */
  default void deleteByKey(Project project) {
    findByName(project.getName()).ifPresent(this::delete);
  }

  default Project upsert(Project project) {
    return save(EntityKeys.reuseKey(findByName(project.getName()).orElse(null), project));
  }
}
