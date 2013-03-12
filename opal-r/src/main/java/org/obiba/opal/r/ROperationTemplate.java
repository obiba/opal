/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r;

/**
 * R operation template is responsible for managing the R connection and make it available to a {@link ROperation}.
 */
public interface ROperationTemplate {

  /**
   * Set up the R connection, do the R operation and clean up the R connection.
   *
   * @param rop
   */
  public void execute(ROperation rop);

  public void execute(Iterable<ROperation> rop);

}
