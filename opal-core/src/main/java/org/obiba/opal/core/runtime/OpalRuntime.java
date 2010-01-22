/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime;

import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
public class OpalRuntime {
  //
  // Instance Variables
  //

  private OpalConfiguration opalConfiguration;

  //
  // InitializingBean Methods
  //

  @Transactional
  public void init() throws Exception {
    MagmaEngineFactory magmaEngineFactory = opalConfiguration.getMagmaEngineFactory();
    magmaEngineFactory.create();
  }

  //
  // Methods
  //

  public void setOpalConfiguration(OpalConfiguration opalConfiguration) {
    this.opalConfiguration = opalConfiguration;
  }

}
