/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entity;

import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.identifiers.IdentifierSuggestions;

import java.util.ArrayList;
import java.util.List;

public class EntityIdentifierSuggestOracle extends SuggestOracle {

  private String entityType;

  private List<Suggestion> suggestions;

  public EntityIdentifierSuggestOracle(String entityType) {
    this.entityType = entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  @Override
  public void requestSuggestions(final Request request, final Callback callback) {
    final String query = request.getQuery();
    UriBuilder ub = UriBuilders.DATASOURCES_ENTITIES_SUGGEST.create()
        .query("query", query)
        .query("type", entityType)
        .query("limit", "10");

    ResourceRequestBuilderFactory.<IdentifierSuggestions>newBuilder().forResource(ub.build())
        .withCallback(ResponseCodeCallback.NO_OP, com.google.gwt.http.client.Response.SC_BAD_REQUEST, com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR)
        .withCallback(new ResourceCallback<IdentifierSuggestions>() {
          @Override
          public void onResource(com.google.gwt.http.client.Response response, IdentifierSuggestions resource) {
            if (response.getStatusCode() == com.google.gwt.http.client.Response.SC_OK) {
              suggestions = Lists.newArrayList();
              for (String id : JsArrays.toIterable(resource.getIdentifiersArray())) {

              }
              Response r = new Response(convertToFormattedSuggestions(query, JsArrays.toList(resource.getIdentifiersArray())));
              callback.onSuggestionsReady(request, r);
            }
          }
        })
        .get().send();
  }

  private List<MultiWordSuggestOracle.MultiWordSuggestion> convertToFormattedSuggestions(String query, List<String> candidates) {
    List<MultiWordSuggestOracle.MultiWordSuggestion> suggestions = new ArrayList<MultiWordSuggestOracle.MultiWordSuggestion>();
    for (String candidate : candidates) {
      String html = candidate.replaceAll(query, "<strong>" + query + "</strong>");
      SafeHtmlBuilder display = new SafeHtmlBuilder().appendHtmlConstant(html);
      MultiWordSuggestOracle.MultiWordSuggestion suggestion = new MultiWordSuggestOracle.MultiWordSuggestion(candidate, display.toSafeHtml().asString());
      suggestions.add(suggestion);
    }
    return suggestions;
  }
}
