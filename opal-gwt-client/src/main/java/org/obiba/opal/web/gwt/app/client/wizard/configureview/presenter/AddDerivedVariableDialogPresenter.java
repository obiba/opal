/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidatableWidgetPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.VariableAddRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallbacks;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;

public class AddDerivedVariableDialogPresenter extends ValidatableWidgetPresenter<AddDerivedVariableDialogPresenter.Display> {

  public interface Display extends PopupView {

    void hideDialog();

    void addVariableSuggestion(String suggestion);

    void clearVariableSuggestions();

    HasText getVariableName();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addAddVariableClickHandler(ClickHandler handler);
  }

  @Inject
  public AddDerivedVariableDialogPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    addHandlers();
    addValidators();
  }

  private void addValidators() {
    addValidator(new RequiredTextValidator(getView().getVariableName(), "CopyFromVariableNameIsRequired"));
  }

  private void addHandlers() {
    super.registerHandler(getView().addAddVariableClickHandler(new AddVariableClickHandler()));
    super.registerHandler(getView().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getEventBus().addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredHandler()));
  }

  private String getVariableName() {
    return getView().getVariableName().getText();
  }

  void refreshVariableNameSuggestions(ViewDto viewDto) {
    getView().clearVariableSuggestions();

    // Add the derived variables to the suggestions.
    VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
    for(VariableDto variable : JsArrays.toList(variableListDto.getVariablesArray())) {
      getView().addVariableSuggestion(variable.getName());
    }

    VariablesDtoCallBack variablesDtoCallBack = new VariablesDtoCallBack();
    // Add the variables to the suggestions.
    String[] tableNameParts;
    for(int i = 0; i < viewDto.getFromArray().length(); i++) {
      tableNameParts = viewDto.getFromArray().get(i).split("\\.");
      ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder()//
      .forResource("/datasource/" + tableNameParts[0] + "/table/" + tableNameParts[1] + "/variables")//
      .get()//
      .withCallback(404, ResponseCodeCallbacks.noOp())//
      .withCallback(variablesDtoCallBack).send();
    }
  }

  public class VariablesDtoCallBack implements ResourceCallback<JsArray<VariableDto>> {

    @Override
    public void onResource(Response response, JsArray<VariableDto> resource) {
      for(int i = 0; i < resource.length(); i++) {
        getView().addVariableSuggestion(resource.get(i).getName());
      }
    }
  }

  private class ViewConfigurationRequiredHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      ViewDto viewDto = event.getView();
      viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));
      VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
      variableListDto.setVariablesArray(JsArrays.toSafeArray(variableListDto.getVariablesArray()));

      refreshVariableNameSuggestions(event.getView());
    }

  }

  private class CancelClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getView().hideDialog();
    }

  }

  private class AddVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(validate()) {
        getView().hideDialog();
        getEventBus().fireEvent(new VariableAddRequiredEvent(getVariableName()));
      }
    }
  }
}
