package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class TaxonomyPresenter extends Presenter<TaxonomyPresenter.Display, TaxonomyPresenter.Proxy>
    implements TaxonomyUiHandlers {

  @ProxyStandard
  @NameToken(Places.taxonomy)
  public interface Proxy extends ProxyPlace<TaxonomyPresenter> {}

  public interface Display extends View, HasUiHandlers<TaxonomyUiHandlers> {
    void setTaxonomy(TaxonomyDto taxonomyDto);
  }

  private final ModalProvider<AddTaxonomyModalPresenter> addTaxonomyModalProvider;

  private String name;

  private TaxonomyDto taxonomy;

  @Inject
  public TaxonomyPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<AddTaxonomyModalPresenter> addTaxonomyModalProvider) {
    super(eventBus, display, proxy, ConfigurationPresenter.CONTENT);
    this.addTaxonomyModalProvider = addTaxonomyModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    name = request.getParameter(ParameterTokens.TOKEN_NAME, null);
    refresh();
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder().forResource("/system/conf/taxonomy/" + name).get()
        .withCallback(new ResourceCallback<TaxonomyDto>() {
          @Override
          public void onResource(Response response, TaxonomyDto resource) {
            taxonomy = resource;
            getView().setTaxonomy(taxonomy);
          }
        }).send();
  }

  @Override
  public void showAddVocabulary(TaxonomyDto taxonomyDto) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void showEditTaxonomy() {
    AddTaxonomyModalPresenter presenter = addTaxonomyModalProvider.get();
    presenter.setTaxonomy(taxonomy);
//    presenter.setTaxonomies(taxonomies);
  }

}
