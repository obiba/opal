/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.CategoricalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.DerivationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.VariableDuplicationHelper;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class CodingViewModalPresenter extends PresenterWidget<CodingViewModalPresenter.Display> {

  private final Collection<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private TableDto table;

  private JsArray<VariableDto> variables;

  @Inject
  public CodingViewModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    updateDatasources();
    addEventHandlers();
    addValidators();
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getView().getViewName(), "ViewNameRequired"));
  }

  private void updateDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new DatasourcesCallback()).send();
  }

  @Override
  protected void onUnbind() {
    validators.clear();
  }

  @Override
  public void onReveal() {
    getView().showDialog();
  }

  private void addEventHandlers() {
    registerHandler(getView().addSaveHandler(new CreateCodingViewHandler()));

    registerHandler(getView().addCloseHandler(new CloseHandler<PopupPanel>() {

      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        unbind();
      }
    }));

  }

  //
  // Inner classes and Interfaces
  //

  private final class DatasourcesCallback implements ResourceCallback<JsArray<DatasourceDto>> {
    @Override
    public void onResource(Response response, JsArray<DatasourceDto> resources) {
      JsArray<DatasourceDto> datasources = JsArrays.toSafeArray(resources);
      getView().populateDatasources(datasources);
      getView().getViewName().setText("");
      getView().showProgress(false);
    }
  }

  private class AlreadyExistViewCallBack implements ResourceCallback<ViewDto> {

    @Override
    public void onResource(Response response, ViewDto resource) {
      getView().showProgress(false);
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("ViewAlreadyExists").build());
    }
  }

  private final class CreateCodingViewHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      if(validCodingView()) {
        getView().showProgress(true);
        ResponseCodeCallback createCodingViewCallback = new CreateCodingViewCallBack();
        ResourceCallback<ViewDto> alreadyExistCodingViewCallback = new AlreadyExistViewCallBack();
        UriBuilder uriBuilder = UriBuilder.create();
        uriBuilder.segment("datasource", getView().getDatasourceName(), "view", getView().getViewName().getText());

        ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(uriBuilder.build()).get()
            .withCallback(alreadyExistCodingViewCallback).withCallback(Response.SC_NOT_FOUND, createCodingViewCallback)
            .send();
      }
    }

    private boolean validCodingView() {
      List<String> messages = new ArrayList<String>();
      String message;
      for(FieldValidator validator : validators) {
        message = validator.validate();
        if(message != null) {
          messages.add(message);
        }
      }

      if(!messages.isEmpty()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(messages).build());
        return false;
      } else {
        return true;
      }
    }
  }

  private class CreateCodingViewCallBack implements ResponseCodeCallback {

    private ViewDto getViewDto() {
      ViewDto view = ViewDtoBuilder.newBuilder().setName(getView().getViewName().getText()).fromTables(table)
          .defaultVariableListView().build();
      VariableListViewDto derivedVariables = (VariableListViewDto) view
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);

      for(VariableDto variable : JsArrays.toIterable(JsArrays.toSafeArray(variables))) {
        DerivationHelper derivator = null;
        if(VariableDtos.hasCategories(variable) && ("text".equals(variable.getValueType()) ||
            "integer".equals(variable.getValueType()) && !VariableDtos.allCategoriesMissing(variable))) {
          derivator = new CategoricalVariableDerivationHelper(variable);
          ((CategoricalVariableDerivationHelper) derivator).initializeValueMapEntries();
        } else if(getView().getDuplicate()) {
          derivator = new VariableDuplicationHelper(variable);
        }

        if(derivator != null) {
          derivedVariables.getVariablesArray().push(derivator.getDerivedVariable());
        }
      }

      return view;
    }

    @Override
    public void onResponseCode(Request request, Response response) {

      ViewDto codingView = getViewDto();
      ResponseCodeCallback callbackHandler = new CreatedCodingViewCallBack(codingView);
      UriBuilder uriBuilder = UriBuilder.create();
      uriBuilder.segment("datasource", getView().getDatasourceName(), "views");
      ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).post()
          .withResourceBody(ViewDto.stringify(codingView)).withCallback(Response.SC_CREATED, callbackHandler)
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler)
          .send();
    }
  }

  private class CreatedCodingViewCallBack implements ResponseCodeCallback {

    ViewDto view;

    private CreatedCodingViewCallBack(ViewDto view) {
      this.view = view;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().showProgress(false);
      getView().hideDialog();
      if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));
      } else if(response.getStatusCode() == Response.SC_FORBIDDEN) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("UnauthorizedOperation").build());
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  public interface Display extends View {
    void showDialog();

    void populateDatasources(JsArray<DatasourceDto> datasources);

    void hideDialog();

    HasText getViewName();

    String getDatasourceName();

    boolean getDuplicate();

    HandlerRegistration addSaveHandler(ClickHandler handler);

    HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> closeHandler);

    void showProgress(boolean progress);
  }

}
