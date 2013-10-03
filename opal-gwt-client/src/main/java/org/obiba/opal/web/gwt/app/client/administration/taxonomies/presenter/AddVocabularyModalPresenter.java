package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.VocabularyDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class AddVocabularyModalPresenter extends ModalPresenterWidget<AddVocabularyModalPresenter.Display>
    implements AddVocabularyModalUiHandlers {

  public interface Display extends PopupView, HasUiHandlers<AddVocabularyModalUiHandlers> {
    void setAvailableLocales(List<String> locales);

    void setEditionMode(boolean edit, JsArray<TaxonomyDto> taxonomiesList, TaxonomyDto taxonomyDto,
        VocabularyDto vocabularyDto);

    void setTaxonomies(JsArray<TaxonomyDto> taxonomiesList);
  }

  private JsArray<TaxonomyDto> taxonomiesList;

  @Inject
  public AddVocabularyModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    setTaxonomies();
    setLocales();
  }

  @Override
  public boolean addTaxonomyNewVocabulary(TaxonomyDto taxonomyDto) {
    ResponseCodeCallback callback = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_CREATED) {
          getEventBus().fireEvent(new TaxonomyCreatedEvent());
        } else if(response.getText() != null && response.getText().length() != 0) {
          ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
          getEventBus().fireEvent(
              NotificationEvent.newBuilder().error("TaxonomyCreationFailed").args(errorDto.getArgumentsArray())
                  .build());
        }
      }
    };
    ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder().forResource("/system/conf/taxonomies").post()//
        .withResourceBody(TaxonomyDto.stringify(taxonomyDto))//
        .withCallback(Response.SC_CREATED, callback).withCallback(Response.SC_BAD_REQUEST, callback)
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback).send();

    return true;
  }

  public void setEditionMode(TaxonomyDto taxonomyDto, VocabularyDto vocabularyDto) {
    getView().setEditionMode(true, taxonomiesList, taxonomyDto, vocabularyDto);
  }

  public void setTaxonomy(TaxonomyDto taxonomyDto) {
    getView().setEditionMode(false, taxonomiesList, taxonomyDto, null);
  }

  private void setTaxonomies() {
    ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder().forResource("/system/conf/taxonomies").get()
        .withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
          @Override
          public void onResource(Response response, JsArray<TaxonomyDto> resource) {
            taxonomiesList = JsArrays.toSafeArray(resource);
            getView().setTaxonomies(taxonomiesList);
          }
        }).send();
  }

  private void setLocales() {
    ResourceRequestBuilderFactory.<GeneralConf>newBuilder().forResource("/system/conf/general").get()
        .withCallback(new ResourceCallback<GeneralConf>() {
          @Override
          public void onResource(Response response, GeneralConf resource) {
            List<String> locales = new ArrayList<String>();
            for(int i = 0; i < resource.getLanguagesArray().length(); i++) {
              locales.add(resource.getLanguages(i));
            }
            getView().setAvailableLocales(locales);
          }
        }).send();
  }
}