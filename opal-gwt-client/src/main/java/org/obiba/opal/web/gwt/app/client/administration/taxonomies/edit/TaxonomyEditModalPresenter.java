package org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
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

  private final TranslationMessages translationMessages;

  private TaxonomyDto originalTaxonomy;

  private RemoveRunnable removeConfirmation;

  private EDIT_MODE mode;

  public enum EDIT_MODE {
    CREATE,
    EDIT
  }

  @Inject
  public TaxonomyEditModalPresenter(EventBus eventBus, Display display, Translations translations,
      TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translations = translations;
    this.translationMessages = translationMessages;
    getView().setUiHandlers(this);
  }

  @Override
  public void onBind() {
    // Register event handlers
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler()));
  }

  @Override
  public void onSaveTaxonomy() {
    final TaxonomyDto dto = TaxonomyDto.create();
    dto.setName(getView().getName().getText());
    dto.setTitleArray(getView().getTitles().getValue());
    dto.setDescriptionArray(getView().getDescriptions().getValue());

    if(mode == EDIT_MODE.EDIT) {
      ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder()
          .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(originalTaxonomy.getName()))//
          .withResourceBody(TaxonomyDto.stringify(dto))//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getView().hide();
              getEventBus().fireEvent(new TaxonomyCreatedEvent(dto.getName()));
            }
          }, Response.SC_OK, Response.SC_CREATED)//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getText() != null && response.getText().length() != 0) {
                fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
              }
            }
          }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR)//
          .put().send();
    } else {
      ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder()
          .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES.create().build())//
          .withResourceBody(TaxonomyDto.stringify(dto))//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getView().hide();
              getEventBus().fireEvent(new TaxonomyCreatedEvent(dto.getName()));
            }
          }, Response.SC_OK, Response.SC_CREATED)//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getText() != null && response.getText().length() != 0) {
                fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
              }
            }
          }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR)//
          .post().send();
    }
  }

  public void initView(final TaxonomyDto taxonomyDto, final EDIT_MODE editionMode) {
    originalTaxonomy = taxonomyDto;
    mode = editionMode;

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
                .setTitle(editionMode == EDIT_MODE.CREATE ? translations.addTaxonomy() : translations.editTaxonomy());
            getView().getName().setText(taxonomyDto.getName());
            getView().getTitles().setValue(taxonomyDto.getTitleArray());
            getView().getDescriptions().setValue(taxonomyDto.getDescriptionArray());
            //getView().setVocabularies(taxonomyDto.getVocabulariesArray());
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

  void showMessage(String id, String message) {
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
              getEventBus().fireEvent(new TaxonomyCreatedEvent(name));
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
