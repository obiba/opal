/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.spi.resource.Resource;

/**
 * Manage the {@link ResourceReference}s persisted, by project.
 */
public interface ResourceReferenceService extends SystemService {

  /**
   * Get all resource references from a project. Returns an empty list if there are none.
   *
   * @param project
   * @return
   */
  Iterable<ResourceReference> getResourceReferences(String project);

  /**
   * Get a single resource reference by its name in a project.
   *
   * @param project
   * @param name
   * @return
   * @throws NoSuchResourceReferenceException
   */
  ResourceReference getResourceReference(String project, String name) throws NoSuchResourceReferenceException;

  /**
   * From a resource reference get the appropriate {@link org.obiba.opal.spi.resource.ResourceFactory} and make
   * a {@link Resource}.
   *
   * @param resourceReference
   * @return
   */
  Resource createResource(ResourceReference resourceReference);

  /**
   * Save (create or update) the resource reference.
   *
   * @param resourceReference
   */
  void save(ResourceReference resourceReference);

  /**
   * Delete the resource reference, ignored if it does not exist.
   *
   * @param resourceReference
   */
  void delete(ResourceReference resourceReference);

  /**
   * Delete all the resource references from a project.
   *
   * @param project
   */
  void deleteAll(String project);

}
