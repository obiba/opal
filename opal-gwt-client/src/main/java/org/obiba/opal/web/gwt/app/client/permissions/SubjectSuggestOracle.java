/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.Subject;
import org.obiba.opal.web.model.client.opal.SuggestionsDto;

import java.util.ArrayList;
import java.util.List;

public class SubjectSuggestOracle extends SuggestOracle {

  private Subject.SubjectType subjectType;

  private UriBuilder uriBuilder;

  public SubjectSuggestOracle() {
  }

  public void setSubjectType(Subject.SubjectType subjectType) {
    this.subjectType = subjectType;
  }

  @Override
  public void requestSuggestions(final Request request, final Callback callback) {
    final String query = request.getQuery();
    UriBuilder ub = UriBuilder.create().segment("system", "subject-profiles", "_search")
        .query("query", query)
        .query("type", subjectType.getName());

    ResourceRequestBuilderFactory.<SuggestionsDto>newBuilder().forResource(ub.build())
        .withCallback(ResponseCodeCallback.NO_OP, com.google.gwt.http.client.Response.SC_BAD_REQUEST, com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR)
        .withCallback(new ResourceCallback<SuggestionsDto>() {
          @Override
          public void onResource(com.google.gwt.http.client.Response response, SuggestionsDto resource) {
            if (response.getStatusCode() == com.google.gwt.http.client.Response.SC_OK) {
              Response r = new Response(convertToFormattedSuggestions(query, JsArrays.toList(resource.getSuggestionsArray())));
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
