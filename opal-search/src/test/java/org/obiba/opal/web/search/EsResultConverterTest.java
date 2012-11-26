/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import org.junit.Before;

import junit.framework.Assert;

public class EsResultConverterTest {

  @Before
  public void setUp() throws Exception {
  }

  @org.junit.Test
  public void testConvert_WithNullQueryDto() throws Exception {
    EsResultConverter converter = new EsResultConverter(null);

    Assert.assertFalse(true);

  }
}
