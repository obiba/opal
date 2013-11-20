/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.security.support;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.opal.core.runtime.security.SubjectPermissionConverter;

/**
 *
 */
public class ProjectsPermissionConverterTest
    extends OpalPermissionConverterTest<ProjectsPermissionConverter.Permission> {

  @Test
  public void testArgs() {
    String[] args = DomainPermissionConverter.args("/datasource/patate/table/pwel", "/datasource/(.+)/table/(.+)");
    Assert.assertNotNull(args);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals("patate", args[0]);
    Assert.assertEquals("pwel", args[1]);
  }

  @Test
  public void testProjectAll() {
    testConversion("/project/patate", ProjectsPermissionConverter.Permission.PROJECT_ALL, //
        "magma:/datasource/patate:*:GET/*", //
        "magma:/functional-units/unit:GET:GET/GET", //
        "magma:/functional-units/entities/table:GET",//
        "magma:/project/patate:*:GET/*",//
        "magma:/files/projects/patate:*:GET/*");
  }

  @Test
  public void testDatasourceAll() {
    testConversion("/datasource/patate", ProjectsPermissionConverter.Permission.DATASOURCE_ALL, //
        "magma:/datasource/patate:*:GET/*", //
        "magma:/functional-units/unit:GET:GET/GET", //
        "magma:/functional-units/entities/table:GET",//
        "magma:/project/patate:GET:GET/*",//
        "magma:/files/projects/patate:GET:GET/*",//
        "magma:/files/projects/patate:POST:GET/*",//
        "magma:/files/projects/patate:PUT:GET/*");
  }

  @Test
  public void testCreateTable() {
    testConversion("/datasource/patate", ProjectsPermissionConverter.Permission.CREATE_TABLE, //
        "magma:/datasource/patate/tables:GET:GET", //
        "magma:/datasource/patate/tables:POST:GET", //
        "magma:/datasource/patate/views:POST:GET", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET", //
        "magma:/files/projects/patate:GET:GET/*", //
        "magma:/files/projects/patate:POST:GET/*",//
        "magma:/files/projects/patate:PUT:GET/*");
  }

  @Test
  public void testTableAll() {
    testConversion("/datasource/patate/table/pwel", ProjectsPermissionConverter.Permission.TABLE_ALL, //
        "magma:/datasource/patate/table/pwel:*:GET/*", //
        "magma:/project/patate/commands/_export:POST:GET", //
        "magma:/project/patate/commands/_copy:POST:GET", //
        "magma:/project/patate/report-templates:GET:GET", //
        "magma:/project/patate/report-templates:POST:GET", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET", //
        "magma:/project/patate/permissions/table/pwel:*:GET/*", //
        "magma:/files/projects/patate:GET:GET/*", //
        "magma:/files/projects/patate:POST:GET/*", //
        "magma:/files/projects/patate:PUT:GET/*");
  }

  @Test
  public void testTableAllOnView() {
    testConversion("/datasource/patate/view/pwel", ProjectsPermissionConverter.Permission.TABLE_ALL, //
        "magma:/datasource/patate/table/pwel:*:GET/*", //
        "magma:/project/patate/commands/_export:POST:GET", //
        "magma:/project/patate/commands/_copy:POST:GET", //
        "magma:/project/patate/report-templates:GET:GET", //
        "magma:/project/patate/report-templates:POST:GET", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET", //
        "magma:/project/patate/permissions/table/pwel:*:GET/*", //
        "magma:/files/projects/patate:GET:GET/*", //
        "magma:/files/projects/patate:POST:GET/*", //
        "magma:/files/projects/patate:PUT:GET/*", //
        "magma:/datasource/patate/view/pwel:*:GET/*");
  }

  @Test
  public void testTableEdit() {
    testConversion("/datasource/patate/table/pwel", ProjectsPermissionConverter.Permission.TABLE_EDIT, //
        "magma:/datasource/patate/table/pwel:PUT:GET", //
        "magma:/datasource/patate/table/pwel/variables:POST:GET/*", //
        "magma:/datasource/patate/table/pwel/variables:DELETE:GET", //
        "magma:/datasource/patate/table/pwel/index:GET:GET", //
        "magma:/datasource/patate/table/pwel/index/schedule:GET:GET", //
        "magma:/files/projects/patate:GET:GET/*",//
        "magma:/files/projects/patate:POST:GET/*",//
        "magma:/files/projects/patate:PUT:GET/*", //
        "magma:/datasource/patate/table/pwel:GET:GET", //
        "magma:/datasource/patate/table/pwel/variable:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/variables:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facet:GET:GET/GET",//
        "magma:/datasource/patate/table/pwel/facets/_search:POST:GET",//
        "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET");
  }

  @Test
  public void testTableValuesEdit() {
    testConversion("/datasource/patate/table/pwel", ProjectsPermissionConverter.Permission.TABLE_VALUES_EDIT, //
        "magma:/datasource/patate/table/pwel/valueSet:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/entities:GET", //
        "magma:/datasource/patate/table/pwel/index:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/index/_search:GET", //
        "magma:/datasource/patate/table/pwel/index/_search:POST", //
        "magma:/datasource/patate/table/pwel/index/_schema:GET", //
        "magma:/project/patate/commands/_export:POST:GET", //
        "magma:/project/patate/commands/_copy:POST:GET", //
        "magma:/project/patate/report-templates:GET:GET", //
        "magma:/project/patate/report-templates:POST:GET", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET", //
        "magma:/datasource/patate/table/pwel:GET:GET", //
        "magma:/datasource/patate/table/pwel/variable:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/variables:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facet:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facets/_search:POST:GET", //
        "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET", //
        "magma:/datasource/patate/table/pwel:PUT:GET", //
        "magma:/datasource/patate/table/pwel/variables:POST:GET/*", //
        "magma:/datasource/patate/table/pwel/variables:DELETE:GET", //
        "magma:/datasource/patate/table/pwel/index:GET:GET", //
        "magma:/datasource/patate/table/pwel/index/schedule:GET:GET", //
        "magma:/files/projects/patate:GET:GET/*", //
        "magma:/files/projects/patate:POST:GET/*", //
        "magma:/files/projects/patate:PUT:GET/*", //
        "magma:/datasource/patate/table/pwel:GET:GET", //
        "magma:/datasource/patate/table/pwel/variable:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/variables:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facet:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facets/_search:POST:GET", //
        "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET");
  }

  @Test
  public void testTableReadOnView() {
    testConversion("/datasource/patate/view/pwel", ProjectsPermissionConverter.Permission.TABLE_READ, //
        "magma:/datasource/patate/table/pwel:GET:GET", //
        "magma:/datasource/patate/table/pwel/variable:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/variables:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facet:GET:GET/GET",//
        "magma:/datasource/patate/table/pwel/facets/_search:POST:GET",//
        "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST",//
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET", //
        "magma:/datasource/patate/view/pwel/xml:GET:GET");
  }

  @Test
  public void testTableValuesOnView() {
    testConversion("/datasource/patate/view/pwel", ProjectsPermissionConverter.Permission.TABLE_VALUES, //
        "magma:/datasource/patate/table/pwel/valueSet:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/entities:GET", //
        "magma:/datasource/patate/table/pwel/index:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/index/_search:GET", //
        "magma:/datasource/patate/table/pwel/index/_search:POST", //
        "magma:/datasource/patate/table/pwel/index/_schema:GET", //
        "magma:/project/patate/commands/_export:POST:GET", //
        "magma:/project/patate/commands/_copy:POST:GET", //
        "magma:/project/patate/report-templates:GET:GET", //
        "magma:/project/patate/report-templates:POST:GET", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET", //
        "magma:/datasource/patate/table/pwel:GET:GET", //
        "magma:/datasource/patate/table/pwel/variable:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/variables:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facet:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facets/_search:POST:GET", //
        "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET", //
        "magma:/datasource/patate/view/pwel/xml:GET:GET");
  }

  @Test
  public void testVariableRead() {
    testConversion("/datasource/patate/table/pwel/variable/pouet", ProjectsPermissionConverter.Permission.VARIABLE_READ,
        //
        "magma:/datasource/patate/table/pwel/variable/pouet:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST:GET", //
        "magma:/project/patate:GET:GET", //
        "magma:/project/patate/summary:GET:GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new ProjectsPermissionConverter();
  }

}
