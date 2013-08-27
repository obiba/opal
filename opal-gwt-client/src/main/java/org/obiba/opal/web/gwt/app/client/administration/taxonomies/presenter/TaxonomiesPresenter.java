package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
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
  @NameToken(Places.taxonomies)
  public interface Proxy extends ProxyPlace<TaxonomiesPresenter> {}

  public interface Display extends View, HasUiHandlers<TaxonomiesUiHandlers> {
    void setTaxonomies(JsArray<TaxonomyDto> taxonomies);

    HasWidgets getBreadcrumbs();
  }

  private final PlaceManager placeManager;

  private JsArray<TaxonomyDto> taxonomies;

  private final Translations translations;

  private final ModalProvider<AddTaxonomyModalPresenter> addTaxonomyModalProvider;

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  @Inject
  public TaxonomiesPresenter(Display display, EventBus eventBus, Proxy proxy, PlaceManager placeManager,
      Translations translations, ModalProvider<AddTaxonomyModalPresenter> addTaxonomyModalProvider,
      BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, PageContainerPresenter.CONTENT);
    this.placeManager = placeManager;
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    getView().setUiHandlers(this);
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
    breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
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
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.vocabulary)
        .with(ParameterTokens.TOKEN_NAME, taxonomyDto.getName() + "/" + vocabularyDto.getName()).build();
    placeManager.revealPlace(request);
  }

  @Override
  public void showAddTaxonomy() {
    addTaxonomyModalProvider.get();
  }
}
