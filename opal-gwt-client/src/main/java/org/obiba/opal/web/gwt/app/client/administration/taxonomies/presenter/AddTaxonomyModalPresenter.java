package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.GeneralConf;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class AddTaxonomyModalPresenter extends ModalPresenterWidget<AddTaxonomyModalPresenter.Display>
    implements AddTaxonomyModalUiHandlers {

  public interface Display extends PopupView, HasUiHandlers<AddTaxonomyModalUiHandlers> {
    void setAvailableLocales(List<String> locales);

    void setEditionMode(boolean edit, TaxonomyDto taxonomyDto);
  }

  @Inject
  public AddTaxonomyModalPresenter(EventBus eventBus, Display display, Translations translations) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    setLocales();
  }

  @Override
  public boolean addTaxonomy(TaxonomyDto taxonomyDto) {
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

  public void setEditionMode(TaxonomyDto taxonomy) {
    getView().setEditionMode(true, taxonomy);
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
