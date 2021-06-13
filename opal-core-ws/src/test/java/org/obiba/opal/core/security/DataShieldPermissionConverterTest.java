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
public class DataShieldPermissionConverterTest extends OpalPermissionConverterTest<DataShieldPermissionConverter.Permission> {

  @Test
  public void testDSUse() {
    testConversion("/", DataShieldPermissionConverter.Permission.DATASHIELD_USE,
        "rest:/datashield/session:*:GET/*",
        "rest:/datashield/env:GET:GET/GET",
        "rest:/datashield/profiles:GET",
        "rest:/service/r/workspaces:GET",
        "rest:/service/r/workspaces:DELETE");
  }

  @Test
  public void testDSAll() {
    testConversion("/", DataShieldPermissionConverter.Permission.DATASHIELD_ALL,
        "rest:/datashield/session:*:GET/*",
        "rest:/datashield/packages:*:GET/*",
        "rest:/datashield/profiles:*:GET/*",
        "rest:/datashield/options:*:GET/*",
        "rest:/datashield/env/aggregate/methods:*:GET/*",
        "rest:/datashield/env/assign/methods:*:GET/*",
        "rest:/service/r:GET",
        "rest:/service/r:PUT",
        "rest:/service/r:DELETE",
        "rest:/service/r/clusters:GET",
        "rest:/service/r/sessions:GET",
        "rest:/service/r/workspaces:GET",
        "rest:/service/r/workspaces:DELETE",
        "rest:/system/permissions/datashield:*:GET/*",
        "rest:/system/subject-profiles/_search:GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new DataShieldPermissionConverter();
  }

}
