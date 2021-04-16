/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.magma.test;

import org.junit.After;
import org.junit.Before;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.xstream.MagmaXStreamExtension;

public abstract class AbstractMagmaTest {

  @Before
  public void startYourEngine() {
    new MagmaEngine();
    MagmaEngine.get().extend(new MagmaXStreamExtension());
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }
}
