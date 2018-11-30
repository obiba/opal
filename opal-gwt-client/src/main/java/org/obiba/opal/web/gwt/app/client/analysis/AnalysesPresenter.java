package org.obiba.opal.web.gwt.app.client.analysis;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import javax.inject.Inject;
import org.obiba.opal.web.gwt.app.client.analysis.service.PluginService;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent.Handler;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.AnalysisPluginPackageDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysesDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.http.client.Response.SC_OK;

public class AnalysesPresenter extends PresenterWidget<AnalysesPresenter.Display> implements AnalysesUiHandlers {

  private final PluginService analysisService;
  private TableDto originalTable;

  private final ModalProvider<AnalysisEditModalPresenter> AnalysisEditModalPresenterProvider;

  private List<AnalysisPluginPackageDto> plugins = new ArrayList<AnalysisPluginPackageDto>();

  private Runnable deleteAnalysisConfirmation;

  private final TranslationMessages translationMessages;

  @Inject
  public AnalysesPresenter(EventBus eventBus, Display view,
                           TranslationMessages translationMessages,
                           ModalProvider<AnalysisEditModalPresenter> analysisEditModalPresenter,
                           PluginService service) {
    super(eventBus, view);
    this.translationMessages = translationMessages;
      AnalysisEditModalPresenterProvider = analysisEditModalPresenter.setContainer(this);
      analysisService = service;
      getView().setUiHandlers(this);
  }

  public interface Display extends View, HasUiHandlers<AnalysesUiHandlers> {

    String RUN_ANALYSIS = "Run";
    String VIEW_ANALYSIS = "View";
    String DUPLICATE_ANALYSIS = "Duplicate";
    String DELETE_ANALYSIS = "Delete";

    void beforeRenderRows();
    void renderRows(JsArray<OpalAnalysisDto> analyses);
    void afterRenderRows();
    void clearTable();

    HandlerRegistration addRefreshButtonHandler(ClickHandler handler);

    HasActionHandler<OpalAnalysisDto> getActionColumn();
  }

  @Override
  protected void onBind() {
    super.onBind();

    getView().addRefreshButtonHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        setTable(originalTable);
      }
    });

    addRegisteredHandler(ConfirmationEvent.getType(), new Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if (deleteAnalysisConfirmation != null && event.getSource().equals(deleteAnalysisConfirmation) && event.isConfirmed()) {
          deleteAnalysisConfirmation.run();
          deleteAnalysisConfirmation = null;
        }
      }
    });

    getView().getActionColumn().setActionHandler(new ActionHandler<OpalAnalysisDto>() {
      @Override
      public void doAction(OpalAnalysisDto analysis, String actionName) {
        switch (actionName) {
          case Display.VIEW_ANALYSIS:
            AnalysisEditModalPresenter modal = AnalysisEditModalPresenterProvider.get();
            modal.initialize(analysis);
            break;

          case Display.DELETE_ANALYSIS: {
            deleteAnalysisConfirmation = new DeleteAnalysisRunnable(analysis);
            fireEvent(ConfirmationRequiredEvent.createWithMessages(deleteAnalysisConfirmation, translationMessages.deleteAnalysis(), translationMessages.confirmDeleteAnalysis()));
            break;
          }
        }

      }
    });

    fetchAnalysisPlugins();
  }

  @Override
  public void createAnalysis() {
    AnalysisEditModalPresenter modal = AnalysisEditModalPresenterProvider.get();
    modal.initialize(null);
  }

  private void fetchAnalysisPlugins() {
    ResourceRequestBuilderFactory.<PluginPackagesDto>newBuilder()
      .forResource(UriBuilders.PLUGINS_ANALYSIS.create().build())
      .withCallback(new ResourceCallback<PluginPackagesDto>() {


        @Override
        public void onResource(Response response, PluginPackagesDto resource) {
          if (resource != null) {
            analysisService.setPlugins(JsArrays.toList(resource.getPackagesArray()));
          }
        }
      })
      .get().send();
  }

  public void setTable(final TableDto table) {
    getView().clearTable();

    if (originalTable == null || !originalTable.getLink().equals(table.getLink())) {
      originalTable = table;
    }

    if (originalTable != null) {
      getView().beforeRenderRows();

      ResourceRequestBuilderFactory.<OpalAnalysesDto>newBuilder()
          .forResource(UriBuilders.PROJECT_ANALYSES_TABLE.create()
            .build(originalTable.getDatasourceName(), originalTable.getName()))
          .withCallback(new ResourceCallback<OpalAnalysesDto>() {
            @Override
            public void onResource(Response response, OpalAnalysesDto resource) {
              if (resource != null) {
                getView().renderRows(resource.getAnalysesArray());
              }

              getView().afterRenderRows();
            }
          })
          .get().send();
    }
  }

  private class DeleteAnalysisRunnable implements Runnable {

    private OpalAnalysisDto toDelete;

    private DeleteAnalysisRunnable(OpalAnalysisDto toDelete) {
      this.toDelete = toDelete;
    }

    @Override
    public void run() {
      ResourceRequestBuilderFactory.newBuilder()
          .forResource(UriBuilder.create().segment("project", "{}", "table", "{}", "analysis", "{}")
              .build(originalTable.getDatasourceName(), originalTable.getName(), toDelete.getId()))
          .withCallback(SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              setTable(originalTable);
              fireEvent(ConfirmationTerminatedEvent.create());
            }
          }).delete().send();
    }
  }

}
