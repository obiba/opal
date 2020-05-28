/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.EntryDto;
import org.obiba.opal.web.model.client.search.ItemFieldsDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableSuggestOracle extends SuggestOracle {

  private static final int LABEL_MAX_SIZE = 75;

  private int limit = 20;

  private List<Suggestion> suggestions;

  public interface Identifiable {
    String getId();
  }

  /**
   * Suggestion class for {@link com.google.gwt.user.client.ui.SuggestOracle.Suggestion}.
   */
  public static class VariableSuggestion implements Suggestion, Identifiable, IsSerializable {
    private String id;

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
     * @param table
     * @param variable
     */
    public VariableSuggestion(String replacementString, String displayString, String datasource, String table, String variable) {
      this.replacementString = replacementString;
      this.displayString = displayString;
      this.datasource = datasource;
      this.table = table;
      this.variable = variable;
      id = Joiner.on(".").join(datasource, table, variable);
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

    @Override
    public String getId() {
      return id;
    }
  }

  public static class AdvancedSearchSuggestion implements Suggestion, Identifiable, IsSerializable {

    private int totalHits;

    private int showingCount;

    private String query;

    private String datasource;

    private String table;

    /**
     * Constructor used by RPC.
     */
    @SuppressWarnings("UnusedDeclaration")
    public AdvancedSearchSuggestion() {
    }

    public AdvancedSearchSuggestion(String query, int totalHits, int showingCount, String datasource, String table) {
      this.query = query;
      this.totalHits = totalHits;
      this.showingCount = showingCount;
      this.datasource = datasource;
      this.table = table;
    }

    @Override
    public String getDisplayString() {
      SafeHtmlBuilder accum = new SafeHtmlBuilder();

      accum.appendHtmlConstant("<span class='advanced-search-suggest-box' id='" + getId() + "' style='font-size:smaller'>");
      accum.appendEscaped("Showing " + showingCount + "/" + totalHits);
      accum.appendHtmlConstant("</span>");
      accum.appendHtmlConstant("<span class='label label-info small-indent'>");
      accum.appendEscaped("Advanced search");
      accum.appendHtmlConstant("</span>");


      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return query.replaceAll("[!^~*:/\"\\\\+]", "");
    }

    public int getTotalHits() {
      return totalHits;
    }

    public int getShowingCount() {
      return showingCount;
    }

    public String getDatasource() {
      return datasource;
    }

    public String getTable() {
      return table;
    }

    @Override
    public String getId() {
      return "_advanced";
    }
  }

  protected final EventBus eventBus;

  protected String datasource;

  protected String table;

  protected String originalQuery;

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
   * @param eventBus
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
    requestSuggestions(request, callback);
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public String getOriginalQuery() {
    return originalQuery;
  }

  @Override
  public void requestSuggestions(final Request request, final Callback callback) {
    originalQuery = request.getQuery();
    if(originalQuery == null || originalQuery.trim().isEmpty()) return;
    String prefix = getQueryPrefix();
    final String query;
    if (Strings.isNullOrEmpty(prefix.trim()))
      query = getOriginalQuery();
    else
      query = prefix + " AND (" + getOriginalQuery() + ")";

    UriBuilder ub = UriBuilder.create().segment("datasources", "variables", "_search")//
        .query("query", query)//
        .query("limit", limit + "")//
        .query("order", "name")//
        .query("field", "name", "field", "datasource", "field", "table", "field", "label", "field", "label-en");

    // Get candidates from search words.
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder().forResource(ub.build()).get().withCallback(
        com.google.gwt.http.client.Response.SC_BAD_REQUEST, ResponseCodeCallback.NO_OP)//
        .withCallback(new ResourceCallback<QueryResultDto>() {
          @Override
          public void onResource(com.google.gwt.http.client.Response response, QueryResultDto resource) {
            if(response.getStatusCode() == com.google.gwt.http.client.Response.SC_OK) {
              QueryResultDto resultDto = JsonUtils.unsafeEval(response.getText());

              suggestions = Lists.newArrayList();
              if(resultDto.getHitsArray() != null && resultDto.getHitsArray().length() > 0) {
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
                if (addAdvancedSearchSuggestion())
                  suggestions.add(new AdvancedSearchSuggestion(getOriginalQuery(), resultDto.getTotalHits(), resultDto.getHitsArray().length(), datasource, table));
              }

              // Convert candidates to suggestions.
              Response r = new Response(suggestions);
              callback.onSuggestionsReady(request, r);
            }
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

  protected String getQueryPrefix() {
    String prefix = "";
    if(datasource != null) {
      prefix = "project:" + datasource + " ";
    }
    if(table != null) {
      prefix += "table:" + table + " ";
    }
    return prefix;
  }

  protected boolean addAdvancedSearchSuggestion() {
    return true;
  }

  protected VariableSuggestion convertToFormattedSuggestions(String query, Map<String, String> attributes) {
    SafeHtmlBuilder accum = new SafeHtmlBuilder();

    String prefix = attributes.get("datasource") + "." + attributes.get("table");

    String name = attributes.get("name");
    accum.appendHtmlConstant("<span class='variable-search-suggest-box' id='" + prefix + "." + name + "'>");
    accum.appendHtmlConstant("<strong>");
    accum.appendEscaped(name);
    accum.appendHtmlConstant("</strong>");
    accum.appendHtmlConstant(" <i>");
    accum.appendEscaped(prefix);
    accum.appendHtmlConstant("</i>");

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
        name);
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

  public SuggestOracle.Suggestion getSelectedSuggestion() {
    String activeItem = findActiveItem();
    if(activeItem != null)
      for(Suggestion suggestion : suggestions)
        if (activeItem.equals(((Identifiable)suggestion).getId())) return suggestion;
    activeItem = findAdvancedSearchItemActive();
    if(activeItem != null)
      for(Suggestion suggestion : suggestions)
        if (activeItem.equals(((Identifiable)suggestion).getId())) return suggestion;
    return null;
  }

  /**
   * To deal with issue http://jira.obiba.org/jira/browse/OPAL-2269, we call this function onKeyPress ENTER.
   *
   * @return selected element that is currently highlighted
   */
  private static native String findActiveItem() /*-{
    return $wnd.jQuery('li.active').find('.variable-search-suggest-box').attr('id');
  }-*/;

  private static native String findAdvancedSearchItemActive() /*-{
    return $wnd.jQuery('li.active').find('.advanced-search-suggest-box').attr('id');
  }-*/;

}
