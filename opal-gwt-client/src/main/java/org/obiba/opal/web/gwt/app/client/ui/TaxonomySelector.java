/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.search.variables.VariableFieldSuggestOracle;
import org.obiba.opal.web.gwt.app.client.support.AttributeHelper;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import java.util.List;

public class TaxonomySelector extends Composite {

  interface TaxonomySelectorUiBinder extends UiBinder<Widget, TaxonomySelector> {
  }

  private static final TaxonomySelectorUiBinder uiBinder = GWT.create(TaxonomySelectorUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final String currentLocale = AttributeHelper.getCurrentLanguage();

  @UiField(provided = true)
  Typeahead quickSearch;

  @UiField
  TextBox quickSearchInput;

  @UiField
  Chooser taxonomyChooser;

  @UiField
  HelpBlock taxonomyHelp;

  @UiField
  Chooser vocabularyChooser;

  @UiField
  HelpBlock vocabularyHelp;

  @UiField
  ControlGroup termGroup;

  @UiField
  Chooser termChooser;

  @UiField
  HelpBlock termHelp;

  private List<TaxonomyDto> taxonomies;

  private VariableFieldSuggestOracle oracle = new VariableFieldSuggestOracle();

  public TaxonomySelector() {
    initQuickSearch();
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void setTaxonomies(List<TaxonomyDto> taxonomies) {
    this.taxonomies = taxonomies;
    taxonomyChooser.clear();
    TaxonomyDto firstTaxo = null;
    for (TaxonomyDto taxo : taxonomies) {
      taxonomyChooser.addItem(getTaxonomyTitle(taxo), taxo.getName());
      if (firstTaxo == null) {
        firstTaxo = taxo;
      }
    }
    setTaxonomyHelp(firstTaxo);
    setVocabularies(firstTaxo);
    if (termGroup.isVisible()){
      quickSearchInput.setPlaceholder("Search term...");
      oracle.setTaxonomyTerms(taxonomies);
    }
    else {
      quickSearchInput.setPlaceholder("Search vocabulary...");
      oracle.setTaxonomyVocabularies(taxonomies);
    }
  }

  public void termSelectable(boolean selectable) {
    termGroup.setVisible(selectable);
  }

  public void setBusy(boolean busy) {
    taxonomyChooser.setEnabled(!busy);
    vocabularyChooser.setEnabled(!busy);
    termChooser.setEnabled(!busy);
    quickSearchInput.setEnabled(!busy);
  }

  @UiHandler("taxonomyChooser")
  public void onTaxonomySelection(ChosenChangeEvent event) {
    TaxonomyDto taxonomy = getTaxonomy(taxonomyChooser.getSelectedValue());
    setTaxonomyHelp(taxonomy);
    setVocabularies(taxonomy);
  }

  @UiHandler("vocabularyChooser")
  public void onVocabularySelection(ChosenChangeEvent event) {
    VocabularyDto vocabulary = getVocabulary(taxonomyChooser.getSelectedValue(), vocabularyChooser.getSelectedValue());
    setVocabularyHelp(vocabulary);
    setTerms(vocabulary);
  }

  @UiHandler("termChooser")
  public void onTermSelection(ChosenChangeEvent event) {
    TermDto term = getTerm(taxonomyChooser.getSelectedValue(), vocabularyChooser.getSelectedValue(), termChooser.getSelectedValue());
    setTermHelp(term);
  }

  public String getTaxonomy() {
    return taxonomyChooser.getSelectedValue();
  }

  public String getVocabulary() {
    return vocabularyChooser.getSelectedValue();
  }

  public String getTerm() {
    return termChooser.getSelectedValue();
  }

  //
  // Private methods
  //

  private void initQuickSearch() {
    quickSearch = new Typeahead(oracle);
    quickSearch.setUpdaterCallback(new Typeahead.UpdaterCallback() {
      @Override
      public String onSelection(SuggestOracle.Suggestion selectedSuggestion) {
        VariableFieldSuggestOracle.VariableFieldSuggestion suggestion = (VariableFieldSuggestOracle.VariableFieldSuggestion) selectedSuggestion;
        if (termGroup.isVisible()) {
          VariableFieldSuggestOracle.TermSuggestion termSuggestion = (VariableFieldSuggestOracle.TermSuggestion) suggestion;
          taxonomyChooser.setSelectedValue(termSuggestion.getTaxonomy().getName());
          setTaxonomyHelp(termSuggestion.getTaxonomy());
          setVocabularies(termSuggestion.getTaxonomy());
          vocabularyChooser.setSelectedValue(termSuggestion.getVocabulary().getName());
          setVocabularyHelp(termSuggestion.getVocabulary());
          setTerms(termSuggestion.getVocabulary());
          termChooser.setSelectedValue(termSuggestion.getTerm().getName());
          setTermHelp(termSuggestion.getTerm());
        } else {
          VariableFieldSuggestOracle.VocabularySuggestion vocabularySuggestion = (VariableFieldSuggestOracle.VocabularySuggestion) suggestion;
          taxonomyChooser.setSelectedValue(vocabularySuggestion.getTaxonomy().getName());
          setTaxonomyHelp(vocabularySuggestion.getTaxonomy());
          setVocabularies(vocabularySuggestion.getTaxonomy());
          vocabularyChooser.setSelectedValue(vocabularySuggestion.getVocabulary().getName());
          setVocabularyHelp(vocabularySuggestion.getVocabulary());
        }
        return "";
      }

    });
    quickSearch.setDisplayItemCount(15);
    quickSearch.setMinLength(2);
    quickSearch.setMatcherCallback(new Typeahead.MatcherCallback() {
      @Override
      public boolean compareQueryToItem(String query, String item) {
        return true;
      }
    });
  }

  private String getTaxonomyTitle(TaxonomyDto taxonomy) {
    String title = taxonomy.getTitleCount() == 0 ? taxonomy.getName() : getLocaleText(taxonomy.getTitleArray());
    return Strings.isNullOrEmpty(title) ? taxonomy.getName() : title;
  }

  private String getVocabularyTitle(VocabularyDto vocabulary) {
    String title = vocabulary.getTitleCount() == 0 ? vocabulary.getName() : getLocaleText(vocabulary.getTitleArray());
    return Strings.isNullOrEmpty(title) ? vocabulary.getName() : title;
  }

  private String getTermTitle(TermDto term) {
    String title = term.getTitleCount() == 0 ? term.getName() : getLocaleText(term.getTitleArray());
    return Strings.isNullOrEmpty(title) ? term.getName() : title;
  }

  private void setTaxonomyHelp(TaxonomyDto taxonomy) {
    taxonomyHelp.setText(taxonomy == null || taxonomy.getDescriptionCount() == 0 ? "" : getLocaleText(taxonomy.getDescriptionArray()));
  }

  private void setVocabularyHelp(VocabularyDto vocabulary) {
    vocabularyHelp.setText(vocabulary == null || vocabulary.getDescriptionCount() == 0 ? "" : getLocaleText(vocabulary.getDescriptionArray()));
  }

  private void setTermHelp(TermDto term) {
    termHelp.setText(term == null || term.getDescriptionCount() == 0 ? "" : getLocaleText(term.getDescriptionArray()));
  }

  private void setVocabularies(TaxonomyDto taxonomy) {
    vocabularyChooser.clear();
    if (taxonomy == null) return;

    VocabularyDto firstVoc = null;
    for (VocabularyDto voc : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
      vocabularyChooser.addItem(getVocabularyTitle(voc), voc.getName());
      if (firstVoc == null) firstVoc = voc;
    }
    vocabularyHelp.setText(getLocaleText(firstVoc.getDescriptionArray()));
    setTerms(firstVoc);
  }

  private void setTerms(VocabularyDto vocabulary) {
    termChooser.clear();
    boolean repeatable = vocabulary.hasRepeatable() && vocabulary.getRepeatable();
    // TODO repeatable
    TermDto firstTerm = null;
    for (TermDto term : JsArrays.toIterable(vocabulary.getTermsArray())) {
      termChooser.addItem(getTermTitle(term), term.getName());
      if (firstTerm == null) firstTerm = term;
    }
    setTermHelp(firstTerm);
  }

  private TaxonomyDto getTaxonomy(String name) {
    for (TaxonomyDto taxo : taxonomies)
      if (taxo.getName().equals(name)) return taxo;
    return null;
  }

  private VocabularyDto getVocabulary(String taxoName, String vocName) {
    TaxonomyDto taxo = getTaxonomy(taxoName);
    if (taxo == null) return null;
    for (VocabularyDto voc : JsArrays.toIterable(taxo.getVocabulariesArray()))
      if (voc.getName().equals(vocName)) return voc;
    return null;
  }

  private TermDto getTerm(String taxoName, String vocName, String termName) {
    VocabularyDto voc = getVocabulary(taxoName, vocName);
    if (voc == null) return null;
    for (TermDto term : JsArrays.toIterable(voc.getTermsArray()))
      if (term.getName().equals(termName)) return term;
    return null;
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
