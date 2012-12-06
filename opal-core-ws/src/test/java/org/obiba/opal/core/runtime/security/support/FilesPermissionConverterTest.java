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

import org.junit.Test;
import org.obiba.opal.core.runtime.security.SubjectPermissionConverter;

/**
 *
 */
public class FilesPermissionConverterTest extends OpalPermissionConverterTest<FilesPermissionConverter.Permission> {

  @Test
  public void testFilesAll() {
    testConversion("/files", FilesPermissionConverter.Permission.FILES_ALL, //
        "magma:/files:*:GET/*");
    testConversion("/files/patate", FilesPermissionConverter.Permission.FILES_ALL, //
        "magma:/files/patate:*:GET/*");
  }

  @Test
  public void testFilesShare() {
    testConversion("/files", FilesPermissionConverter.Permission.FILES_SHARE, //
        "magma:/files:GET:GET/*", //
        "magma:/files:POST:GET/*");
    testConversion("/files/patate", FilesPermissionConverter.Permission.FILES_SHARE, //
        "magma:/files/patate:GET:GET/*", //
        "magma:/files/patate:POST:GET/*");
  }

  @Override
  protected SubjectPermissionConverter newConverter() {
    return new FilesPermissionConverter();
  }

}
