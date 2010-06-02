/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.test;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractGwtTestSetup {

  protected MockGWTBridge mockBridge;

  @Before
  public void createGwtMockBridge() {
    mockBridge = GWTMockUtilities.disarm();
  }

  @After
  public void restoreNormalBridge() {
    GWTMockUtilities.restore();
  }
}
