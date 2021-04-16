/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.security;

import org.junit.Test;

/**
 *
 */
public class ProjectResourceReferencesPermissionConverterTest
    extends OpalPermissionConverterTest<ProjectResourceReferencesPermissionConverter.Permission> {

  @Test
  public void testResourcesAll() {
    testConversion("/project/patate/resources", ProjectResourceReferencesPermissionConverter.Permission.RESOURCES_ALL, //
        "rest:/project/patate/resources:*:GET/*",
        "rest:/project/patate:GET:GET",
        "rest:/datasource/patate/tables:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/permissions/resources:*:GET/*",
        "rest:/files/projects/patate:GET:GET/*",
        "rest:/files/projects/patate:POST:GET/*",
        "rest:/files/projects/patate:PUT:GET/*",
        "rest:/system/subject-profiles/_search:GET");
  }

  @Test
  public void testResourcesView() {
    testConversion("/project/patate/resources", ProjectResourceReferencesPermissionConverter.Permission.RESOURCES_VIEW, //
        "rest:/project/patate/resources:GET:GET/GET",
        "rest:/project/patate:GET:GET",
        "rest:/datasource/patate/tables:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/project/patate/state:GET:GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new ProjectResourceReferencesPermissionConverter();
  }

}
