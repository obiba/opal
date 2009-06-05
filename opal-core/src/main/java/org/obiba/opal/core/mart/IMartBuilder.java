/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.mart;

/**
 * Base interface for defining the mart builders.
 */
public interface IMartBuilder {

  /**
   * Call this before building a mart.
   * @throws Exception
   */
  public void initialize() throws Exception;

  /**
   * Call this to finish the mart build.
   * @throws Exception
   */
  public void shutdown() throws Exception;

}
