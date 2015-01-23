/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.view;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.BaseVariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditor;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;

import static org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableTaxonomyModalPresenter.Display;

/**
 *
 */
public class VariableTaxonomyModalView extends ModalPopupViewWithUiHandlers<VariableAttributeModalUiHandlers>
    implements Display {

  private final Translations translations;

  private List<TaxonomyDto> taxonomies;

  interface Binder extends UiBinder<Widget, VariableTaxonomyModalView> {}

  @UiField
  Modal modal;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  Chooser taxonomyChooser;

  @UiField
  Chooser vocabularyChooser;

  @UiField
  ControlGroup termGroup;

  @UiField
  Panel termPanel;

  private Chooser termChooser;

  @UiField
  ControlGroup valuesGroup;

  @UiField
  LocalizedEditor editor;

  @UiField
  Paragraph editAttributeHelp;

  @Inject
  public VariableTaxonomyModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.addTaxonomyAttribute());
    termChooser = new Chooser();
    termPanel.add(termChooser);
    new ConstrainedModal(modal);
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    Map<String, String> values;
    if(termGroup.isVisible()) {
      values = Maps.newHashMap();
      values.put("", getTermChooserValue());
    } else {
      values = editor.getLocalizedTexts();
    }
    getUiHandlers().save(taxonomyChooser.getSelectedValue(), vocabularyChooser.getSelectedValue(), values);
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @UiHandler("taxonomyChooser")
  public void onTaxonomy(ChosenChangeEvent event) {
    setVocabulary(getTaxonomy(taxonomyChooser.getSelectedValue()));
  }

  @UiHandler("vocabularyChooser")
  public void onVocabulary(ChosenChangeEvent event) {
    setTerm(getVocabulary(taxonomyChooser.getSelectedValue(), vocabularyChooser.getSelectedValue()));
  }

  private TaxonomyDto getTaxonomy(String name) {
    for(TaxonomyDto taxo : taxonomies) {
      if(taxo.getName().equals(name)) return taxo;
    }
    return null;
  }

  private VocabularyDto getVocabulary(String taxoName, String vocName) {
    TaxonomyDto taxo = getTaxonomy(taxoName);
    if(taxo == null) return null;

    for(VocabularyDto voc : JsArrays.toIterable(taxo.getVocabulariesArray())) {
      if(voc.getName().equals(vocName)) return voc;
    }
    return null;
  }

  @Override
  public void setTaxonomies(List<TaxonomyDto> taxonomies) {
    this.taxonomies = taxonomies;
    taxonomyChooser.clear();
    TaxonomyDto firstTaxo = null;
    for(TaxonomyDto taxo : taxonomies) {
      taxonomyChooser.addItem(taxo.getName());
      if(firstTaxo == null) {
        firstTaxo = taxo;
      }
    }
    setVocabulary(firstTaxo);
  }


  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void setDialogMode(BaseVariableAttributeModalPresenter.Mode mode) {
    editAttributeHelp.setVisible(false);
    switch(mode) {
      case APPLY:
        modal.setTitle(translations.applyAttribute());
        editAttributeHelp.setText(translations.applyAttributeHelp());
        editAttributeHelp.setVisible(true);
        break;
      case UPDATE_MULTIPLE:
        valuesGroup.setVisible(false);
        //nameGroup.setVisible(false);
        modal.setTitle(translations.editAttributes());
        editAttributeHelp.setText(translations.editAttributesHelp());
        editAttributeHelp.setVisible(true);
        break;
      case DELETE:
        valuesGroup.setVisible(false);
        termGroup.setVisible(false);
        modal.setTitle(translations.removeAttributes());
        break;
      case UPDATE_SINGLE:
        modal.setTitle(translations.editAttribute());
        break;
    }
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAMESPACE:
          //group = namespaceGroup;
          break;
        case NAME:
          //group = nameGroup;
          break;
        case VALUE:
          group = valuesGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  @Override
  public void setNamespace(String namespace) {
    taxonomyChooser.setSelectedValue(namespace);
    setVocabulary(getTaxonomy(namespace));
  }

  @Override
  public void setName(String name) {
    vocabularyChooser.setSelectedValue(name);
    setTerm(getVocabulary(taxonomyChooser.getSelectedValue(), name));
  }

  @Override
  public void setLocalizedTexts(Map<String, String> localizedTexts, List<String> locales) {
    editor.setLocalizedTexts(localizedTexts, locales);
    if (localizedTexts.keySet().size() == 1 && localizedTexts.keySet().contains("")) {
      String value = localizedTexts.get("");
      if (termChooser.isMultipleSelect()) {
        List<String> values = Lists.newArrayList(value.split(","));
        for(int i = 0; i < termChooser.getItemCount(); i++) {
          termChooser.setItemSelected(i, values.contains(termChooser.getValue(i)));
        }
      } else {
        termChooser.setSelectedValue(value);
      }
    }
  }

  @Override
  public void clearErrors() {
    modal.closeAlerts();
  }

  //
  // Private methods
  //

  private void setVocabulary(TaxonomyDto taxonomy) {
    vocabularyChooser.clear();
    if(taxonomy == null) return;

    VocabularyDto firstVoc = null;
    for(VocabularyDto voc : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
      vocabularyChooser.addItem(voc.getName());
      if(firstVoc == null) {
        firstVoc = voc;
      }
    }
    setTerm(firstVoc);
  }

  private void setTerm(VocabularyDto vocabulary) {
    if(!termGroup.isVisible() && !valuesGroup.isVisible()) return;
    termChooser.clear();
    if(vocabulary == null || vocabulary.getTermsCount() == 0) {
      enableTermSelection(false);
      return;
    }

    enableTermSelection(true);
    boolean repeatable = vocabulary.hasRepeatable() && vocabulary.getRepeatable();
    if (repeatable != termChooser.isMultipleSelect()) {
      termChooser = new Chooser(repeatable);
      if (repeatable) termChooser.setPlaceholderText(translations.selectSomeTerms());
      termPanel.clear();
      termPanel.add(termChooser);
    }
    for(TermDto term : JsArrays.toIterable(vocabulary.getTermsArray())) {
      termChooser.addItem(term.getName());
    }
  }

  private void enableTermSelection(boolean enable) {
    termGroup.setVisible(enable);
    valuesGroup.setVisible(!enable);
  }

  private String getTermChooserValue() {
    String value = "";
    if (termChooser.isMultipleSelect()) {
      for(int i = 0; i < termChooser.getItemCount(); i++) {
        if(termChooser.isItemSelected(i)) {
          value += (value.isEmpty() ? "" : ",") + termChooser.getValue(i);
        }
      }
    } else {
      value = termChooser.getSelectedValue();
    }
    return value;
  }

}
