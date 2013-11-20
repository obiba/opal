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
   * Constructor for <code>MultiWordSuggestOracle</code> which takes in a set of
   * whitespace chars that filter its input.
   * <p>
   * Example: If <code>".,"</code> is passed in as whitespace, then the string
   * "foo.bar" would match the queries "foo", "bar", "foo.bar", "foo...bar", and
   * "foo, bar". If the empty string is used, then all characters are used in
   * matching. For example, the query "bar" would match "bar", but not "foo
   * bar".
   * </p>
   *
   * @param whitespaceChars the characters to treat as word separators
   */
  public TableVariableSuggestOracle(EventBus eventBus) {
    super(eventBus);
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
