/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import com.google.common.base.Joiner;

import java.util.List;

/**
 * Bind the rows of R objects using dplyr and assign result to a symbol in R.
 */
public class BindRowsAssignROperation extends AbstractROperation {

  private final String symbol;

  private final List<String> symbols;

  // remove symbols after being bound
  private final boolean remove;

  public BindRowsAssignROperation(String symbol, List<String> symbols) {
    this(symbol, symbols, true);
  }

  public BindRowsAssignROperation(String symbol, List<String> symbols, boolean remove) {
    this.symbol = symbol;
    this.symbols = symbols;
    this.remove = remove;
  }

  @Override
  public void doWithConnection() {
    if(symbol == null) return;
    if (symbols.size()>1) {
      ensurePackage("dplyr");
      eval(String.format("is.null(base::assign('%s', value=dplyr::bind_rows(%s)))", symbol, getArguments()), false);
      eval(String.format("for (n in names(`%s`)) attributes(`%s`[[n]]) <- attributes(`%s`[[n]])", symbol, symbol, symbols.get(0)), false);
      if (remove) {
        eval(String.format("is.null(base::rm(list=c('%s')))", Joiner.on("','").join(symbols)), false);
      }
    } else if (symbol != symbols.get(0)) {
      eval(String.format("is.null(base::assign('%s', value=`%s`))", symbol, symbols.get(0)), false);
    }
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append(symbol).append(" <- dplyr::bind_rows(").append(getArguments()).append(")\n");
    return buffer.toString();
  }

  private String getArguments() {
    return "`" + Joiner.on("`,`").join(symbols) + "`";
  }
}
