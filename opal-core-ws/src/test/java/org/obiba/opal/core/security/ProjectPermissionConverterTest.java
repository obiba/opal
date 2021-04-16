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
public class ProjectPermissionConverterTest
    extends OpalPermissionConverterTest<ProjectPermissionConverter.Permission> {


  @Test
  public void testProjectAll() {
    testConversion("/project/patate", ProjectPermissionConverter.Permission.PROJECT_ALL,
        "rest:/datasource/patate:*:GET/*",
        "rest:/identifiers/mappings:GET",
        "rest:/datasource-plugin:GET:GET/GET",
        "rest:/project/patate:*:GET/*",
        "rest:/files/projects/patate:*:GET/*",
        "rest:/system/subject-profiles/_search:GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new ProjectPermissionConverter();
  }

}
