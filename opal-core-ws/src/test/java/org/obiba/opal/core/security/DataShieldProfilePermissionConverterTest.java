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
public class DataShieldProfilePermissionConverterTest extends OpalPermissionConverterTest<DataShieldProfilePermissionConverter.Permission> {

  @Test
  public void testDSProfileUse() {
    testConversion("/datashield/profile/pwel", DataShieldProfilePermissionConverter.Permission.DATASHIELD_PROFILE_USE,
        "rest:/datashield/session:*:GET/*",
        "rest:/datashield/env:GET:GET/GET",
        "rest:/datashield/profiles:GET",
        "rest:/service/r/workspaces:GET",
        "rest:/service/r/workspaces:DELETE",
        "rest:/datashield/profile/pwel:GET:GET/GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new DataShieldProfilePermissionConverter();
  }

}
