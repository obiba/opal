/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.wizard.variablestoview.presenter;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.CopyVariablesToViewEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsVariableCopyColumn;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.DerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.VariableDuplicationHelper;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class VariablesToViewPresenter extends PresenterWidget<VariablesToViewPresenter.Display> {

  private static Translations translations = GWT.create(Translations.class);

  private TableDto table;

  private List<VariableDto> variables;

  JsArray<DatasourceDto> datasources;

  @Inject
  public VariablesToViewPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

//  public void revealDisplay() {
//    getView().show();
//  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
  }

  private void addEventHandlers() {
    // Event CopyVariablesToViewEvent
    registerHandler(
        getEventBus().addHandler(CopyVariablesToViewEvent.getType(), new CopyVariablesToViewEventHandler()));

    // Remove action
    getView().getActions().setActionHandler(new ActionHandler<VariableDto>() {
      @Override
      public void doAction(VariableDto object, String actionName) {
        if(actionName.equals(ActionsVariableCopyColumn.REMOVE_ACTION)) {
          getView().removeVariable(object);
        }
      }
    });

    // Save button
    registerHandler(getView().getSaveButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ViewDto view = ViewDtoBuilder.newBuilder().setName(getView().getViewName()).fromTables(table)
            .defaultVariableListView().build();
        VariableListViewDto derivedVariables = (VariableListViewDto) view
            .getExtension(VariableListViewDto.ViewDtoExtensions.view);

        JsArray<VariableDto> variablesDto = JsArrays.create();
        for(VariableDto v : getView().getVariables()) {
          variablesDto.push(v);
        }
        derivedVariables.setVariablesArray(variablesDto);

        //return view;
//        ResponseCodeCallback callbackHandler = new CreatedViewCallBack(view);
        CreateViewCallBack createCodingViewCallback = new CreateViewCallBack(view);
        UpdateExistViewCallBack alreadyExistCodingViewCallback = new UpdateExistViewCallBack(view);
        UriBuilder uriBuilder = UriBuilder.create();
        uriBuilder.segment("datasource", getView().getDatasourceName(), "view", getView().getViewName());

        ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(uriBuilder.build()).get()
            .withCallback(alreadyExistCodingViewCallback)//
            .withCallback(Response.SC_NOT_FOUND, createCodingViewCallback)//
            .send();

//
      }
    }));

    // Cancel button
    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));
  }

  private class CreateViewCallBack implements ResponseCodeCallback {
    ViewDto view;

    private CreateViewCallBack(ViewDto view) {
      this.view = view;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      ResponseCodeCallback callbackHandler = new CreatedViewCallBack(view, translations.addViewSuccess());
      UriBuilder uriBuilder = UriBuilder.create();
      uriBuilder.segment("datasource", getView().getDatasourceName(), "views");
      ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).post()
          .withResourceBody(ViewDto.stringify(view))//
          .withCallback(callbackHandler, Response.SC_OK, Response.SC_CREATED, Response.SC_FORBIDDEN)//
          .send();
    }
  }

  private class UpdateExistViewCallBack implements ResourceCallback<ViewDto> {
    ViewDto view;

    private UpdateExistViewCallBack(ViewDto view) {
      this.view = view;
    }

    @Override
    public void onResource(Response response, ViewDto resource) {
      ResponseCodeCallback callbackHandler = new CreatedViewCallBack(view, translations.updateViewSuccess());
      UriBuilder uriBuilder = UriBuilder.create();
      uriBuilder.segment("datasource", getView().getDatasourceName(), "view", view.getName());
      ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).put()
          .withResourceBody(ViewDto.stringify(view))//
          .withCallback(callbackHandler, Response.SC_OK, Response.SC_CREATED, Response.SC_FORBIDDEN)//
          .send();
    }
  }

  private class CreatedViewCallBack implements ResponseCodeCallback {

    ViewDto view;

    String message;

    private CreatedViewCallBack(ViewDto view, String message) {
      this.view = view;
      this.message = message;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().info(message).build());
        getEventBus().fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));

      } else if(response.getStatusCode() == Response.SC_FORBIDDEN) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("UnauthorizedOperation").build());
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  class CopyVariablesToViewEventHandler implements CopyVariablesToViewEvent.Handler {

    private void refreshDatasources() {
      ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
          .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
            @Override
            public void onResource(Response response, JsArray<DatasourceDto> resource) {
              datasources = JsArrays.toSafeArray(resource);
              for(int i = 0; i < datasources.length(); i++) {
                DatasourceDto d = datasources.get(i);
                d.setTableArray(JsArrays.toSafeArray(d.getTableArray()));
                d.setViewArray(JsArrays.toSafeArray(d.getViewArray()));
              }

              getView().setDatasources(datasources, table.getDatasourceName());
            }
          }).send();
    }

    @Override
    public void onVariableCopy(CopyVariablesToViewEvent event) {
      table = event.getTable();
      variables = event.getSelection();

      refreshDatasources();

      // Prepare the array of variableDto
      JsArray<VariableDto> derivedVariables = JsArrays.create();
      for(VariableDto variable : variables) {
//        DerivationHelper derivator = null;
//        if(VariableDtos.hasCategories(variable) && ("text".equals(variable.getValueType()) ||
//            "integer".equals(variable.getValueType()) && !VariableDtos.allCategoriesMissing(variable))) {
//          CategoricalVariableDerivationHelper d = new CategoricalVariableDerivationHelper(variable);
//          d.initializeValueMapEntries();
//          derivator = d;
//        }
//        else {
        DerivationHelper derivator = new VariableDuplicationHelper(variable);
//        }

//        if(derivator != null) {
        derivedVariables.push(derivator.getDerivedVariable());
//        }

        GWT.log(VariableDto.stringify(derivator.getDerivedVariable()));
      }

      getView().renderRows(derivedVariables);
      getView().showDialog();
    }
  }

  public interface Display extends PopupView {

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    void showDialog();

    void hideDialog();

    void setDatasources(JsArray<DatasourceDto> datasources, String name);

    void renderRows(JsArray<VariableDto> rows);

    ActionsVariableCopyColumn<VariableDto> getActions();

    void removeVariable(VariableDto object);

    String getViewName();

    List<VariableDto> getVariables();

    String getDatasourceName();
//    void setTableSelectionHandler(TableSelectionHandler handler);

  }

  public interface TableSelectionHandler {
    void onTableSelected(String datasource, String table);
  }

}
