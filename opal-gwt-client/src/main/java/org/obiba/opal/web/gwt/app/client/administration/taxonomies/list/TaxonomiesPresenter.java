package org.obiba.opal.web.gwt.app.client.administration.taxonomies.list;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit.TaxonomyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomySelectedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.TaxonomyPresenter;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.TaxonomiesDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class TaxonomiesPresenter extends PresenterWidget<TaxonomiesPresenter.Display> implements TaxonomiesUiHandlers {

  private final TaxonomyPresenter taxonomyPresenter;

  private final ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public TaxonomiesPresenter(Display display, EventBus eventBus,
      TaxonomyPresenter taxonomyPresenter,
      ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.taxonomyPresenter = taxonomyPresenter;
    this.taxonomyEditModalProvider = taxonomyEditModalProvider.setContainer(this);
  }

  @Override
  public void onBind() {
    super.onBind();
    setInSlot(null, taxonomyPresenter);
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
    refresh();
  }

  void refresh() {
    ResourceRequestBuilderFactory.<TaxonomiesDto>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES_SUMMARIES.create().build()).get()
        .withCallback(new ResourceCallback<TaxonomiesDto>() {
          @Override
          public void onResource(Response response, TaxonomiesDto resource) {
            getView().setTaxonomies(JsArrays.toSafeArray(resource.getSummariesArray()));
          }
        }).send();
  }

  @Override
  public void onTaxonomySelection(TaxonomiesDto.TaxonomySummaryDto taxonomy) {
    fireEvent(new TaxonomySelectedEvent(taxonomy.getName()));
  }

  @Override
  public void onTaxonomyEdit(TaxonomyDto taxonomyDto) {
    taxonomyEditModalProvider.get().initView(taxonomyDto, TaxonomyEditModalPresenter.EDIT_MODE.EDIT);
  }

  @Override
  public void onAddTaxonomy() {
    taxonomyEditModalProvider.get().initView(TaxonomyDto.create(), TaxonomyEditModalPresenter.EDIT_MODE.CREATE);
  }

  @Override
  public void onVocabularySelection(String name, String vocabulary) {

  }

  public interface Display extends View, HasUiHandlers<TaxonomiesUiHandlers> {

    void setTaxonomies(JsArray<TaxonomiesDto.TaxonomySummaryDto> taxonomies);
  }
}
