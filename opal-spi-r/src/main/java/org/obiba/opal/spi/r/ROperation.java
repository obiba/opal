/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

/**
 * Once a R connection is setup by a {@link ROperationTemplate}, this class handles the real things to be done on it.
 */
public interface ROperation {

  /**
   * Does anything with the provided R connection.
   *
   * @param connection
   */
  void doWithConnection(RServerConnection connection);

}
