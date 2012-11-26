/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.support.StaticValueTable;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class IndexManagerConfigurationTest {

  @Before
  public void setupDataAndKeysTable() {
    new MagmaEngine();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_gracePeriod() {

    //isReadyForIndexing

    assertTrue(true);


  }

}
