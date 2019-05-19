/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.variables;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.AttributeHelper;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import java.util.*;

public class VariableFieldSuggestOracle extends SuggestOracle {

  private final Translations translations = GWT.create(Translations.class);

  private final String currentLocale;

  private final List<VocabularySuggestion> vocabularySuggestions = Lists.newArrayList();

  private final List<TermSuggestion> termSuggestions = Lists.newArrayList();

  private final List<PropertySuggestion> propertySuggestions = Lists.newArrayList();

  private final List<MagmaSuggestion> magmaSuggestions = Lists.newArrayList();

  public VariableFieldSuggestOracle() {
    this.currentLocale = AttributeHelper.getCurrentLanguage();
    initPropertySuggestions();
  }

  public void setTaxonomyTerms(List<TaxonomyDto> taxonomies) {
    termSuggestions.clear();
    for (TaxonomyDto taxonomy : taxonomies) {
      for (VocabularyDto vocabulary : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
        for (TermDto term : JsArrays.toIterable(vocabulary.getTermsArray())) {
          termSuggestions.add(new TermSuggestion(taxonomy, vocabulary, term));
        }
      }
    }
  }

  public void setTaxonomyVocabularies(List<TaxonomyDto> taxonomies) {
    vocabularySuggestions.clear();
    for (TaxonomyDto taxonomy : taxonomies) {
      for (VocabularyDto vocabulary : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
        vocabularySuggestions.add(new VocabularySuggestion(taxonomy, vocabulary));
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
    for (VocabularySuggestion suggestion : vocabularySuggestions) {
      if (suggestion.isCandidate(query)) candidates.add(suggestion);
    }
    for (TermSuggestion suggestion : termSuggestions) {
      if (suggestion.isCandidate(query)) candidates.add(suggestion);
    }
    for (MagmaSuggestion suggestion : magmaSuggestions) {
      if (suggestion.isCandidate(query)) candidates.add(suggestion);
    }
    for (PropertySuggestion suggestion : propertySuggestions) {
      if (suggestion.isCandidate(query)) candidates.add(suggestion);
    }
    Response response = new Response(candidates);
    response.setMoreSuggestionsCount(Math.max(0, candidates.size() - limit));
    callback.onSuggestionsReady(request, response);
  }

  public VariableFieldSuggestion findSuggestion(String fieldName) {
    for (VocabularySuggestion suggestion : vocabularySuggestions) {
      if (suggestion.getField().getName().equals(fieldName)) return suggestion;
    }
    for (TermSuggestion suggestion : termSuggestions) {
      if (suggestion.getField().getName().equals(fieldName)) return suggestion;
    }
    for (MagmaSuggestion suggestion : magmaSuggestions) {
      if (suggestion.getField().getName().equals(fieldName)) return suggestion;
    }
    for (PropertySuggestion suggestion : propertySuggestions) {
      if (suggestion.getField().getName().equals(fieldName)) return suggestion;
    }
    for (PropertySuggestion suggestion : propertySuggestions) {
      if (suggestion.getField().getName().equals(fieldName + ".analyzed")) return suggestion;
    }
    return null;
  }

  //
  // Private methods
  //

  /**
   * Query string is split in multiple words, all matches is expected, minus prefix inverse the condition.
   *
   * @param ref
   * @param query
   * @return
   */
  private static boolean isCandidate(String ref, String query) {
    return FilterHelper.matches(ref, FilterHelper.tokenize(query));
  }

  private void initPropertySuggestions() {
    propertySuggestions.add(new PropertySuggestion(translations.nameLabel(), "name.analyzed"));
    propertySuggestions.add(new PropertySuggestion(translations.entityTypeLabel(), "entityType"));
    initPropertySuggestions(translations.valueTypeLabel(), "valueType", new String[]{"integer", "decimal", "text", "boolean", "date", "datetime"});
    initPropertySuggestions(translations.repeatableLabel(), "repeatable", new String[]{"true", "false"});
    initPropertySuggestions(translations.natureLabel(), "nature", new String[]{"CATEGORICAL", "CONTINUOUS", "TEMPORAL", "GEO", "BINARY", "UNDETERMINED"});
    propertySuggestions.add(new PropertySuggestion(translations.occurrenceGroupLabel(), "occurrenceGroup"));
    propertySuggestions.add(new PropertySuggestion(translations.referencedEntityTypeLabel(), "referencedEntityType"));
    propertySuggestions.add(new PropertySuggestion(translations. mimeTypeLabel(), "mimeType"));
    propertySuggestions.add(new PropertySuggestion(translations.unitLabel(),"unit"));
    propertySuggestions.add(new PropertySuggestion(translations.scriptLabel(), "script"));
  }

  private void initPropertySuggestions(String title, String property, String[] values) {
    for (String value : values) {
      propertySuggestions.add(new PropertySuggestion(title, property, value, values));
    }
  }

  private String normalizeSearch(String search) {
    return search.trim();
  }

  public class FieldItem {

    private final String name;

    private final String title;

    private final String description;

    public FieldItem(String name) {
      this(name, "", "");
    }

    public FieldItem(String name, String title) {
      this(name, title, "");
    }

    public FieldItem(String name, String title, String description) {
      this.name = name;
      this.title = Strings.isNullOrEmpty(title) ? name : title;
      this.description = this.title.equals(description) ? "" : description;
    }

    public String getName() {
      return name;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }
  }

  //
  // Suggestion classes
  //

  public interface VariableFieldSuggestion extends Suggestion {

    /**
     * Field name.
     *
     * @return
     */
    FieldItem getField();

    /**
     * Possible values of the field, if any.
     *
     * @return
     */
    List<FieldItem> getFieldTerms();

    /**
     * Field statement contains the queried word.
     *
     * @param query
     * @return
     */
    boolean isCandidate(String query);

  }

  public class PropertySuggestion implements VariableFieldSuggestion {

    private final String title;

    private final String property;

    private final String value;

    private final List<FieldItem> fieldTerms = Lists.newArrayList();

    private PropertySuggestion(String title, String property) {
      this.title = title;
      this.property = property;
      this.value = "";
    }

    private PropertySuggestion(String title, String property, String value, String[] values) {
      this.title = title;
      this.property = property;
      this.value = value == null ? "" : value;
      for (String val : values) this.fieldTerms.add(new FieldItem(val));
    }

    @Override
    public String getDisplayString() {

      SafeHtmlBuilder accum = new SafeHtmlBuilder();
      String name = value.isEmpty() ? getField().getTitle() : value;
      accum.appendHtmlConstant("<div id='" + getReplacementString() + "'>");
      accum.appendHtmlConstant("  <i class='icon-list'></i>");
      accum.appendHtmlConstant("  <strong>");
      accum.appendEscaped(name);
      accum.appendHtmlConstant("  </strong>");
      accum.appendHtmlConstant("</div>");
      if (!value.isEmpty()) {
        accum.appendHtmlConstant("<div>");
        accum.appendHtmlConstant("  <small>");
        accum.appendEscaped(getField().getTitle());
        accum.appendHtmlConstant("  </small>");
        accum.appendHtmlConstant("</div>");
      }
      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return getField().getName() + ":" + escape(value);
    }

    @Override
    public boolean isCandidate(String query) {
      return VariableFieldSuggestOracle.isCandidate(getReplacementString(), query);
    }

    private String escape(String value) {
      if (value.isEmpty()) return "*";
      return value.replaceAll(" ", "+");
    }

    @Override
    public FieldItem getField() {
      return new FieldItem(property, title);
    }

    @Override
    public List<FieldItem> getFieldTerms() {
      return fieldTerms;
    }
  }

  public abstract class MagmaSuggestion implements VariableFieldSuggestion {

    protected final List<FieldItem> fieldTerms = Lists.newArrayList();

    @Override
    public boolean isCandidate(String query) {
      return VariableFieldSuggestOracle.isCandidate(getReplacementString(), query);
    }

    @Override
    public List<FieldItem> getFieldTerms() {
      return fieldTerms;
    }

    protected String escape(String value) {
      return value.replaceAll(" ", "+");
    }
  }

  public class DatasourceSuggestion extends MagmaSuggestion implements VariableFieldSuggestion {

    private final String datasource;

    private DatasourceSuggestion(String datasource, Collection<String> datasources) {
      this.datasource = datasource;
      for (String ds : datasources) fieldTerms.add(new FieldItem(ds));
    }

    @Override
    public String getDisplayString() {
      SafeHtmlBuilder accum = new SafeHtmlBuilder();
      accum.appendHtmlConstant("<div id='" + getReplacementString() + "'>");
      accum.appendHtmlConstant("  <i class='icon-briefcase'></i>");
      accum.appendHtmlConstant("  <strong>");
      accum.appendEscaped(datasource);
      accum.appendHtmlConstant("  </strong>");
      accum.appendHtmlConstant("</div>");
      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return getField().getName() + ":" + escape(datasource);
    }

    @Override
    public FieldItem getField() {
      return new FieldItem("project", translations.projectLabel());
    }
  }

  public class TableSuggestion extends MagmaSuggestion implements VariableFieldSuggestion {

    private final List<String> datasources = Lists.newArrayList();

    private final String table;

    private TableSuggestion(String table, Collection<TableDto> allTables) {
      this.table = table;
      List<String> tableNames = Lists.newArrayList();
      for (TableDto tableDto : allTables) {
        if (tableDto.getName().equals(table)) datasources.add(tableDto.getDatasourceName());
        if (!tableNames.contains(tableDto.getName())) {
          fieldTerms.add(new FieldItem(tableDto.getName()));
          tableNames.add(tableDto.getName());
        }
      }
      Collections.sort(fieldTerms, new Comparator<FieldItem>() {
        @Override
        public int compare(FieldItem o1, FieldItem o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
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
      return getField().getName() + ":" + escape(table);
    }

    @Override
    public FieldItem getField() {
      return new FieldItem("table", translations.tableLabel());
    }
  }


  public class VocabularySuggestion implements VariableFieldSuggestion {

    private final TaxonomyDto taxonomy;

    private final VocabularyDto vocabulary;

    private String stringToMatch;

    private VocabularySuggestion(TaxonomyDto taxonomy, VocabularyDto vocabulary) {
      this.taxonomy = taxonomy;
      this.vocabulary = vocabulary;
    }

    public TaxonomyDto getTaxonomy() {
      return taxonomy;
    }

    public VocabularyDto getVocabulary() {
      return vocabulary;
    }

    @Override
    public String getDisplayString() {
      SafeHtmlBuilder accum = new SafeHtmlBuilder();
      String name = getLocaleText(vocabulary.getTitleArray());
      accum.appendHtmlConstant("<div id='" + getReplacementString() + "' title=\"" + getLocaleText(vocabulary.getDescriptionArray()).replaceAll("\"","'") + "\">");
      accum.appendHtmlConstant("  <i class='icon-tag'></i>");
      accum.appendHtmlConstant("  <strong>");
      accum.appendEscaped(name);
      accum.appendHtmlConstant("  </strong>");
      accum.appendHtmlConstant("</div>");
      accum.appendHtmlConstant("<div>");
      accum.appendHtmlConstant("  <small>");
      accum.appendEscaped(getLocaleText(taxonomy.getTitleArray()));
      accum.appendHtmlConstant("  </small>");
      accum.appendHtmlConstant("</div>");
      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return getField().getName() + ":*";
    }

    @Override
    public boolean isCandidate(String query) {
      if (Strings.isNullOrEmpty(stringToMatch)) {
        stringToMatch = Joiner.on(" ").join(Lists.newArrayList(getReplacementString(),
            getLocaleText(vocabulary.getTitleArray()), getLocaleText(vocabulary.getDescriptionArray()),
            getLocaleText(vocabulary.getKeywordsArray()), getLocaleText(taxonomy.getTitleArray())));
      }
      return VariableFieldSuggestOracle.isCandidate(stringToMatch, query);
    }

    @Override
    public FieldItem getField() {
      return new FieldItem(taxonomy.getName() + "-" + vocabulary.getName(),
          getLocaleText(vocabulary.getTitleArray()), getLocaleText(vocabulary.getDescriptionArray()));
    }

    @Override
    public List<FieldItem> getFieldTerms() {
      if (vocabulary.getTermsCount() == 0) return Lists.newArrayList();
      List<FieldItem> fieldTerms = Lists.newArrayList();
      for (TermDto termDto : JsArrays.toIterable(vocabulary.getTermsArray())) {
        fieldTerms.add(new FieldItem(termDto.getName(), getLocaleText(termDto.getTitleArray()),
            getLocaleText(termDto.getDescriptionArray())));
      }
      return fieldTerms;
    }

    private String getLocaleText(JsArray<LocaleTextDto> texts) {
      for (LocaleTextDto text : JsArrays.toIterable(texts)) {
        if (currentLocale.equals(text.getLocale())) return text.getText();
      }
      // fallback in english
      for (LocaleTextDto text : JsArrays.toIterable(texts)) {
        if ("en".equals(text.getLocale())) return text.getText();
      }
      return "";
    }
  }

  public class TermSuggestion implements VariableFieldSuggestion {

    private final TaxonomyDto taxonomy;

    private final VocabularyDto vocabulary;

    private final TermDto term;

    private String stringToMatch;

    private TermSuggestion(TaxonomyDto taxonomy, VocabularyDto vocabulary, TermDto term) {
      this.taxonomy = taxonomy;
      this.vocabulary = vocabulary;
      this.term = term;
    }

    public TaxonomyDto getTaxonomy() {
      return taxonomy;
    }

    public VocabularyDto getVocabulary() {
      return vocabulary;
    }

    public TermDto getTerm() {
      return term;
    }

    @Override
    public String getDisplayString() {
      SafeHtmlBuilder accum = new SafeHtmlBuilder();
      String name = getLocaleText(term.getTitleArray());
      accum.appendHtmlConstant("<div id='" + getReplacementString() + "' title=\"" + getLocaleText(term.getDescriptionArray()).replaceAll("\"","'") + "\">");
      accum.appendHtmlConstant("  <i class='icon-tag'></i>");
      accum.appendHtmlConstant("  <strong>");
      accum.appendEscaped(name);
      accum.appendHtmlConstant("  </strong>");
      accum.appendHtmlConstant("</div>");
      accum.appendHtmlConstant("<div>");
      accum.appendHtmlConstant("  <small>");
      accum.appendEscaped(getLocaleText(taxonomy.getTitleArray()) + " - " + getLocaleText(vocabulary.getTitleArray()));
      accum.appendHtmlConstant("  </small>");
      accum.appendHtmlConstant("</div>");
      return accum.toSafeHtml().asString();
    }

    @Override
    public String getReplacementString() {
      return getField().getName() + ":" + term.getName();
    }

    @Override
    public boolean isCandidate(String query) {
      if (Strings.isNullOrEmpty(stringToMatch)) {
        stringToMatch = Joiner.on(" ").join(Lists.newArrayList(getReplacementString(),
            getLocaleText(term.getTitleArray()), getLocaleText(term.getDescriptionArray()),
            getLocaleText(term.getKeywordsArray()), getLocaleText(vocabulary.getTitleArray()), getLocaleText(taxonomy.getTitleArray())));
      }
      return VariableFieldSuggestOracle.isCandidate(stringToMatch, query);
    }

    @Override
    public FieldItem getField() {
      return new FieldItem(taxonomy.getName() + "-" + vocabulary.getName(),
          getLocaleText(vocabulary.getTitleArray()), getLocaleText(vocabulary.getDescriptionArray()));
    }

    @Override
    public List<FieldItem> getFieldTerms() {
      List<FieldItem> fieldTerms = Lists.newArrayList();
      for (TermDto termDto : JsArrays.toIterable(vocabulary.getTermsArray())) {
        fieldTerms.add(new FieldItem(termDto.getName(), getLocaleText(termDto.getTitleArray()),
            getLocaleText(termDto.getDescriptionArray())));
      }
      return fieldTerms;
    }

    private String getLocaleText(JsArray<LocaleTextDto> texts) {
      for (LocaleTextDto text : JsArrays.toIterable(texts)) {
        if (currentLocale.equals(text.getLocale())) return text.getText();
      }
      // fallback in english
      for (LocaleTextDto text : JsArrays.toIterable(texts)) {
        if ("en".equals(text.getLocale())) return text.getText();
      }
      return "";
    }
  }

}
