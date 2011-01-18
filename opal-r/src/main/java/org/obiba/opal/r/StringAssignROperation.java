/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r;

import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

/**
 * Assign textual values to symbols in R.
 */
public class StringAssignROperation extends AbstractROperation {

  private MultivaluedMap<String, String> symbols;

  public StringAssignROperation(MultivaluedMap<String, String> symbols) {
    super();
    this.symbols = symbols;
  }

  public StringAssignROperation(String symbol, String content) {
    super();
    symbols = new MultivaluedMapImpl<String, String>();
    symbols.putSingle(symbol, content);
  }

  @Override
  public void doWithConnection() {
    if(symbols == null) return;
    for(Entry<String, List<String>> entry : symbols.entrySet()) {
      for(String content : entry.getValue()) {
        assign(entry.getKey(), content);
      }
    }
  }

}
