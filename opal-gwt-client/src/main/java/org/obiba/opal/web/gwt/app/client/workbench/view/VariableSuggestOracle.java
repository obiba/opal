/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.workbench.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.search.ItemFieldsDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;

public class VariableSuggestOracle extends SuggestOracle {

  private static final int LABEL_MAX_SIZE = 75;

  /**
   * Suggestion class for {@link MultiWordSuggestOracle}.
   */
  public static class VariableSuggestion implements Suggestion, IsSerializable {
    private String displayString;

    private String replacementString;

    private String datasource;

    private String table;

    private String variable;

    /**
     * Constructor used by RPC.
     */
    @SuppressWarnings("UnusedDeclaration")
    public VariableSuggestion() {
    }

    /**
     * Constructor for <code>MultiWordSuggestion</code>.
     *
     * @param replacementString the string to enter into the SuggestBox's text
     * box if the suggestion is chosen
     * @param displayString the display string
     * @param table1
     * @param variable1
     */
    public VariableSuggestion(String replacementString, String displayString, String datasource, String table,
        String variable) {
      this.replacementString = replacementString;
      this.displayString = displayString;
      this.datasource = datasource;
      this.table = table;
      this.variable = variable;
    }

    @Override
    public String getDisplayString() {
      return displayString;
    }

    @Override
    public String getReplacementString() {
      return replacementString;
    }

    public String getTable() {
      return table;
    }

    public String getVariable() {
      return variable;
    }

    public String getDatasource() {
      return datasource;
    }
  }

  private Response defaultResponse;

  private final EventBus eventBus;

  private String datasource;

  private String table;

  private String originalQuery;

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
  public VariableSuggestOracle(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public boolean isDisplayStringHTML() {
    return true;
  }

  @Override
  public void requestDefaultSuggestions(Request request, Callback callback) {
    if(defaultResponse != null) {
      callback.onSuggestionsReady(request, defaultResponse);
    } else {
      requestSuggestions(request, callback);
    }
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String getOriginalQuery() {
    return originalQuery;
  }

  @Override
  public void requestSuggestions(final Request request, final Callback callback) {
    String prefix = "";
    if(datasource != null) {
      prefix = "datasource:" + datasource + " ";
    }
    if(table != null) {
      prefix += "table:" + table + " ";
    }

    originalQuery = request.getQuery();
    final String query = request.getQuery() == null ? prefix + "*" : prefix + request.getQuery();

    UriBuilder ub = UriBuilder.create().segment("datasources", "variables", "_search")//
        .query("query", query)//
        .query("field", "name", "field", "datasource", "field", "table", "field", "label", "field", "label-en");

    // Get candidates from search words.
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(ub.build()).get()
        .withCallback(com.google.gwt.http.client.Response.SC_BAD_REQUEST, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(com.google.gwt.http.client.Request request,
              com.google.gwt.http.client.Response response) {
            // nothing
          }
        })//
        .withCallback(new ResourceCallback<QueryResultDto>() {
          @Override
          public void onResource(com.google.gwt.http.client.Response response, QueryResultDto resource) {
            if(response.getStatusCode() == com.google.gwt.http.client.Response.SC_OK) {
              QueryResultDto resultDto = JsonUtils.unsafeEval(response.getText());

              List<VariableSuggestion> suggestions = new ArrayList<VariableSuggestion>();
              for(int i = 0; i < resultDto.getHitsArray().length(); i++) {
                ItemFieldsDto itemDto = (ItemFieldsDto) resultDto.getHitsArray().get(i)
                    .getExtension("Search.ItemFieldsDto.item");

                JsArray<EntryDto> fields = itemDto.getFieldsArray();
                Map<String, String> attributes = new HashMap<String, String>();
                for(int j = 0; j < fields.length(); j++) {
                  if("label-en".equals(fields.get(j).getKey()) ||
                      "label".equals(fields.get(j).getKey()) && !attributes.containsKey("label")) {
                    attributes.put("label", fields.get(j).getValue());
                  } else {
                    attributes.put(fields.get(j).getKey(), fields.get(j).getValue());
                  }
                }

                suggestions.add(convertToFormattedSuggestions(query, attributes));
              }

              // Convert candidates to suggestions.
              Response r = new Response(suggestions);
              callback.onSuggestionsReady(request, r);
            }
          }

          private VariableSuggestion convertToFormattedSuggestions(String query, Map<String, String> attributes) {
            SafeHtmlBuilder accum = new SafeHtmlBuilder();

            accum.appendHtmlConstant("<span class='variable-search-suggest-box'>");
            accum.appendHtmlConstant("<strong>");
            accum.appendEscaped(attributes.get("name"));
            accum.appendHtmlConstant("</strong>");
            accum.appendEscaped(" " + attributes.get("datasource") + "." + attributes.get("table") + "");

            if(attributes.containsKey("label")) {
              accum.appendHtmlConstant("<br>");

              String label = attributes.get("label");
              if(label.length() > LABEL_MAX_SIZE) {
                label = label.substring(0, LABEL_MAX_SIZE) + " ...";
              }
              accum.appendEscaped(label);
            }
            accum.appendHtmlConstant("</span>");

            return createSuggestion(query, accum.toSafeHtml().asString(), attributes.get("datasource"),
                attributes.get("table"), attributes.get("name"));
          }
        })//
        .withCallback(com.google.gwt.http.client.Response.SC_SERVICE_UNAVAILABLE, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(com.google.gwt.http.client.Request request,
              com.google.gwt.http.client.Response response) {
            eventBus.fireEvent(NotificationEvent.newBuilder().warn("SearchServiceUnavailable").build());
          }
        }).send();

  }

  /**
   * Creates the suggestion based on the given replacement and display strings.
   *
   * @param replacementString the string to enter into the SuggestBox's text box
   * if the suggestion is chosen
   * @param displayString the display string
   * @return the suggestion created
   */
  protected VariableSuggestion createSuggestion(String replacementString, String displayString, String datasource,
      String table, String variable) {
    return new VariableSuggestion(replacementString, displayString, datasource, table, variable);
  }

}
