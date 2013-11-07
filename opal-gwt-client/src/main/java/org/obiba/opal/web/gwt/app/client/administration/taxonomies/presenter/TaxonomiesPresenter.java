package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasWidgets;
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
  @NameToken(Places.TAXONOMIES)
  public interface Proxy extends ProxyPlace<TaxonomiesPresenter> {}

  private final PlaceManager placeManager;

  private JsArray<TaxonomyDto> taxonomies;

  private final Translations translations;

  private final ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider;

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public TaxonomiesPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager,
      Translations translations, ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider,
      BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.placeManager = placeManager;
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    getView().setUiHandlers(this);
    this.translations = translations;
    this.taxonomyEditModalProvider = taxonomyEditModalProvider.setContainer(this);
    setHistoryTokens();
  }

  @TitleFunction
  public String getTitle() {
    return translations.pageTaxonomiesTitle();
  }

  @Override
  public void onBind() {
    super.onBind();
    registerHandler(
        getEventBus().addHandler(TaxonomyCreatedEvent.getType(), new TaxonomyCreatedEvent.TaxonomyCreatedHandler() {
          @Override
          public void onTaxonomyCreated(TaxonomyCreatedEvent event) {
            refresh();
          }
        }));
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
    refresh();
  }

  void refresh() {
    ResourceRequestBuilderFactory.<JsArray<TaxonomyDto>>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES.create().build()).get()
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
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.TAXONOMIES)
        .with(ParameterTokens.TOKEN_NAME, taxonomy.getName()).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void onTaxonomyEdit(TaxonomyDto taxonomyDto) {
    taxonomyEditModalProvider.get().initView(taxonomyDto);
  }

  @Override
  public void onAddTaxonomy() {
    taxonomyEditModalProvider.get().initView(TaxonomyDto.create());
  }

  @Override
  public void onVocabularySelection(String name, String vocabulary) {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.VOCABULARY)
        .with(TaxonomyTokens.TOKEN_TAXONOMY, name).with(TaxonomyTokens.TOKEN_VOCABULARY, vocabulary).build();
    placeManager.revealRelativePlace(request, 2);
  }

  private void setHistoryTokens() {
    getView().setGeneralConfigHistoryToken(
        placeManager.buildRelativeHistoryToken(new PlaceRequest.Builder().nameToken(Places.SERVER).build(), 1));
  }

  public interface Display extends View, HasUiHandlers<TaxonomiesUiHandlers> {

    void setGeneralConfigHistoryToken(String historyToken);

    void setTaxonomies(JsArray<TaxonomyDto> taxonomies);

    HasWidgets getBreadcrumbs();
  }
}
