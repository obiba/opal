package org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditableText;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
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
  FlowPanel taxonomyTitles;

  @UiField
  FlowPanel taxonomyDescriptions;

  private JsArrayString availableLocales;

  @Inject
  public TaxonomyEditModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.addTaxonomy());
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

  @UiHandler("cancel")
  void onCancelAddTaxonomy(ClickEvent event) {
    modal.hide();
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

      @Override
      protected LocalizedEditableText getTextValueInput(String locale, String text) {
        LocalizedEditableText textWidget = super.getTextValueInput(locale, text);
        textWidget.setLargeText(true);
        return textWidget;
      }
    };
  }

  @Override
  public void showError(@Nullable TaxonomyEditModalPresenter.Display.FormField formField, String message) {
    modal.addAlert(message, AlertType.ERROR);
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

    protected LocalizedEditableText getTextValueInput(String locale, String text) {
      LocalizedEditableText localizedText = new LocalizedEditableText();
      localizedText.setValue(new LocalizedEditableText.LocalizedText(locale, text));
      return localizedText;
    }
  }

}
