package org.obiba.opal.web.gwt.app.client.administration.taxonomies.list;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit.TaxonomyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyDeletedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomySelectedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.VocabularySelectedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.TaxonomyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view.VocabularyPresenter;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.TaxonomiesDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class TaxonomiesPresenter extends PresenterWidget<TaxonomiesPresenter.Display> implements TaxonomiesUiHandlers {

  private final Provider<TaxonomyPresenter> taxonomyPresenterProvider;

  private final Provider<VocabularyPresenter> vocabularyPresenterProvider;

  private final ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public TaxonomiesPresenter(Display display, EventBus eventBus, Provider<TaxonomyPresenter> taxonomyPresenterProvider,
      Provider<VocabularyPresenter> vocabularyPresenterProvider,
      ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.taxonomyPresenterProvider = taxonomyPresenterProvider;
    this.vocabularyPresenterProvider = vocabularyPresenterProvider;
    this.taxonomyEditModalProvider = taxonomyEditModalProvider.setContainer(this);
  }

  @Override
  public void onBind() {
    super.onBind();
    addHandlers();
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

//  @Override
//  public void onTaxonomyEdit(TaxonomyDto taxonomyDto) {
//    taxonomyEditModalProvider.get().initView(taxonomyDto, TaxonomyEditModalPresenter.EDIT_MODE.EDIT);
//  }

  @Override
  public void onAddTaxonomy() {
    taxonomyEditModalProvider.get().initView(TaxonomyDto.create(), TaxonomyEditModalPresenter.EDIT_MODE.CREATE);
  }

  @Override
  public void onImportDefaultTaxonomies() {
    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES_IMPORT_DEFAULT.create().build()).post()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refresh();
          }
        }, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_NOT_FOUND).send();
  }

  private void addHandlers() {
    addRegisteredHandler(TaxonomyCreatedEvent.getType(), new TaxonomyCreatedEvent.TaxonomyCreatedHandler() {
      @Override
      public void onTaxonomyCreated(TaxonomyCreatedEvent event) {
        refresh();
      }
    });

    addRegisteredHandler(TaxonomySelectedEvent.getType(), new TaxonomySelectedEvent.TaxonomySelectedHandler() {

      @Override
      public void onTaxonomySelected(TaxonomySelectedEvent event) {
        String name = event.getTaxonomy();
        TaxonomyPresenter presenter = taxonomyPresenterProvider.get();
        presenter.setTaxonomy(event.getTaxonomy());
        setInSlot(null, presenter);
      }
    });

    addRegisteredHandler(VocabularySelectedEvent.getType(), new VocabularySelectedEvent.VocabularySelectedHandler() {
      @Override
      public void onVocabularySelected(VocabularySelectedEvent event) {
        TaxonomyDto taxonomy = event.getTaxonomy();
        String name = event.getVocabulary();
        VocabularyPresenter presenter = vocabularyPresenterProvider.get();
        presenter.setVocabulary(event.getTaxonomy(), event.getVocabulary());
        setInSlot(null, presenter);
      }
    });

    addRegisteredHandler(TaxonomyDeletedEvent.getType(), new TaxonomyDeletedEvent.TaxonomyDeletedHandler() {
      @Override
      public void onTaxonomyDeleted(TaxonomyDeletedEvent event) {
        refresh();
      }
    });
  }

  public interface Display extends View, HasUiHandlers<TaxonomiesUiHandlers> {

    void setTaxonomies(JsArray<TaxonomiesDto.TaxonomySummaryDto> taxonomies);
  }
}
