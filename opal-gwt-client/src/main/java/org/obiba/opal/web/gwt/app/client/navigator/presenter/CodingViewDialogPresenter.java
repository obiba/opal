/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.CategoricalVariableDerivationHelper;
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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

public class CodingViewDialogPresenter extends WidgetPresenter<CodingViewDialogPresenter.Display> {

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private JsArray<DatasourceDto> datasources;

  private TableDto table;

  private JsArray<VariableDto> variables;

  @Inject
  public CodingViewDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    updateDatasources();
    addEventHandlers();
    addValidators();
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getDisplay().getViewName(), "ViewNameRequired"));
  }

  private void updateDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get()
        .withCallback(new DatasourcesCallback()).send();
  }

  public void setTableVariables(TableDto table, JsArray<VariableDto> variables) {
    this.table = table;
    this.variables = variables;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    validators.clear();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addSaveHandler(new CreateCodingViewHandler()));

    super.registerHandler(getDisplay().addCloseHandler(new CloseHandler<PopupPanel>() {

      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        unbind();
      }
    }));

  }

  private ViewDto getViewDto() {
    ViewDto view =
        ViewDtoBuilder.newBuilder().setName(getDisplay().getViewName().getText()).fromTables(table)
            .defaultVariableListView().build();
    VariableListViewDto derivedVariables =
        (VariableListViewDto) view.getExtension(VariableListViewDto.ViewDtoExtensions.view);

    for(VariableDto variable : JsArrays.toIterable(JsArrays.toSafeArray(variables))) {
      DerivationHelper derivator = null;
      if(VariableDtos.hasCategories(variable)
          && (variable.getValueType().equals("text") || (variable.getValueType().equals("integer") && VariableDtos
              .allCategoriesMissing(variable) == false))) {
        CategoricalVariableDerivationHelper d = new CategoricalVariableDerivationHelper(variable);
        d.initializeValueMapEntries();
        derivator = d;
      } else if(getDisplay().getDuplicate()) {
        derivator = new VariableDuplicationHelper(variable);
      }

      if(derivator != null) {
        derivedVariables.getVariablesArray().push(derivator.getDerivedVariable());
      }
    }

    return view;
  }

  //
  // Inner classes and Interfaces
  //

  private final class DatasourcesCallback implements ResourceCallback<JsArray<DatasourceDto>> {
    @Override
    public void onResource(Response response, JsArray<DatasourceDto> resources) {
      datasources = JsArrays.toSafeArray(resources);
      getDisplay().populateDatasources(datasources);
      getDisplay().getViewName().setText("");
      getDisplay().showProgress(false);
    }
  }

  private class AlreadyExistViewCallBack implements ResourceCallback<ViewDto> {

    @Override
    public void onResource(Response response, ViewDto resource) {
      getDisplay().showProgress(false);
      eventBus.fireEvent(NotificationEvent.newBuilder().error("ViewAlreadyExists").build());
    }

  }

  private final class CreateCodingViewHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      if(validCodingView()) {
        getDisplay().showProgress(true);
        CreateCodingViewCallBack createCodingViewCallback = new CreateCodingViewCallBack();
        AlreadyExistViewCallBack alreadyExistCodingViewCallback = new AlreadyExistViewCallBack();
        UriBuilder uriBuilder = UriBuilder.create();
        uriBuilder
            .segment("datasource", getDisplay().getDatasourceName(), "view", getDisplay().getViewName().getText());

        ResourceRequestBuilderFactory.<ViewDto> newBuilder().forResource(uriBuilder.build()).get()
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

      if(messages.size() > 0) {
        eventBus.fireEvent(NotificationEvent.newBuilder().error(messages).build());
        return false;
      } else {
        return true;
      }
    }
  }

  private class CreateCodingViewCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {

      ViewDto codingView = getViewDto();
      CreatedCodingViewCallBack callbackHandler = new CreatedCodingViewCallBack(codingView);
      UriBuilder uriBuilder = UriBuilder.create();
      uriBuilder.segment("datasource", getDisplay().getDatasourceName(), "views");
      ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).post()
          .withResourceBody(ViewDto.stringify(codingView)).withCallback(Response.SC_CREATED, callbackHandler)
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler)
          .send();
    }
  }

  private class CreatedCodingViewCallBack implements ResponseCodeCallback {

    ViewDto view;

    public CreatedCodingViewCallBack(ViewDto view) {
      this.view = view;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getDisplay().showProgress(false);
      getDisplay().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        eventBus.fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));
      } else if(response.getStatusCode() == Response.SC_FORBIDDEN) {
        eventBus.fireEvent(NotificationEvent.newBuilder().error("UnauthorizedOperation").build());
      } else {
        eventBus.fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  public interface Display extends WidgetDisplay {
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
