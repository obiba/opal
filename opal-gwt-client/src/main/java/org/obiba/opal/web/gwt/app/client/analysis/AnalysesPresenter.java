package org.obiba.opal.web.gwt.app.client.analysis;

import static com.google.gwt.http.client.Response.SC_OK;

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
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent.Handler;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysesDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;

public class AnalysesPresenter extends PresenterWidget<AnalysesPresenter.Display> implements AnalysesUiHandlers {

  private TableDto originalTable;

  private Runnable deleteAnalysisConfirmation;

  private final TranslationMessages translationMessages;

  @Inject
  public AnalysesPresenter(EventBus eventBus, Display view,
      TranslationMessages translationMessages) {
    super(eventBus, view);
    this.translationMessages = translationMessages;
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
      public void doAction(OpalAnalysisDto object, String actionName) {
        switch (actionName) {
          case Display.DELETE_ANALYSIS: {
            deleteAnalysisConfirmation = new DeleteAnalysisRunnable(object);
            fireEvent(ConfirmationRequiredEvent.createWithMessages(deleteAnalysisConfirmation, translationMessages.deleteAnalysis(), translationMessages.confirmDeleteAnalysis()));
            break;
          }
        }
      }
    });
  }

  public void setTable(final TableDto table) {
    getView().clearTable();

    if (originalTable == null || !originalTable.getLink().equals(table.getLink())) {
      originalTable = table;
    }

    if (originalTable != null) {
      getView().beforeRenderRows();

      ResourceRequestBuilderFactory.<OpalAnalysesDto>newBuilder()
          .forResource(UriBuilder.create().segment("project", "{}", "table", "{}", "analyses").build(originalTable.getDatasourceName(), originalTable.getName()))
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
