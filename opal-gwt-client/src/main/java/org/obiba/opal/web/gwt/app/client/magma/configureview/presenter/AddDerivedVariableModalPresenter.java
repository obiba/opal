/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.configureview.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.configureview.event.VariableAddRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidatableWidgetPresenter;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class AddDerivedVariableModalPresenter
    extends ValidatableWidgetPresenter<AddDerivedVariableModalPresenter.Display>
    implements AddDerivedVariableModalUiHandlers {

  @Override
  public void addVariable(String variableName) {
    getView().hideDialog();
    getEventBus().fireEvent(new VariableAddRequiredEvent(variableName));

  }

  public interface Display extends PopupView, HasUiHandlers<AddDerivedVariableModalUiHandlers> {

    void hideDialog();

    void clearVariableSuggestions();

    HasText getVariableName();
  }

  @Inject
  public AddDerivedVariableModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
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
    registerHandler(
        getEventBus().addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredHandler()));
  }

  void refreshVariableNameSuggestions(ViewDto viewDto) {
    getView().clearVariableSuggestions();
  }

  private class ViewConfigurationRequiredHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      ViewDto viewDto = event.getView();
      viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));
      VariableListViewDto variableListDto = (VariableListViewDto) viewDto
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);
      variableListDto.setVariablesArray(JsArrays.toSafeArray(variableListDto.getVariablesArray()));

      refreshVariableNameSuggestions(event.getView());
    }
  }
}
