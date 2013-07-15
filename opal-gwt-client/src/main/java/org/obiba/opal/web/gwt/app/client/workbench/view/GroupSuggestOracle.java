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
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.GroupDto;

import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;

public class GroupSuggestOracle extends SuggestOracle {

  private static final int LABEL_MAX_SIZE = 75;

  /**
   * Suggestion class for {@link MultiWordSuggestOracle}.
   */
  public static class GroupSuggestion implements Suggestion, IsSerializable {
    private String displayString;

    private String replacementString;

    private String group;

    /**
     * Constructor used by RPC.
     */
    @SuppressWarnings("UnusedDeclaration")
    public GroupSuggestion() {
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
    public GroupSuggestion(String replacementString, String displayString, String group) {
      this.replacementString = replacementString;
      this.displayString = displayString;
      this.group = group;
    }

    @Override
    public String getDisplayString() {
      return displayString;
    }

    @Override
    public String getReplacementString() {
      return replacementString;
    }

    public String getGroup() {
      return group;
    }
  }

  private Response defaultResponse;

  private final EventBus eventBus;

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
  public GroupSuggestOracle(EventBus eventBus) {
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

  public String getOriginalQuery() {
    return originalQuery;
  }

  @Override
  public void requestSuggestions(final Request request, final Callback callback) {
    originalQuery = request.getQuery();
    if(originalQuery == null || originalQuery.trim().isEmpty()) return;

    final String query = originalQuery;

    UriBuilder ub = UriBuilder.create().segment("groups");

    // Get groups candidates from search words.
    ResourceRequestBuilderFactory.<JsArray<GroupDto>>newBuilder().forResource(ub.build()).get()
        .withCallback(com.google.gwt.http.client.Response.SC_BAD_REQUEST, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(com.google.gwt.http.client.Request request,
              com.google.gwt.http.client.Response response) {
            // nothing
          }
        })//
        .withCallback(new ResourceCallback<JsArray<GroupDto>>() {
          @Override
          public void onResource(com.google.gwt.http.client.Response response, JsArray<GroupDto> resources) {
            if(response.getStatusCode() == com.google.gwt.http.client.Response.SC_OK) {
              List<GroupSuggestion> suggestions = new ArrayList<GroupSuggestion>();
              for(int i = 0; i < resources.length(); i++) {
                if(resources.get(i).getName().toLowerCase().contains(query.toLowerCase())) {
                  suggestions.add(convertToFormattedSuggestions(query, resources.get(i)));
                }
              }

              // Convert candidates to suggestions.
              Response r = new Response(suggestions);
              callback.onSuggestionsReady(request, r);
            }
          }

          private GroupSuggestion convertToFormattedSuggestions(String query, GroupDto group) {
            SafeHtmlBuilder accum = new SafeHtmlBuilder();

            accum.appendHtmlConstant("<span class='variable-search-suggest-box'>");
            accum.appendHtmlConstant("<strong>");
            accum.appendEscaped(group.getName());
            accum.appendHtmlConstant("</strong>");

            return createSuggestion(query, accum.toSafeHtml().asString(), group.getName());
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
  protected GroupSuggestion createSuggestion(String replacementString, String displayString, String group) {
    GroupSuggestion v = new GroupSuggestion(replacementString, displayString, group);

    return v;
  }

}
