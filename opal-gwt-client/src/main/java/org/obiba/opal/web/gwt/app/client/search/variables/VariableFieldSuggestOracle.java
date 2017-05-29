/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.variables;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import java.util.List;


public class VariableFieldSuggestOracle extends SuggestOracle {

  private List<TaxonomyDto> taxonomies;

  private final List<TermSuggestion> termSuggestions = Lists.newArrayList();

  private final List<PropertySuggestion> propertySuggestions = Lists.newArrayList();

  public VariableFieldSuggestOracle() {
    initPropertySuggestions();
  }

  public void setTaxonomies(List<TaxonomyDto> taxonomies) {
    this.taxonomies = taxonomies;
    termSuggestions.clear();
    for (TaxonomyDto taxonomy : taxonomies) {
      for (VocabularyDto vocabulary : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
        for (TermDto term : JsArrays.toIterable(vocabulary.getTermsArray())) {
          termSuggestions.add(new TermSuggestion(taxonomy, vocabulary, term));
        }
      }
    }
  }

  @Override
  public boolean isDisplayStringHTML() {
    return true;
  }

  @Override
  public void requestSuggestions(Request request, Callback callback) {
    String query = normalizeSearch(request.getQuery());
    List<CandidateSuggestion> candidates = Lists.newArrayList();
    for (TermSuggestion suggestion : termSuggestions) {
      if (suggestion.isCandidate(query)) candidates.add(suggestion);
    }
    for (PropertySuggestion suggestion : propertySuggestions) {
      if (suggestion.isCandidate(query)) candidates.add(suggestion);
    }
    Response response = new Response(candidates);
    callback.onSuggestionsReady(request, response);
  }

  private void initPropertySuggestions() {
    for (String type : new String[] {"integer", "decimal", "text", "boolean", "date", "datetime"}) {
      propertySuggestions.add(new PropertySuggestion("valueType", type));
    }
  }

  private String normalizeSearch(String search) {
    String nSearch = search.trim();
    int idx = nSearch.lastIndexOf(' ');
    if (idx>0) nSearch = nSearch.substring(idx + 1);
    GWT.log(search + " => " + nSearch);
    return nSearch.trim();
  }

  private interface CandidateSuggestion extends Suggestion {
    boolean isCandidate(String query);
  }

  private class PropertySuggestion implements CandidateSuggestion {

    private final String property;

    private final String value;

    private PropertySuggestion(String property) {
      this.property = property;
      this.value = "";
    }

    private PropertySuggestion(String property, String value) {
      this.property = property;
      this.value = value == null ? "" : value;
    }

    @Override
    public String getDisplayString() {
      return getReplacementString();
    }

    @Override
    public String getReplacementString() {
      return property + ":" + value;
    }

    @Override
    public boolean isCandidate(String query) {
      return getReplacementString().toLowerCase().contains(query.toLowerCase());
    }
  }

  private class TermSuggestion implements CandidateSuggestion {

    private final TaxonomyDto taxonomy;

    private final VocabularyDto vocabulary;

    private final TermDto term;

    private TermSuggestion(TaxonomyDto taxonomy, VocabularyDto vocabulary, TermDto term) {
      this.taxonomy = taxonomy;
      this.vocabulary = vocabulary;
      this.term = term;
    }

    @Override
    public String getDisplayString() {
      return getReplacementString();
    }

    @Override
    public String getReplacementString() {
      return taxonomy.getName() + "-" + vocabulary.getName() + ":" + term.getName();
    }

    @Override
    public boolean isCandidate(String query) {
      boolean rval = getReplacementString().toLowerCase().contains(query.toLowerCase());
      // TODO look in title, description, keywords
      if (rval) GWT.log(getReplacementString() + " isCandidate " + query);
      return rval;
    }
  }
}
