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

import org.obiba.opal.core.runtime.security.SubjectPermissionConverter;

/**
 *
 */
public abstract class OpalPermissionConverterTest<T> {

  protected void testConversion(String node, T perm, String... expected) {
    SubjectPermissionConverter converter = newConverter();
    Assert.assertTrue(converter.canConvert("opal", perm.toString()));
    Assert.assertNotNull(expected);
    Iterable<String> convertedIter = converter.convert("opal", node, perm.toString());
    Assert.assertNotNull(convertedIter);
    System.out.println("opal:" + node + ":" + perm.toString());
    for(String converted : convertedIter) {
      System.out.println("  " + converted);
    }
    int i = 0;
    for(String converted : convertedIter) {
      Assert.assertTrue(i < expected.length);
      Assert.assertEquals(expected[i], converted);
      i++;
    }
    Assert.assertEquals(expected.length, i);
  }

  protected abstract SubjectPermissionConverter newConverter();

}
