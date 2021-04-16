/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource;

import org.obiba.magma.DatasourceFactory;
import org.obiba.opal.spi.r.datasource.magma.RSymbolWriter;

public interface RDatasourceFactory extends DatasourceFactory {

  /**
   * Set the accessor to the R session for executing operations.
   *
   * @param sessionHandler
   */
  void setRSessionHandler(RSessionHandler sessionHandler);

  /**
   * Output parameters to write a tibble symbol into another format.
   *
   * @return
   */
  RSymbolWriter createSymbolWriter();

}
