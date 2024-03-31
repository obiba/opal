/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.obiba.opal.spi.r.AbstractROperation;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Assign textual values to symbols in R.
 */
public class StringAssignROperation extends AbstractROperation {

  private final MultivaluedMap<String, String> symbols;

  public StringAssignROperation(MultivaluedMap<String, String> symbols) {
    this.symbols = symbols;
  }

  public StringAssignROperation(String symbol, String content) {
    symbols = new MultivaluedMapImpl<>();
    symbols.putSingle(symbol, content);
  }

  @Override
  public void doWithConnection() {
    if (symbols == null) return;
    for (Entry<String, List<String>> entry : symbols.entrySet()) {
      for (String content : entry.getValue()) {
        assignScript(entry.getKey(), content);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    for (Entry<String, List<String>> entry : symbols.entrySet()) {
      for (String content : entry.getValue()) {
        buffer.append(entry.getKey()).append(" <- ").append(content).append("\n");
      }
    }
    return buffer.toString();
  }
}
