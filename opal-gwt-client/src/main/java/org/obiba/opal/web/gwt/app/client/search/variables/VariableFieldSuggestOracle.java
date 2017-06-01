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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VariableFieldSuggestOracle extends SuggestOracle {

  private final List<TermSuggestion> termSuggestions = Lists.newArrayList();

  private final List<PropertySuggestion> propertySuggestions = Lists.newArrayList();

  private final List<MagmaSuggestion> magmaSuggestions = Lists.newArrayList();


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
    magmaSuggestions.clear();
    Set<String> datasourceNames = Sets.newHashSet();
    Set<String> tableNames = Sets.newHashSet();
    for (TableDto table : tables) {
      datasourceNames.add(table.getDatasourceName());
      tableNames.add(table.getName());
    }
    for (String name : tableNames) {
      magmaSuggestions.add(new TableSuggestion(name, tables));
    }
    for (String name : datasourceNames) {
      magmaSuggestions.add(new DatasourceSuggestion(name, datasourceNames));
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
    for (MagmaSuggestion suggestion : magmaSuggestions) {
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
    propertySuggestions.add(new PropertySuggestion("name"));
    propertySuggestions.add(new PropertySuggestion("entityType"));
    initPropertySuggestions("valueType", new String[]{"integer", "decimal", "text", "boolean", "date", "datetime"});
    initPropertySuggestions("repeatable", new String[]{"true", "false"});
    initPropertySuggestions("nature", new String[]{"CATEGORICAL", "CONTINUOUS", "TEMPORAL", "GEO", "BINARY", "UNDETERMINED"});
    propertySuggestions.add(new PropertySuggestion("occurrenceGroup"));
    propertySuggestions.add(new PropertySuggestion("referencedEntityType"));
    propertySuggestions.add(new PropertySuggestion("mimeType"));
    propertySuggestions.add(new PropertySuggestion("unit"));
    propertySuggestions.add(new PropertySuggestion("script"));
  }

  private void initPropertySuggestions(String property, String[] values) {
    for (String value : values) {
      propertySuggestions.add(new PropertySuggestion(property, value, values));
    }
  }

  private String normalizeSearch(String search) {
    //String nSearch = search.trim();
    //int idx = nSearch.lastIndexOf(' ');
    //if (idx > 0) nSearch = nSearch.substring(idx + 1);
    //GWT.log(search + " => " + nSearch);
    //return nSearch.trim();
    return search.trim();
  }

  public interface VariableFieldSuggestion extends Suggestion {

    /**
     * Field name.
     *
     * @return
     */
    String getField();

    /**
     * Possible values of the field, if any.
     *
     * @return
     */
    List<String> getCategories();

    /**
     * Field statement contains the queried word.
     *
     * @param query
     * @return
     */
    boolean isCandidate(String query);

  }

  public class PropertySuggestion implements VariableFieldSuggestion {

    private final String property;

    private final String value;

    private final List<String> categories = Lists.newArrayList();

    private PropertySuggestion(String property) {
      this.property = property;
      this.value = "";
    }

    private PropertySuggestion(String property, String value, String[] values) {
      this.property = property;
      this.value = value == null ? "" : value;
      for (String val : values) this.categories.add(val);
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
      return getField() + ":" + escape(value);
    }

    @Override
    public boolean isCandidate(String query) {
      return getReplacementString().toLowerCase().contains(query.toLowerCase());
    }

    private String escape(String value) {
      if (value.isEmpty()) return "*";
      return value.replaceAll(" ", "+");
    }

    @Override
    public String getField() {
      return property;
    }

    @Override
    public List<String> getCategories() {
      return categories;
    }
  }

  public abstract class MagmaSuggestion implements VariableFieldSuggestion {

    protected final List<String> categories = Lists.newArrayList();

    @Override
    public boolean isCandidate(String query) {
      //GWT.log("'" + getReplacementString().toLowerCase() + "' '" + escape(query.toLowerCase()) + "'");
      return getReplacementString().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public List<String> getCategories() {
      return categories;
    }

    protected String escape(String value) {
      return value.replaceAll(" ", "+");
    }
  }

  public class DatasourceSuggestion extends MagmaSuggestion implements VariableFieldSuggestion {

    private final String datasource;

    private DatasourceSuggestion(String datasource, Collection<String> datasources) {
      this.datasource = datasource;
      categories.addAll(datasources);
    }

    @Override
    public String getDisplayString() {
      SafeHtmlBuilder accum = new SafeHtmlBuilder();
      accum.appendHtmlConstant("<div id='" + getReplacementString() + "'>");
      accum.appendHtmlConstant("  <i class='icon-folder-close'></i>");
      accum.appendHtmlConstant("  <strong>");
      accum.appendEscaped(datasource);
      accum.appendHtmlConstant("  </strong>");
      accum.appendHtmlConstant("</div>");
      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return getField() + ":" + escape(datasource);
    }

    @Override
    public String getField() {
      return "datasource";
    }

  }

  public class TableSuggestion extends MagmaSuggestion implements VariableFieldSuggestion {

    private final List<String> datasources = Lists.newArrayList();

    private final String table;

    private TableSuggestion(String table, Collection<TableDto> allTables) {
      this.table = table;
      for (TableDto tableDto : allTables) {
        if (tableDto.getName().equals(table)) datasources.add(tableDto.getDatasourceName());
        if (!categories.contains(tableDto.getName())) categories.add(tableDto.getName());
      }
      Collections.sort(categories);
    }

    @Override
    public String getDisplayString() {
      SafeHtmlBuilder accum = new SafeHtmlBuilder();
      accum.appendHtmlConstant("<div id='" + getReplacementString() + "'>");
      accum.appendHtmlConstant("  <i class='icon-table'></i>");
      accum.appendHtmlConstant("  <strong>");
      accum.appendEscaped(table);
      accum.appendHtmlConstant("  </strong>");
      accum.appendHtmlConstant("</div>");
      accum.appendHtmlConstant("<div>");
      accum.appendHtmlConstant("  <small>");
      accum.appendEscaped(Joiner.on(", ").join(datasources));
      accum.appendHtmlConstant("  </small>");
      accum.appendHtmlConstant("</div>");
      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return getField() + ":" + escape(table);
    }

    @Override
    public String getField() {
      return "table";
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
      return getField() + ":" + term.getName();
    }

    @Override
    public boolean isCandidate(String query) {
      return getReplacementString().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public String getField() {
      return taxonomy.getName() + "-" + vocabulary.getName();
    }

    @Override
    public List<String> getCategories() {
      List<String> categories = Lists.newArrayList();
      for (TermDto termDto : JsArrays.toIterable(vocabulary.getTermsArray())) {
        categories.add(termDto.getName());
      }
      return categories;
    }
  }
}
