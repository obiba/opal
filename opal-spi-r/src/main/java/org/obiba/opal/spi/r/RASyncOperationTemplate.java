/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

/**
 * R operation template with extended capabilities for asynchronous execution of R operations.
 */
public interface RASyncOperationTemplate extends ROperationTemplate {

  /**
   * Enqueue an R operation to be executed.
   * @param rop
   * @return the identifier of the R command
   */
  String executeAsync(ROperation rop);

  /**
   * Iterate over the registered R commands.
   * @return
   */
  Iterable<RCommand> getRCommands();

  /**
   * Check if a R command exists with provided identifier.
   * @param id
   * @return
   */
  boolean hasRCommand(String id);

  /**
   * Get the R command from its identifier.
   * @param id
   * @return
   * @throws org.obiba.opal.spi.r.NoSuchRCommandException
   */
  RCommand getRCommand(String id);

  /**
   * Get and remove the R command from its identifier.
   * @param id
   * @return
   * @throws org.obiba.opal.spi.r.NoSuchRCommandException
   */
  RCommand removeRCommand(String id);

}
