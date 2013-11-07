package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomyEditModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditableText;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class TaxonomyEditModalView extends ModalPopupViewWithUiHandlers<TaxonomyEditModalUiHandlers>
    implements TaxonomyEditModalPresenter.Display {

  private final Map<String, LocalizedEditableText> taxonomyTitleTexts = new HashMap<String, LocalizedEditableText>();

  private final Map<String, LocalizedEditableText> taxonomyDescriptionTexts
      = new HashMap<String, LocalizedEditableText>();

  interface AddTaxonomyModalViewUiBinder extends UiBinder<Widget, TaxonomyEditModalView> {}

  private static final AddTaxonomyModalViewUiBinder uiBinder = GWT.create(AddTaxonomyModalViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal modal;

  @UiField
  TextBox name;

  @UiField
  ControlGroup vocabularyGroup;

  @UiField
  FlowPanel taxonomyTitles;

  @UiField
  FlowPanel taxonomyDescriptions;

  @UiField
  FlowPanel vocabularies;

  @UiField
  TextBox newVocabularyName;

  private JsArrayString availableLocales;

  @Inject
  public TaxonomyEditModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.addTaxonomy());
    newVocabularyName.setPlaceholder(translations.newVocabularyNameLabel());

  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @Override
  public void setAvailableLocales(JsArrayString locales) {
    availableLocales = locales;
  }

  @Override
  public void setTitle(String title) {
    // Set title for Add or Edit
    modal.setTitle(title);
  }

  @UiHandler("save")
  void onSaveTaxonomy(ClickEvent event) {
    getUiHandlers().onSaveTaxonomy();
  }

  @UiHandler("delete")
  void onDeleteTaxonomy(ClickEvent event) {
    getUiHandlers().onDeleteTaxonomy();
  }

  @UiHandler("cancel")
  void onCancelAddTaxonomy(ClickEvent event) {
    modal.hide();
  }

  @UiHandler("addVocabulary")
  void onAddVocabulary(ClickEvent event) {
    getUiHandlers().onAddVocabulary();
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public TakesValue<JsArray<LocaleTextDto>> getTitles() {

    return new LocaleTextDtoTakesValue(taxonomyTitles, availableLocales) {
      @Override
      public Map<String, LocalizedEditableText> getLocalizedEditableTextMap() {
        return taxonomyTitleTexts;
      }
    };

  }

  @Override
  public TakesValue<JsArray<LocaleTextDto>> getDescriptions() {
    return new LocaleTextDtoTakesValue(taxonomyDescriptions, availableLocales) {
      @Override
      public Map<String, LocalizedEditableText> getLocalizedEditableTextMap() {
        return taxonomyDescriptionTexts;
      }
    };
  }

  @Override
  public void setVocabularies(final JsArrayString vocabulariesArray) {
    vocabularies.clear();
    for(int i = 0; i < vocabulariesArray.length(); i++) {
      FlowPanel vocabularyPanel = new FlowPanel();
      Label label = new Label(vocabulariesArray.get(i));
      label.addStyleName("inline-block");
      IconAnchor deleteIcon = new IconAnchor();
      deleteIcon.setIcon(IconType.REMOVE);
      deleteIcon.addStyleName("small-dual-indent");
      final int finalI = i;
      deleteIcon.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          getUiHandlers().onDeleteVocabulary(vocabulariesArray.get(finalI));
        }
      });

      vocabularyPanel.add(label);
      vocabularyPanel.add(deleteIcon);
      vocabularies.add(vocabularyPanel);
    }
  }

  @Override
  public HasText getNewVocabularyName() {
    return newVocabularyName;
  }

  @Override
  public void showError(@Nullable TaxonomyEditModalPresenter.Display.FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case VOCABULARY:
          group = vocabularyGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  private abstract static class LocaleTextDtoTakesValue implements TakesValue<JsArray<LocaleTextDto>> {

    final FlowPanel target;

    final JsArrayString locales;

    LocaleTextDtoTakesValue(FlowPanel target, JsArrayString locales) {
      this.target = target;
      this.locales = locales;
    }

    public abstract Map<String, LocalizedEditableText> getLocalizedEditableTextMap();

    @Override
    public void setValue(JsArray<LocaleTextDto> value) {
      // Add all TexDto to vocabularyTitles
      target.clear();
      int size = value != null ? value.length() : 0;
      int nbLocales = locales.length();
      for(int i = 0; i < nbLocales; i++) {
        // Find the right textDto corresponding with the locale
        boolean found = false;
        for(int j = 0; j < size; j++) {
          if(locales.get(i).equals(value.get(j).getLocale())) {
            LocalizedEditableText textValueInput = getTextValueInput(value.get(j).getLocale(), value.get(j).getText());
            getLocalizedEditableTextMap().put(value.get(j).getLocale(), textValueInput);

            target.add(textValueInput);
            found = true;
            break;
          }
        }

        if(!found) {
          LocalizedEditableText textValueInput = getTextValueInput(locales.get(i), "");
          getLocalizedEditableTextMap().put(locales.get(i), textValueInput);

          target.add(textValueInput);
        }
      }
    }

    @Override
    public JsArray<LocaleTextDto> getValue() {
      JsArray<LocaleTextDto> texts = JsArrays.create();
      for(String locale : getLocalizedEditableTextMap().keySet()) {
        LocaleTextDto localeText = LocaleTextDto.create();
        localeText.setText(getLocalizedEditableTextMap().get(locale).getTextBox().getText());
        localeText.setLocale(locale);

        texts.push(localeText);
      }

      return texts;
    }

    private LocalizedEditableText getTextValueInput(String locale, String text) {
      LocalizedEditableText localizedText = new LocalizedEditableText();
      localizedText.setValue(new LocalizedEditableText.LocalizedText(locale, text));

      return localizedText;
    }
  }

}
