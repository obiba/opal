/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.mart.sas;

import org.obiba.opal.core.mart.IMartBuilder;

/**
 * Interface for defining the SAS mart builders.
 */
public interface ISasMartBuilder extends IMartBuilder {

  public void enableOccurrences();

  /**
   * Declares the variable names.
   * @param headers
   */
  public void setVariableNames(String... names);

  /**
   * Set the values in the same order, including null values, than the variable names.
   * @param participantId
   * @param values
   */
  public void withData(String participantId, Object... values);

  /**
   * Set the values in the same order, including null values, than the variable names.
   * @param participantId
   * @param values
   */
  public void withData(String participantId, int occurrence, Object... values);

}
