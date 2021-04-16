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
public class FilesPermissionConverterTest extends OpalPermissionConverterTest<FilesPermissionConverter.Permission> {

  @Test
  public void testFilesAll() {
    testConversion("/files", FilesPermissionConverter.Permission.FILES_ALL,
        "rest:/files:*:GET/*",
        "rest:/system/subject-profiles/_search:GET");
    testConversion("/files/patate", FilesPermissionConverter.Permission.FILES_ALL,
        "rest:/files/patate:*:GET/*",
        "rest:/system/subject-profiles/_search:GET");
  }

  @Test
  public void testFilesShare() {
    testConversion("/files", FilesPermissionConverter.Permission.FILES_SHARE,
        "rest:/files:GET:GET/*",
        "rest:/files:POST:GET/*");
    testConversion("/files/patate", FilesPermissionConverter.Permission.FILES_SHARE,
        "rest:/files/patate:GET:GET/*",
        "rest:/files/patate:POST:GET/*");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new FilesPermissionConverter();
  }

}
