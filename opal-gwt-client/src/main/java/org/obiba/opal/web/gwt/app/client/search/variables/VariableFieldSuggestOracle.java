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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import java.util.List;

public class VariableFieldSuggestOracle extends SuggestOracle {

  private final List<TermSuggestion> termSuggestions = Lists.newArrayList();

  private final List<PropertySuggestion> propertySuggestions = Lists.newArrayList();

  private final List<MagmaSuggestion> tablesSuggestions = Lists.newArrayList();


  public VariableFieldSuggestOracle() {
    initPropertySuggestions();
  }

  public void setTaxonomies(List<TaxonomyDto> taxonomies) {
    termSuggestions.clear();
    for (TaxonomyDto taxonomy : taxonomies) {
      for (VocabularyDto vocabulary : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
        for (TermDto term : JsArrays.toIterable(vocabulary.getTermsArray())) {
          termSuggestions.add(new TermSuggestion(taxonomy, vocabulary, term));
        }
      }
    }
  }

  public void setTables(List<TableDto> tables) {
    tablesSuggestions.clear();
    List<String> datasourceNames = Lists.newArrayList();
    for (TableDto table : tables) {
      tablesSuggestions.add(new MagmaSuggestion(table));
      if (!datasourceNames.contains(table.getDatasourceName())) datasourceNames.add(table.getDatasourceName());
    }
    for (String name : datasourceNames) {
      tablesSuggestions.add(new MagmaSuggestion(name));
    }
  }

  @Override
  public boolean isDisplayStringHTML() {
    return true;
  }

  @Override
  public void requestSuggestions(Request request, Callback callback) {
    int limit = request.getLimit();
    String query = normalizeSearch(request.getQuery());
    List<VariableFieldSuggestion> candidates = Lists.newArrayList();
    for (TermSuggestion suggestion : termSuggestions) {
      if (suggestion.isCandidate(query)) candidates.add(suggestion);
    }
    for (MagmaSuggestion suggestion : tablesSuggestions) {
      if (suggestion.isCandidate(query)) candidates.add(suggestion);
    }
    for (PropertySuggestion suggestion : propertySuggestions) {
      if (suggestion.isCandidate(query)) candidates.add(suggestion);
    }
    //GWT.log("=========");
    //for (Suggestion sugg : candidates) {
    //  GWT.log(sugg.getReplacementString());
    //}
    Response response = new Response(candidates);
    response.setMoreSuggestionsCount(Math.max(0, candidates.size() - limit));
    callback.onSuggestionsReady(request, response);
  }

  private void initPropertySuggestions() {
    propertySuggestions.add(new PropertySuggestion("entityType"));
    for (String value : new String[]{"integer", "decimal", "text", "boolean", "date", "datetime"}) {
      propertySuggestions.add(new PropertySuggestion("valueType", value));
    }
    for (String value : new String[]{"true", "false"}) {
      propertySuggestions.add(new PropertySuggestion("repeatable", value));
    }
    for (String value : new String[]{"CATEGORICAL", "CONTINUOUS", "TEMPORAL", "GEO", "BINARY", "UNDETERMINED"}) {
      propertySuggestions.add(new PropertySuggestion("nature", value));
    }
    propertySuggestions.add(new PropertySuggestion("occurrenceGroup"));
    propertySuggestions.add(new PropertySuggestion("referencedEntityType"));
    propertySuggestions.add(new PropertySuggestion("mimeType"));
    propertySuggestions.add(new PropertySuggestion("unit"));
  }

  private String normalizeSearch(String search) {
    String nSearch = search.trim();
    int idx = nSearch.lastIndexOf(' ');
    if (idx > 0) nSearch = nSearch.substring(idx + 1);
    //GWT.log(search + " => " + nSearch);
    return nSearch.trim();
  }

  public interface VariableFieldSuggestion extends Suggestion {
    boolean isCandidate(String query);
  }

  public class PropertySuggestion implements VariableFieldSuggestion {

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

      SafeHtmlBuilder accum = new SafeHtmlBuilder();
      String name = value.isEmpty() ? property : value;
      accum.appendHtmlConstant("<div id='" + getReplacementString() + "'>");
      accum.appendHtmlConstant("  <i class='icon-list'></i>");
      accum.appendHtmlConstant("  <strong>");
      accum.appendEscaped(name);
      accum.appendHtmlConstant("  </strong>");
      accum.appendHtmlConstant("</div>");
      if (!value.isEmpty()) {
        accum.appendHtmlConstant("<div>");
        accum.appendHtmlConstant("  <small>");
        accum.appendEscaped(property);
        accum.appendHtmlConstant("  </small>");
        accum.appendHtmlConstant("</div>");
      }
      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return property + ":" + escape(value);
    }

    @Override
    public boolean isCandidate(String query) {
      return getReplacementString().toLowerCase().contains(query.toLowerCase());
    }

    private String escape(String value) {
      if (value.isEmpty()) return "*";
      return value.contains(" ") ? "\"" + value + "\"" : value;
    }
  }

  public class MagmaSuggestion implements VariableFieldSuggestion {

    private final String datasource;

    private final TableDto table;

    private MagmaSuggestion(TableDto table) {
      this.datasource = table.getDatasourceName();
      this.table = table;
    }

    private MagmaSuggestion(String datasource) {
      this.datasource = datasource;
      this.table = null;
    }

    @Override
    public String getDisplayString() {
      SafeHtmlBuilder accum = new SafeHtmlBuilder();
      String name = table == null ? datasource : table.getName();
      accum.appendHtmlConstant("<div id='" + getReplacementString() + "'>");
      if (table != null) accum.appendHtmlConstant("  <i class='icon-table'></i>");
      else accum.appendHtmlConstant("  <i class='icon-folder-close'></i>");
      accum.appendHtmlConstant("  <strong>");
      accum.appendEscaped(name);
      accum.appendHtmlConstant("  </strong>");
      accum.appendHtmlConstant("</div>");
      if (table != null) {
        accum.appendHtmlConstant("<div>");
        accum.appendHtmlConstant("  <small>");
        accum.appendEscaped(datasource);
        accum.appendHtmlConstant("  </small>");
        accum.appendHtmlConstant("</div>");
      }
      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return table == null ? "datasource:" + escape(datasource) : "table:" + escape(table.getName());
    }

    @Override
    public boolean isCandidate(String query) {
      return getReplacementString().toLowerCase().contains(query.toLowerCase());
    }

    private String escape(String value) {
      return value.contains(" ") ? "\"" + value + "\"" : value;
    }
  }

  public class TermSuggestion implements VariableFieldSuggestion {

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
      SafeHtmlBuilder accum = new SafeHtmlBuilder();
      String name = term.getName();
      accum.appendHtmlConstant("<div id='" + getReplacementString() + "'>");
      accum.appendHtmlConstant("  <i class='icon-tag'></i>");
      accum.appendHtmlConstant("  <strong>");
      accum.appendEscaped(name);
      accum.appendHtmlConstant("  </strong>");
      accum.appendHtmlConstant("</div>");
      accum.appendHtmlConstant("<div>");
      accum.appendHtmlConstant("  <small>");
      accum.appendEscaped(taxonomy.getName() + " - " + vocabulary.getName());
      accum.appendHtmlConstant("  </small>");
      accum.appendHtmlConstant("</div>");
      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return taxonomy.getName() + "-" + vocabulary.getName() + ":" + term.getName();
    }

    @Override
    public boolean isCandidate(String query) {
      boolean rval = getReplacementString().toLowerCase().contains(query.toLowerCase());
      // TODO look in title, description, keywords
      //if (rval) GWT.log(getReplacementString() + " isCandidate " + query);
      return rval;
    }
  }
}
