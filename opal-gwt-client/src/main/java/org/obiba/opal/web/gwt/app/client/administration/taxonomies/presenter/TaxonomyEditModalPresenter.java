package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class TaxonomyEditModalPresenter extends ModalPresenterWidget<TaxonomyEditModalPresenter.Display>
    implements TaxonomyEditModalUiHandlers {

  private final Translations translations;

  private TaxonomyDto originalTaxonomy;

  private RemoveRunnable removeConfirmation;

  @Inject
  public TaxonomyEditModalPresenter(EventBus eventBus, Display display, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    getView().setUiHandlers(this);
//    setLocales();
  }

  @Override
  public void onBind() {
    // Register event handlers
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler()));
  }

  @Override
  public void onSaveTaxonomy() {
    TaxonomyDto dto = TaxonomyDto.create();
    dto.setName(getView().getName().getText());
    dto.setTitlesArray(getView().getTitles().getValue());
    dto.setDescriptionsArray(getView().getDescriptions().getValue());
    dto.setVocabulariesArray(originalTaxonomy.getVocabulariesArray());

    ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(originalTaxonomy.getName()))//
        .withResourceBody(TaxonomyDto.stringify(dto))//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hide();
            getEventBus().fireEvent(new TaxonomyCreatedEvent());
          }
        }, Response.SC_OK, Response.SC_CREATED)//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getText() != null && response.getText().length() != 0) {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
            }
          }
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR)//
        .put().send();
  }

  @Override
  public void onDeleteTaxonomy() {
    removeConfirmation = new RemoveRunnable(originalTaxonomy.getName());

    fireEvent(ConfirmationRequiredEvent
        .createWithMessages(removeConfirmation, translations.confirmationTitleMap().get("removeTaxonomy"),
            TranslationsUtils.replaceArguments(translations.confirmationMessageMap().get("confirmRemoveTaxonomy"),
                originalTaxonomy.getName())));
  }

  @Override
  public void onAddVocabulary() {
    String name = getView().getNewVocabularyName().getText();
    if(uniqueVocabularyName(name)) {
      JsArrayString vocabularies = JsArrays.toSafeArray(originalTaxonomy.getVocabulariesArray());
      vocabularies.push(name);
      originalTaxonomy.setVocabulariesArray(vocabularies);
      refreshVocabularies();
    }
  }

  @Override
  public void onDeleteVocabulary(String vocabularyName) {
    JsArrayString vocabularies = JsArrayString.createArray().cast();
    for(int i = 0; i < originalTaxonomy.getVocabulariesArray().length(); i++) {
      if(!originalTaxonomy.getVocabularies(i).equals(vocabularyName)) {
        vocabularies.push(originalTaxonomy.getVocabularies(i));
      }
    }
    originalTaxonomy.setVocabulariesArray(vocabularies);
    refreshVocabularies();
  }

  private void refreshVocabularies() {
    getView().setVocabularies(originalTaxonomy.getVocabulariesArray());
    getView().getNewVocabularyName().setText("");
  }

  public void initView(final TaxonomyDto taxonomyDto) {
    originalTaxonomy = taxonomyDto;
    ResourceRequestBuilderFactory.<GeneralConf>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_GENERAL.create().build())
        .withCallback(new ResourceCallback<GeneralConf>() {
          @Override
          public void onResource(Response response, GeneralConf resource) {
            JsArrayString locales = JsArrayString.createArray().cast();
            for(int i = 0; i < resource.getLanguagesArray().length(); i++) {
              locales.push(resource.getLanguages(i));
            }
            getView().setAvailableLocales(locales);
            getView()
                .setTitle(taxonomyDto.getName().isEmpty() ? translations.addTaxonomy() : translations.editTaxonomy());
            getView().getName().setText(taxonomyDto.getName());
            getView().getTitles().setValue(taxonomyDto.getTitlesArray());
            getView().getDescriptions().setValue(taxonomyDto.getDescriptionsArray());
            getView().setVocabularies(taxonomyDto.getVocabulariesArray());
          }
        }).get().send();
  }

  private boolean uniqueVocabularyName(String name) {
    for(int i = 0; i < originalTaxonomy.getVocabulariesCount(); i++) {
      if(originalTaxonomy.getVocabularies(i).equals(name)) {
        showMessage("VOCABULARY", translations.userMessageMap().get("VocabularyNameMustBeUnique"));
        return false;
      }
    }
    return true;
  }

  protected void showMessage(String id, String message) {
    getView().showError(Display.FormField.valueOf(id), message);
  }

  public interface Display extends PopupView, HasUiHandlers<TaxonomyEditModalUiHandlers> {

    enum FormField {
      VOCABULARY
    }

    void setAvailableLocales(JsArrayString locales);

    void setTitle(String title);

    TakesValue<JsArray<LocaleTextDto>> getTitles();

    TakesValue<JsArray<LocaleTextDto>> getDescriptions();

    HasText getName();

    void setVocabularies(JsArrayString vocabulariesArray);

    HasText getNewVocabularyName();

    void showError(FormField formField, String message);
  }

  // Remove group/user confirmation event
  private class RemoveRunnable implements Runnable {

    private final String name;

    RemoveRunnable(String name) {
      this.name = name;
    }

    @Override
    public void run() {
      ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(name))//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getView().hide();
              getEventBus().fireEvent(new TaxonomyCreatedEvent());
            }
          }, Response.SC_OK)//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getText() != null && response.getText().length() != 0) {
                ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
                getEventBus().fireEvent(
                    NotificationEvent.newBuilder().error(error.getStatus()).args(error.getArgumentsArray()).build());
              }
            }
          }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR)//
          .delete().send();
    }
  }

  private class RemoveConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      }
    }
  }

}
