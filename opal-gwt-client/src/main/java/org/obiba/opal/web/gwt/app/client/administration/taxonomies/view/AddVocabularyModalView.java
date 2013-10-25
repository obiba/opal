package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.AddVocabularyModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.AddVocabularyModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.TaxonomiesOrVocabulariesModalPanel;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.Tab;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AddVocabularyModalView extends ModalPopupViewWithUiHandlers<AddVocabularyModalUiHandlers>
    implements AddVocabularyModalPresenter.Display {

  interface AddVocabularyModalViewUiBinder extends UiBinder<Widget, AddVocabularyModalView> {}

  private static final AddVocabularyModalViewUiBinder uiBinder = GWT.create(AddVocabularyModalViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal modal;

  @UiField
  Panel alertPlace;

  @UiField
  ControlGroup labelGroup;

  @UiField
  HasText nameTxt;

  @UiField
  Chooser chooser;

  @UiField
  TabPanel localesTabs;

  @UiField
  CheckBox isRepeatable;

  private List<String> availableLocales;

  private List<TaxonomiesOrVocabulariesModalPanel> panelList;

  private final Map<String, TaxonomyDto> taxonomies = new HashMap<String, TaxonomyDto>();

  private TaxonomyDto taxonomy;

  private VocabularyDto vocabulary;

  private boolean editionMode;

  @Inject
  public AddVocabularyModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.addVocabulary());
  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @Override
  public void setAvailableLocales(List<String> locales) {
    availableLocales = locales;
    if(locales.isEmpty()) {
      localesTabs.clear();
    } else {
      draw();
    }
  }

  @Override
  public void setEditionMode(boolean edit, JsArray<TaxonomyDto> taxonomiesList, TaxonomyDto taxonomy,
      VocabularyDto vocabulary) {
    editionMode = edit;
    this.taxonomy = taxonomy;
    this.vocabulary = vocabulary;
  }

  @Override
  public void setTaxonomies(JsArray<TaxonomyDto> taxonomiesList) {
    for(int i = 0; i < taxonomiesList.length(); i++) {
      taxonomies.put(taxonomiesList.get(i).getName(), taxonomiesList.get(i));
      chooser.addItem(taxonomiesList.get(i).getName());
    }
  }

  @UiHandler("save")
    //TODO
  void onSaveVocabulary(ClickEvent event) {
    TaxonomyDto taxonomyDto;
    taxonomyDto = chooser.getSelectedValue().equals(taxonomy.getName())
        ? taxonomy
        : taxonomies.get(chooser.getSelectedValue());

    VocabularyDto vocabularyDto = VocabularyDto.create();
    vocabularyDto.setName(nameTxt.getText());
    vocabularyDto = setTitleAndDescription(vocabularyDto);
    vocabularyDto.setRepeatable(isRepeatable.getValue());

    if(editionMode) vocabularyDto.setTermsArray(vocabulary.getTermsArray());
    taxonomyDto.getVocabulariesArray().push(vocabularyDto); //TODO gerer la duplication

    if(getUiHandlers().addTaxonomyNewVocabulary(taxonomyDto)) {
      modal.hide();
    }
  }

  private VocabularyDto setTitleAndDescription(VocabularyDto vocabularyDto) {
    JsArray<LocaleTextDto> titles = JsArrays.create();
    JsArray<LocaleTextDto> descriptions = JsArrays.create();

    for(int i = 0; i < availableLocales.size(); i++) {
      if(!panelList.get(i).getTitleTxt().isEmpty()) {
        titles.push(asLocaleTextDto(availableLocales.get(i), panelList.get(i).getTitleTxt()));
      }
      if(!panelList.get(i).getDescriptionTxt().isEmpty()) {
        descriptions.push(asLocaleTextDto(availableLocales.get(i), panelList.get(i).getDescriptionTxt()));
      }
    }
    vocabularyDto.setTitlesArray(titles);
    vocabularyDto.setDescriptionsArray(descriptions);
    return vocabularyDto;
  }

  private LocaleTextDto asLocaleTextDto(String locale, String text) {
    LocaleTextDto dto = LocaleTextDto.create();
    dto.setLocale(locale);
    dto.setText(text);
    return dto;
  }

  @UiHandler("cancel")
  void onCancelAddTaxonomy(ClickEvent event) {
    modal.hide();
  }

  private void draw() {
    if(editionMode) {
      nameTxt.setText(vocabulary.getName());
      isRepeatable.setValue(vocabulary.getRepeatable());
    }
//    taxonomies.setSelectedValue(taxonomy.getName());
    panelList = new ArrayList<TaxonomiesOrVocabulariesModalPanel>(availableLocales.size());
    createLocaleTabs();
  }

  private void createLocaleTabs() {
    for(String locale : availableLocales) {
      Tab tab = new Tab();
      tab.setHeading(locale);
      TaxonomiesOrVocabulariesModalPanel panel = new TaxonomiesOrVocabulariesModalPanel();
      if(editionMode) {
        String title = getText(locale, vocabulary.getTitlesArray());
        String description = getText(locale, vocabulary.getDescriptionsArray());
        if(title != null) {
          panel.setTitleTxt(title);
        }
        if(description != null) {
          panel.setDescriptionTxt(description);
        }
      }
      panelList.add(panel);
      tab.add(panel);
      localesTabs.add(tab);
    }
    modal.add(localesTabs);
  }

  private String getText(String locale, JsArray<LocaleTextDto> array) {
    for(int i = 0; i < array.length(); i++) {
      if(array.get(i).getLocale().equals(locale)) {
        return array.get(i).getText();
      }
    }
    return null;
  }
}