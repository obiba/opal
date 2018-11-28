package org.obiba.opal.web.gwt.app.client.analysis;

import com.google.gwt.http.client.Response;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import javax.inject.Inject;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysesDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;

public class AnalysesPresenter extends PresenterWidget<AnalysesPresenter.Display> implements AnalysesUiHandlers {

  private TableDto originalTable;

  @Inject
  public AnalysesPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  public interface Display extends View, HasUiHandlers<AnalysesUiHandlers> {

    String RUN_ANALYSIS = "Run";
    String VIEW_ANALYSIS = "View";
    String DUPLICATE_ANALYSIS = "Duplicate";
    String DELETE_ANALYSIS = "Delete";

    void beforeRenderRows();
    void renderRows(OpalAnalysesDto analyses);
    void afterRenderRows();

    HasActionHandler<OpalAnalysisDto> getActionColumn();
  }

  @Override
  protected void onBind() {
    super.onBind();

    getView().getActionColumn().setActionHandler(new ActionHandler<OpalAnalysisDto>() {
      @Override
      public void doAction(OpalAnalysisDto object, String actionName) {

      }
    });
  }

  public void setTable(final TableDto table) {
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
                getView().renderRows(resource);
              }

              getView().afterRenderRows();
            }
          })
          .get().send();
    }
  }

}
