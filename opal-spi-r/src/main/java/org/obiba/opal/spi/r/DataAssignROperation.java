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

import java.util.Base64;

/**
 * Assign a serialized R object value to symbol in R.
 */
public class DataAssignROperation extends AbstractROperation {

  private final String symbol;

  private final String base64Content;

  public DataAssignROperation(String symbol, String base64Content) {
    this.symbol = symbol;
    this.base64Content = base64Content;
  }

  @Override
  public void doWithConnection() {
    if (symbol == null) return;
    // write the byte array and unserialize it
    assignData(symbol, base64Content);
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append(symbol).append(" <- byte[").append(base64Content == null ? 0 : base64Content.length()).append("]");
    return buffer.toString();
  }
}
