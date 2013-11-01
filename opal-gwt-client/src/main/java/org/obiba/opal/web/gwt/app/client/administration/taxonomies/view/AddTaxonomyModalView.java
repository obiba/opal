package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.AddTaxonomyModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.AddTaxonomyModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.TaxonomiesOrVocabulariesModalPanel;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

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

public class AddTaxonomyModalView extends ModalPopupViewWithUiHandlers<AddTaxonomyModalUiHandlers>
    implements AddTaxonomyModalPresenter.Display {

  interface AddTaxonomyModalViewUiBinder extends UiBinder<Widget, AddTaxonomyModalView> {}

  private static final AddTaxonomyModalViewUiBinder uiBinder = GWT.create(AddTaxonomyModalViewUiBinder.class);

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
  TabPanel localesTabs;

  private List<String> availableLocales;

  private List<TaxonomiesOrVocabulariesModalPanel> panelList;

  private TaxonomyDto taxonomy;

  private boolean editionMode;

  @Inject
  public AddTaxonomyModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.addTaxonomy());
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
      redraw();
    }
  }

  @Override
  public void setEditionMode(boolean edit, TaxonomyDto taxonomy) {
    editionMode = edit;
    this.taxonomy = taxonomy;
  }

  @UiHandler("save")
  void onSaveTaxonomy(ClickEvent event) {
    TaxonomyDto taxonomyDto = TaxonomyDto.create();
    taxonomyDto.setName(nameTxt.getText());

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

    taxonomyDto.setTitlesArray(JsArrays.toSafeArray(titles));
    taxonomyDto.setDescriptionsArray(descriptions);
    if(editionMode) taxonomyDto.setVocabulariesArray(taxonomy.getVocabulariesArray());
    if(getUiHandlers().addTaxonomy(taxonomyDto)) {
      modal.hide();
    }
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

  private void redraw() {
    if(editionMode) nameTxt.setText(taxonomy.getName());
    panelList = new ArrayList<TaxonomiesOrVocabulariesModalPanel>(availableLocales.size());
    createLocaleTabs();
  }

  private void createLocaleTabs() {
    for(String locale : availableLocales) {
      Tab tab = new Tab();
      tab.setHeading(locale);
      TaxonomiesOrVocabulariesModalPanel panel = new TaxonomiesOrVocabulariesModalPanel();
      if(editionMode) {
        String title = getText(locale, taxonomy.getTitlesArray());
        String description = getText(locale, taxonomy.getDescriptionsArray());
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
