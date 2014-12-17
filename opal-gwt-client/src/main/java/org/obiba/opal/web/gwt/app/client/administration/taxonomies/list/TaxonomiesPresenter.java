package org.obiba.opal.web.gwt.app.client.administration.taxonomies.list;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit.TaxonomyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyDeletedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomySelectedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.VocabularyDeletedEvent;
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
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class TaxonomiesPresenter extends PresenterWidget<TaxonomiesPresenter.Display> implements TaxonomiesUiHandlers {

  private final TaxonomyPresenter taxonomyPresenter;

  private final VocabularyPresenter vocabularyPresenter;

  private final ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public TaxonomiesPresenter(Display display, EventBus eventBus, TaxonomyPresenter taxonomyPresenter,
      VocabularyPresenter vocabularyPresenter, ModalProvider<TaxonomyEditModalPresenter> taxonomyEditModalProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.taxonomyPresenter = taxonomyPresenter;
    this.vocabularyPresenter = vocabularyPresenter;
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
    refresh(null);
  }

  /**
   * Refresh the taxonomy list and select the one with the provided name or the first one.
   *
   * @param taxonomy
   */
  void refresh(final String taxonomy) {
    ResourceRequestBuilderFactory.<TaxonomiesDto>newBuilder()
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMIES_SUMMARIES.create().build()).get()
        .withCallback(new ResourceCallback<TaxonomiesDto>() {
          @Override
          public void onResource(Response response, TaxonomiesDto resource) {
            JsArray<TaxonomiesDto.TaxonomySummaryDto> summaries = JsArrays.toSafeArray(resource.getSummariesArray());
            String selection = taxonomy != null ? taxonomy : null;
            getView().setTaxonomies(summaries, selection);
          }
        }).send();
  }

  @Override
  public void onTaxonomySelection(TaxonomiesDto.TaxonomySummaryDto taxonomy) {
    fireEvent(new TaxonomySelectedEvent(taxonomy.getName()));
  }

  @Override
  public void onAddTaxonomy() {
    taxonomyEditModalProvider.get().initView(TaxonomyDto.create());
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
    addRegisteredHandler(TaxonomyUpdatedEvent.getType(), new TaxonomyUpdatedEvent.TaxonomyUpdatedHandler() {

      @Override
      public void onTaxonomyUpdated(TaxonomyUpdatedEvent event) {
        refresh(event.getName());
      }
    });

    addRegisteredHandler(TaxonomySelectedEvent.getType(), new TaxonomySelectedEvent.TaxonomySelectedHandler() {

      @Override
      public void onTaxonomySelected(TaxonomySelectedEvent event) {
        taxonomyPresenter.setTaxonomy(event.getTaxonomy());
        setInSlot(null, taxonomyPresenter);
      }
    });

    addRegisteredHandler(VocabularySelectedEvent.getType(), new VocabularySelectedEvent.VocabularySelectedHandler() {
      @Override
      public void onVocabularySelected(VocabularySelectedEvent event) {
        vocabularyPresenter.setVocabulary(event.getTaxonomy(), event.getVocabulary());
        setInSlot(null, vocabularyPresenter);
      }
    });

    addRegisteredHandler(TaxonomyDeletedEvent.getType(), new TaxonomyDeletedEvent.TaxonomyDeletedHandler() {
      @Override
      public void onTaxonomyDeleted(TaxonomyDeletedEvent event) {
        refresh();
      }
    });

    addRegisteredHandler(VocabularyDeletedEvent.getType(), new VocabularyDeletedEvent.VocabularyDeletedHandler() {
      @Override
      public void onVocabularyDeleted(VocabularyDeletedEvent event) {
        taxonomyPresenter.setTaxonomy(event.getTaxonomy());
        setInSlot(null, taxonomyPresenter);
      }
    });
  }

  public interface Display extends View, HasUiHandlers<TaxonomiesUiHandlers> {

    void setTaxonomies(JsArray<TaxonomiesDto.TaxonomySummaryDto> taxonomies, String selection);
  }
}
