/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
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
public class DatasourcePermissionConverterTest
    extends OpalPermissionConverterTest<DatasourcePermissionConverter.Permission> {

  @Test
  public void testDatasourceAll() {
    testConversion("/datasource/patate", DatasourcePermissionConverter.Permission.DATASOURCE_ALL, //
        "rest:/datasource/patate:*:GET/*", //
        "rest:/identifiers/mappings:GET", //
        "rest:/datasource-plugin:GET:GET/GET", //
        "rest:/project/patate:GET:GET", //
        "rest:/project/patate/transient-datasource:*:GET/*", //
        "rest:/project/patate/report-template:*:GET/*", //
        "rest:/project/patate/command:*:GET/*", //
        "rest:/project/patate/permissions/datasource:*:GET/*", //
        "rest:/project/patate/permissions/table:*:GET/*", //
        "rest:/project/patate/permissions/report-template:*:GET/*", //
        "rest:/project/patate/permissions/subject:GET:GET/GET", //
        "rest:/files/projects/patate:GET:GET/*",//
        "rest:/files/projects/patate:POST:GET/*",//
        "rest:/files/projects/patate:PUT:GET/*");
  }

  @Test
  public void testDatasourceView() {
    testConversion("/datasource/patate", DatasourcePermissionConverter.Permission.DATASOURCE_VIEW, //
        "rest:/datasource/patate:GET:GET/GET", //
        "rest:/project/patate/commands/_analyse:POST:GET", //
        "rest:/project/patate/commands/_export:POST:GET", //
        "rest:/project/patate/commands/_copy:POST:GET", //
        "rest:/project/patate/report-templates:GET:GET", //
        "rest:/project/patate/report-templates:POST:GET", //
        "rest:/project/patate:GET:GET", //
        "rest:/project/patate/summary:GET:GET");
  }

  @Test
  public void testCreateTable() {
    testConversion("/datasource/patate", DatasourcePermissionConverter.Permission.TABLE_ADD, //
        "rest:/datasource/patate/tables:GET:GET", //
        "rest:/datasource/patate/tables:POST:GET", //
        "rest:/datasource/patate/tables:DELETE", //
        "rest:/datasource/patate/views:POST:GET", //
        "rest:/identifiers/mappings:GET", //
        "rest:/datasource-plugin:GET:GET/GET", //
        "rest:/project/patate:GET:GET", //
        "rest:/project/patate/summary:GET:GET", //
        "rest:/project/patate/transient-datasources:POST", //
        "rest:/project/patate/commands/_import:POST:GET", //
        "rest:/project/patate/commands/_export:POST:GET", //
        "rest:/project/patate/commands/_copy:POST:GET", //
        "rest:/files/projects/patate:GET:GET/*", //
        "rest:/files/projects/patate:POST:GET/*",//
        "rest:/files/projects/patate:PUT:GET/*");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new DatasourcePermissionConverter();
  }

}
