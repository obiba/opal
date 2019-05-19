/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import org.rosuda.REngine.REXPRaw;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Assign a serialized R object value to symbol in R.
 */
public class DataAssignROperation extends AbstractROperation {

  private final String symbol;

  private final byte[] content;

  public DataAssignROperation(String symbol, String base64Content) {
    this.symbol = symbol;
    this.content = Base64.getDecoder().decode(base64Content.replaceAll("\\n", "").replaceAll("\\r", ""));
  }

  @Override
  public void doWithConnection() {
    if(symbol == null) return;
    // write the byte array and unserialize it
    assign(symbol, new REXPRaw(content));
    eval(String.format("is.null(base::assign('%s', value=unserialize(%s)))", symbol, symbol), false);
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append(symbol).append(" <- byte[").append(content == null ? 0 : content.length).append("]\n");
    return buffer.toString();
  }
}
