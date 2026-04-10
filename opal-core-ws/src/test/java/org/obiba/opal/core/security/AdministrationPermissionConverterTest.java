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
public class AdministrationPermissionConverterTest
    extends OpalPermissionConverterTest<AdministrationPermissionConverter.Permission> {

  @Test
  public void testSystemAll() {
    testConversion("", AdministrationPermissionConverter.Permission.SYSTEM_ALL,
        "rest:/:*:*/*");
  }

  @Test
  public void testAuditAll() {
    testConversion("", AdministrationPermissionConverter.Permission.AUDIT_ALL,
        "rest:/:GET:GET/GET");
  }

  @Test
  public void testProjectAdd() {
    testConversion("", AdministrationPermissionConverter.Permission.PROJECT_ADD,
        "rest:/projects:POST",
        "rest:/files/projects:GET",
        "rest:/system/databases:GET:GET/GET");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new AdministrationPermissionConverter();
  }

}
