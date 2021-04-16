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

import static org.fest.assertions.api.Assertions.assertThat;

/**
 *
 */
public abstract class OpalPermissionConverterTest<T> {

  protected void testConversion(String node, T perm, String... expected) {
    SubjectPermissionConverter converter = newConverter();
    assertThat(converter.canConvert("opal", perm.toString())).isTrue();
    assertThat(expected).isNotNull();
    Iterable<String> convertedIter = converter.convert("opal", node, perm.toString());
    assertThat(convertedIter).isNotNull();
    System.out.println("opal:" + node + ":" + perm);
    for(String converted : convertedIter) {
      System.out.println("  " + converted);
    }
    int i = 0;
    for(String converted : convertedIter) {
      assertThat(expected.length).isGreaterThan(i);
      assertThat(expected[i]).isEqualTo(converted);
      i++;
    }
    assertThat(expected.length).isEqualTo(i);
  }

  protected abstract SubjectPermissionConverter newConverter();

}
