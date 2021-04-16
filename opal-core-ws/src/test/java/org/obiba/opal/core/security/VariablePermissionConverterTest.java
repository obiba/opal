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
public class VariablePermissionConverterTest
    extends OpalPermissionConverterTest<VariablePermissionConverter.Permission> {

  @Test
  public void testVariableRead() {
    testConversion("/datasource/patate/table/pwel/variable/pouet", VariablePermissionConverter.Permission.VARIABLE_READ,
        "rest:/datasource/patate/table/pwel/variable/pouet:GET:GET/GET",
        "rest:/datasource/patate/table/pwel/variable/_transient/summary:POST:GET",
        "rest:/project/patate:GET:GET",
        "rest:/project/patate/state:GET:GET",
        "rest:/project/patate/summary:GET:GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new VariablePermissionConverter();
  }

}
