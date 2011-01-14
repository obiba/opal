/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPRaw;

/**
 *
 */
public interface ROperationWithResult {

  public boolean hasResult();

  public REXP getResult();

  public boolean hasRawResult();

  public REXPRaw getRawResult();

}
