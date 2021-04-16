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
public class ProjectResourceReferencePermissionConverterTest
    extends OpalPermissionConverterTest<ProjectResourceReferencePermissionConverter.Permission> {

  @Test
  public void testResourceAll() {
    testConversion("/project/patate/resource/pwel", ProjectResourceReferencePermissionConverter.Permission.RESOURCE_ALL, //
        "rest:/project/patate/resource/pwel:*:GET/*",
        "rest:/project/patate:GET:GET",
        "rest:/datasource/patate/tables:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/permissions/resource/pwel:*:GET/*",
        "rest:/system/subject-profiles/_search:GET");
  }

  @Test
  public void testResourceView() {
    testConversion("/project/patate/resource/pwel", ProjectResourceReferencePermissionConverter.Permission.RESOURCE_VIEW, //
        "rest:/project/patate/resource/pwel:GET:GET/GET",
        "rest:/project/patate:GET:GET",
        "rest:/datasource/patate/tables:GET:GET",
        "rest:/project/patate/summary:GET:GET",
        "rest:/project/patate/state:GET:GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new ProjectResourceReferencePermissionConverter();
  }

}
