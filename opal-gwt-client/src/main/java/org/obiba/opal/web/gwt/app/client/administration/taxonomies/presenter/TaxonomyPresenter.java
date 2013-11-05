package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

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

public class TaxonomyPresenter extends Presenter<TaxonomyPresenter.Display, TaxonomyPresenter.Proxy>
    implements TaxonomyUiHandlers {

  @ProxyStandard
  @NameToken(Places.TAXONOMY)
  public interface Proxy extends ProxyPlace<TaxonomyPresenter> {}

  public interface Display extends View, HasUiHandlers<TaxonomyUiHandlers> {
    void setTaxonomy(TaxonomyDto taxonomyDto);

    HasWidgets getBreadcrumbs();
  }

  private final PlaceManager placeManager;

  private final ModalProvider<AddTaxonomyModalPresenter> addTaxonomyModalProvider;

  private final ModalProvider<AddVocabularyModalPresenter> addVocabularyModalProvider;

  private final BreadcrumbsBuilder breadcrumbsBuilder;

  private String name;

  private TaxonomyDto taxonomy;

  @Inject
  public TaxonomyPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<AddTaxonomyModalPresenter> addTaxonomyModalProvider, PlaceManager placeManager,
      ModalProvider<AddVocabularyModalPresenter> addVocabularyModalProvider, BreadcrumbsBuilder breadcrumbsBuilder) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    this.placeManager = placeManager;
    this.breadcrumbsBuilder = breadcrumbsBuilder;
    this.addTaxonomyModalProvider = addTaxonomyModalProvider.setContainer(this);
    this.addVocabularyModalProvider = addVocabularyModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @TitleFunction
  public String getTitle() {
    return taxonomy.getName();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    refresh();
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
            breadcrumbsBuilder.setBreadcrumbView(getView().getBreadcrumbs()).build();
          }
        }).send();
  }

  @Override
  public void showAddVocabulary(TaxonomyDto taxonomyDto) {
    AddVocabularyModalPresenter presenter = addVocabularyModalProvider.get();
    presenter.setTaxonomy(taxonomy);
  }

  @Override
  public void showEditTaxonomy() {
    AddTaxonomyModalPresenter presenter = addTaxonomyModalProvider.get();
    presenter.setEditionMode(taxonomy);
  }

  @Override
  public void onVocabularySelection(String taxonomyName, String vocabularyName) {
    PlaceRequest request = new PlaceRequest.Builder().nameToken(Places.VOCABULARY)
        .with(TaxonomyTokens.TOKEN_TAXONOMY, taxonomyName).with(TaxonomyTokens.TOKEN_VOCABULARY, vocabularyName)
        .build();
    placeManager.revealPlace(request);
  }

}
