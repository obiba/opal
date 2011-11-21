/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

public class MultiWordSuggestOracleWithDisplay extends MultiWordSuggestOracle {

  Map<String, String> map = new HashMap<String, String>();

  public void add(String replacementString, String displayString) {
    super.add(replacementString);
    map.put(replacementString, displayString);
  }

  @Override
  protected MultiWordSuggestion createSuggestion(String replacementString, String displayString) {
    return super.createSuggestion(replacementString, "<div style='float:left'>" + displayString + createSpaces() + "</div><div style='float:right'>(" + map.get(replacementString) + ")</div>");
  }

  private String createSpaces() {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < 25; i++) {
      sb.append("&nbsp.");
    }
    return sb.toString();
  }
}
