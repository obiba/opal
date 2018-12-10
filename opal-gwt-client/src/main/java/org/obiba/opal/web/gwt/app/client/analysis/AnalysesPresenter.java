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
import java.util.ArrayList;
import org.obiba.opal.web.gwt.app.client.analysis.event.RunAnalysisRequestEvent;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalyseCommandOptionsFactory;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent.Handler;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.AnalyseCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysesDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import javax.inject.Inject;
import java.util.List;

import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_OK;

public class AnalysesPresenter extends PresenterWidget<AnalysesPresenter.Display> implements AnalysesUiHandlers {

  private TableDto originalTable;

  private JsArray<OpalAnalysisDto> originalAnalysisJsArray;

  private final ModalProvider<AnalysisModalPresenter> AnalysisModalPresenterProvider;

  private Runnable deleteAnalysisConfirmation;

  private final TranslationMessages translationMessages;

  private List<PluginPackageDto> plugins;

  @Inject
  public AnalysesPresenter(EventBus eventBus, Display view,
                           TranslationMessages translationMessages,
                           ModalProvider<AnalysisModalPresenter> AnalysisModalPresenter) {
    super(eventBus, view);
    this.translationMessages = translationMessages;
      AnalysisModalPresenterProvider = AnalysisModalPresenter.setContainer(this);
      getView().setUiHandlers(this);
  }

  public void setPlugins(List<PluginPackageDto> plugins) {
    this.plugins = plugins;
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
    initializeEventListeners();
  }

  @Override
  public void createAnalysis() {
    AnalysisModalPresenter modal = AnalysisModalPresenterProvider.get();
    modal.initialize(originalTable, null, existingAnalysisNames(), plugins);
  }

  @Override
  public void onUpdateAnalysesFilter(String filterText) {
    if (originalAnalysisJsArray != null && originalAnalysisJsArray.length() > 0) {
      JsArray<OpalAnalysisDto> array = JsArrays.<OpalAnalysisDto>create();

      if (filterText == null || filterText.length() == 0) {
        array = originalAnalysisJsArray;
      } else {
        for (int i = 0; i < originalAnalysisJsArray.length(); i++) {
          OpalAnalysisDto analysisDto = originalAnalysisJsArray.get(i);

          if (analysisDto.getName().toLowerCase().contains(filterText.toLowerCase())) {
            array.push(analysisDto);
          }
        }
      }

      getView().renderRows(array);
    }
  }

  public void setTable(final TableDto table) {
    getView().clearTable();

    if (originalTable == null || !originalTable.getLink().equals(table.getLink())) {
      originalTable = table;
      originalAnalysisJsArray = null;
    }

    if (originalTable != null) {
      getView().beforeRenderRows();

      ResourceRequestBuilderFactory.<OpalAnalysesDto>newBuilder()
          .forResource(UriBuilders.PROJECT_TABLE_ANALYSES.create()
            .build(originalTable.getDatasourceName(), originalTable.getName()))
          .withCallback(new ResourceCallback<OpalAnalysesDto>() {
            @Override
            public void onResource(Response response, OpalAnalysesDto resource) {
              if (resource != null) {
                originalAnalysisJsArray = resource.getAnalysesArray();
                getView().renderRows(originalAnalysisJsArray);
              }

              getView().afterRenderRows();
            }
          })
          .get().send();
    }
  }

  private void initializeEventListeners() {
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
          case Display.RUN_ANALYSIS:
            runAnalysis(analysis);
            break;

          case Display.DUPLICATE_ANALYSIS:
            OpalAnalysisDto duplicate = OpalAnalysisDto.parse(OpalAnalysisDto.stringify(analysis));
            duplicate.setName(null);
            AnalysisModalPresenterProvider.get().initialize(originalTable, duplicate, existingAnalysisNames(), plugins);
            break;

          case Display.VIEW_ANALYSIS:
            viewAnalysis(analysis);
            break;

          case Display.DELETE_ANALYSIS: {
            deleteAnalysisConfirmation = new DeleteAnalysisRunnable(analysis);
            fireEvent(ConfirmationRequiredEvent.createWithMessages(deleteAnalysisConfirmation, translationMessages.deleteAnalysis(), translationMessages.confirmDeleteAnalysis()));
            break;
          }
        }

      }
    });

    addRegisteredHandler(RunAnalysisRequestEvent.getType(), new RunAnalysisRequestEvent.RunAnalysisRequestHandler() {
      @Override
      public void onRunAnalysisRequest(RunAnalysisRequestEvent event) {
        runAnalysis(event.getAnalysisDto());
      }
    });
  }

  private void viewAnalysis(OpalAnalysisDto analysis) {
    ResourceRequestBuilderFactory.<OpalAnalysisDto>newBuilder()
      .forResource(UriBuilders.PROJECT_TABLE_ANALYSIS.create()
        .build(originalTable.getDatasourceName(), originalTable.getName(), analysis.getName()))
      .withCallback(new ResourceCallback<OpalAnalysisDto>() {
        @Override
        public void onResource(Response response, OpalAnalysisDto resource) {
          if (resource != null) {
            AnalysisModalPresenterProvider.get().initialize(originalTable, resource, new ArrayList<String>(), plugins);
          }
        }
      })
      .get().send();
  }


  private void runAnalysis(OpalAnalysisDto analysisDto) {
    ResourceRequestBuilderFactory
      .newBuilder()
      .withResourceBody(AnalyseCommandOptionsDto.stringify(AnalyseCommandOptionsFactory.create(analysisDto)))
      .forResource(UriBuilders.PROJECT_ANALYSE_COMMAND.create().build(originalTable.getDatasourceName()))
      .post()
      .withCallback(SC_CREATED, new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          fireEvent(NotificationEvent.newBuilder().info("AnalysisTask").build());
        }
      })
      .send();
  }

  private List<String> existingAnalysisNames() {
    List<String> currentNames = new ArrayList<String>();

    for(int i = 0; i < originalAnalysisJsArray.length(); i++) {
      currentNames.add(originalAnalysisJsArray.get(i).getName());
    }

    return currentNames;
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
              .build(originalTable.getDatasourceName(), originalTable.getName(), toDelete.getName()))
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
