/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
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
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Base for the service tests that read and write the configuration database. The hooks are kept from the OrientDB
 * base this replaces, as that is where the tests needing an empty table clear it.
 */
public abstract class AbstractConfigDbTest extends AbstractJUnit4SpringContextTests {

  @Before
  public void startDB() throws Exception {
  }

  @After
  public void stopDB() {
  }
}
