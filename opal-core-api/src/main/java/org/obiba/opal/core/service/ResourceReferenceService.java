/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.ResourceReference;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ResourceAssignROperation;
import org.obiba.opal.spi.resource.Resource;

import java.util.List;

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
  List<ResourceReference> getResourceReferences(String project);

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
   * Verify if there is a resource reference in a project with the provided name.
   *
   * @param project
   * @param name
   * @return
   */
  boolean hasResourceReference(String project, String name);

  /**
   * From a resource reference get the appropriate resource factory and make
   * a {@link Resource}.
   *
   * @param resourceReference
   * @return
   */
  Resource createResource(ResourceReference resourceReference);

  /**
   * Get the required package names from the appropriate resource factory.
   * @param resourceReference
   * @return empty if none
   */
  List<String> getRequiredPackages(ResourceReference resourceReference);

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
   * Delete a resource reference, by its name and project, ignored if it does not exist.
   *
   * @param project
   * @param name
   */
  void delete(String project, String name);

  /**
   * Delete all the resource references from a project.
   *
   * @param project
   */
  void deleteAll(String project);

  /**
   * Make assignment R operation from resource reference.
   *
   * @param project
   * @param name
   * @return
   * @throws NoSuchResourceReferenceException
   */
  ResourceAssignROperation asAssignOperation(String project, String name, String symbol) throws NoSuchResourceReferenceException;

  /**
   * Get the profile name that could be associated to this resource reference.
   *
   * @param project
   * @param name
   * @return
   */
  String getProfile(String project, String name);
}
