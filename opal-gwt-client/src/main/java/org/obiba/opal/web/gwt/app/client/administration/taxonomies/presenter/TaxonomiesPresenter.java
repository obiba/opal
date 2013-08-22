package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class TaxonomiesPresenter extends Presenter<TaxonomiesPresenter.Display, TaxonomiesPresenter.Proxy>
    implements TaxonomiesUiHandlers {

  @ProxyStandard
  @NameToken(Places.taxonomies)
  public interface Proxy extends ProxyPlace<TaxonomiesPresenter> {}

  public interface Display extends View, HasUiHandlers<TaxonomiesUiHandlers> {
    void setTaxonomies(JsArray<TaxonomyDto> taxonomies);
  }

  private final PlaceManager placeManager;

  private JsArray<TaxonomyDto> taxonomies;

  private final Translations translations;

  private final ModalProvider<AddTaxonomyModalPresenter> addTaxonomyModalProvider;

  @Inject
  public TaxonomiesPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager,
      Translations translations, ModalProvider<AddTaxonomyModalPresenter> addTaxonomyModalProvider) {
    super(eventBus, display, proxy, ConfigurationPresenter.CONTENT);
    getView().setUiHandlers(this);
    this.placeManager = placeManager;
    this.translations = translations;
    this.addTaxonomyModalProvider = addTaxonomyModalProvider.setContainer(this);
  }

  @TitleFunction
  public String getTitle() {
    return translations.pageTaxonomiesTitle();
  }

  @Override
  public void onBind() {
    super.onBind();
    registerHandler(getEventBus().addHandler(TaxonomyCreatedEvent.getType(), new TaxonomyCreatedEvent.Handler() {
      @Override
      public void onProjectCreated(TaxonomyCreatedEvent event) {
        refresh();
      }
    }));
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    refresh();
  }

  public void refresh() {
    ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder().forResource("/system/conf/taxonomies").get()
        .withCallback(new ResourceCallback<JsArray<TaxonomyDto>>() {
          @Override
          public void onResource(Response response, JsArray<TaxonomyDto> resource) {
            taxonomies = JsArrays.toSafeArray(resource);
            getView().setTaxonomies(taxonomies);
          }
        }).send();
  }

  @Override
  public void onTaxonomySelection(TaxonomyDto taxonomy) {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.taxonomy)
        .with(ParameterTokens.TOKEN_NAME, taxonomy.getName()).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onVocabularySelection(TaxonomyDto taxonomyDto, TaxonomyDto.VocabularyDto vocabularyDto) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void showAddTaxonomy() {
    AddTaxonomyModalPresenter presenter = addTaxonomyModalProvider.get();
    presenter.setTaxonomies(taxonomies);
  }
}
