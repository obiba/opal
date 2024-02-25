/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

public abstract class AbstractOrientdbServiceTest extends AbstractJUnit4SpringContextTests {

  @Autowired
  protected OrientDbServerFactory orientDbServerFactory;

  @Before
  public void startDB() throws Exception {
    orientDbServerFactory.start();
  }

  @After
  public void stopDB() {
    orientDbServerFactory.stop();
  }
}
