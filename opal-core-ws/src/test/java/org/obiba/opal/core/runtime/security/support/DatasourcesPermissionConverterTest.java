/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security.support;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.opal.core.runtime.security.SubjectPermissionConverter;

/**
 *
 */
public class DatasourcesPermissionConverterTest {

  @Test
  public void testArgs() {
    String[] args = OpalPermissionConverter.args("/datasource/patate/table/pwel", "/datasource/(.+)/table/(.+)");
    Assert.assertNotNull(args);
    Assert.assertEquals(2, args.length);
    Assert.assertEquals("patate", args[0]);
    Assert.assertEquals("pwel", args[1]);
  }

  @Test
  public void testDatasourceAll() {
    testConversion("/datasource/patate", DatasourcesPermissionConverter.Permission.DATASOURCE_ALL, //
    "magma:/datasource/patate:*:GET/*");
  }

  @Test
  public void testCreateView() {
    testConversion("/datasource/patate", DatasourcesPermissionConverter.Permission.CREATE_VIEW, //
    "magma:/datasource/patate/tables:GET:GET", //
    "magma:/datasource/patate/views:POST:GET");
  }

  @Test
  public void testViewRead() {
    testConversion("/datasource/patate/view/pwel", DatasourcesPermissionConverter.Permission.VIEW_READ, //
    "magma:/datasource/patate/view/pwel/xml:GET:GET", //
    "magma:/datasource/patate/table/pwel:GET:GET", //
    "magma:/datasource/patate/table/pwel/variable:GET:GET/GET", //
    "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST");
  }

  @Test
  public void testVariableRead() {
    testConversion("/datasource/patate/table/pwel/variable/pouet", DatasourcesPermissionConverter.Permission.VARIABLE_READ, //
    "magma:/datasource/patate/table/pwel/variable/pouet:GET:GET/GET", //
    "magma:/datasource/patate/table/pwel/variable/_transient/summary:POST:GET");
  }

  private void testConversion(String node, DatasourcesPermissionConverter.Permission perm, String... expected) {
    SubjectPermissionConverter converter = new DatasourcesPermissionConverter();
    Assert.assertTrue(converter.canConvert("opal", perm.toString()));
    Assert.assertNotNull(expected);
    Iterable<String> convertedIter = converter.convert("opal", node, perm.toString());
    Assert.assertNotNull(convertedIter);
    int i = 0;
    for(String converted : convertedIter) {
      Assert.assertTrue(i < expected.length);
      Assert.assertEquals(expected[i], converted);
      i++;
    }
    Assert.assertEquals(expected.length, i);
  }

}
