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

import org.junit.Test;
import org.obiba.opal.core.runtime.security.SubjectPermissionConverter;

import junit.framework.Assert;

/**
 *
 */
public class DatasourcesPermissionConverterTest extends
    OpalPermissionConverterTest<DatasourcesPermissionConverter.Permission> {

  @Test
  public void testArgs() {
    String[] args = DomainPermissionConverter.args("/datasource/patate/table/pwel", "/datasource/(.+)/table/(.+)");
    Assert.assertNotNull(args);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals("patate", args[0]);
    Assert.assertEquals("pwel", args[1]);
  }

  @Test
  public void testDatasourceAll() {
    testConversion("/datasource/patate", DatasourcesPermissionConverter.Permission.DATASOURCE_ALL, //
        "magma:/datasource/patate:*:GET/*", //
        "magma:/shell/command:*:GET/*", //
        "magma:/functional-units/unit:GET:GET/GET", //
        "magma:/functional-units/entities/table:GET");
  }

  @Test
  public void testCreateTable() {
    testConversion("/datasource/patate", DatasourcesPermissionConverter.Permission.CREATE_TABLE, //
        "magma:/datasource/patate/tables:GET:GET", //
        "magma:/datasource/patate/tables:POST:GET");
  }

  @Test
  public void testCreateView() {
    testConversion("/datasource/patate", DatasourcesPermissionConverter.Permission.CREATE_VIEW, //
        "magma:/datasource/patate/tables:GET:GET", //
        "magma:/datasource/patate/views:POST:GET");
  }

  @Test
  public void testTableEdit() {
    testConversion("/datasource/patate/table/pwel", DatasourcesPermissionConverter.Permission.TABLE_EDIT, //
        "magma:/datasource/patate/table/pwel/variables:POST:GET", //
        "magma:/datasource/patate/table/pwel/index:*:GET", //
        "magma:/datasource/patate/table/pwel/index/schedule:*:GET", //
        "magma:/datasource/patate/table/pwel:GET:GET", //
        "magma:/datasource/patate/table/pwel/variable:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/variables:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facet:GET:GET/GET",//
        "magma:/datasource/patate/table/pwel/facets/_search:POST:GET",//
        "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST");
  }

  @Test
  public void testViewRead() {
    testConversion("/datasource/patate/view/pwel", DatasourcesPermissionConverter.Permission.VIEW_READ, //
        "magma:/datasource/patate/view/pwel/xml:GET:GET", //
        "magma:/datasource/patate/table/pwel:GET:GET", //
        "magma:/datasource/patate/table/pwel/variable:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/variables:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/facet:GET:GET/GET",//
        "magma:/datasource/patate/table/pwel/facets/_search:POST:GET",//
        "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST");
  }

  @Test
  public void testViewValues() {
    testConversion("/datasource/patate/view/pwel", DatasourcesPermissionConverter.Permission.VIEW_VALUES, //
        "magma:/datasource/patate/table/pwel/valueSet:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/entities:GET", //
        "magma:/datasource/patate/table/pwel/variables:GET", //
        "magma:/datasource/patate/table/pwel/index:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/index/_search:GET",//
        "magma:/datasource/patate/table/pwel/index/_search:POST",//
        "magma:/datasource/patate/table/pwel/index/_schema:GET",//
        "magma:/datasource/patate/table/pwel/facet:GET:GET/GET",//
        "magma:/datasource/patate/table/pwel/facets/_search:POST:GET");
  }

  @Test
  public void testVariableRead() {
    testConversion("/datasource/patate/table/pwel/variable/pouet",
        DatasourcesPermissionConverter.Permission.VARIABLE_READ, //
        "magma:/datasource/patate/table/pwel/variable/pouet:GET:GET/GET", //
        "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST:GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new DatasourcesPermissionConverter();
  }

}
