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

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

/**
 * Bind the rows of R objects using dplyr and assign result to a symbol in R.
 */
public class ResourceTibbleAssignROperation extends AbstractROperation {

  private final String symbol;

  private final String clientSymbol;

  public ResourceTibbleAssignROperation(String symbol, String clientSymbol) {
    this.symbol = symbol;
    this.clientSymbol = clientSymbol;
  }

  @Override
  public void doWithConnection() {
    if (symbol == null) return;

    try {
      ensurePackage("resourcer");
      ensurePackage("dplyr");
      ensurePackage("moments"); // needed for descriptive stats
      String script = String.format("resourcer::as.resource.tbl(%s)", clientSymbol);
      eval(String.format("is.null(base::assign('%s', %s))", symbol, script), RSerialize.NATIVE);
    } catch (Exception e) {
      throw new RRuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return String.format("%s <- as.resource.tbl(%s)", symbol, clientSymbol);
  }

}
