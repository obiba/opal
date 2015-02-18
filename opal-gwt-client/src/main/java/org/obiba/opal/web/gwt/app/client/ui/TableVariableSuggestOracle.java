/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;

public class TableVariableSuggestOracle extends VariableSuggestOracle {

  private static final int LABEL_MAX_SIZE = 75;

  /**
   * Same behavior as VariableSuggestOracle but the list of results do not display the datasource and table name
   *
   * @param eventBus
   */
  public TableVariableSuggestOracle(EventBus eventBus) {
    super(eventBus);
  }

  @Override
  public String getOriginalQuery() {
    // Filter query for value types
    return originalQuery + " valueType:(integer OR text OR decimal OR date OR datetime OR boolean)";
  }

  @Override
  protected VariableSuggestion convertToFormattedSuggestions(String query, Map<String, String> attributes) {
    SafeHtmlBuilder accum = new SafeHtmlBuilder();

    accum.appendHtmlConstant("<span class='variable-search-suggest-box'>");
    accum.appendHtmlConstant("<strong>");
    accum.appendEscaped(attributes.get("name"));
    accum.appendHtmlConstant("</strong>");

    if(attributes.containsKey("label")) {
      accum.appendHtmlConstant("<br>");

      String label = attributes.get("label");
      if(label.length() > LABEL_MAX_SIZE) {
        label = label.substring(0, LABEL_MAX_SIZE) + " ...";
      }
      accum.appendEscaped(label);
    }
    accum.appendHtmlConstant("</span>");

    return createSuggestion(query, accum.toSafeHtml().asString(), attributes.get("datasource"), attributes.get("table"),
        attributes.get("name"));
  }
}
