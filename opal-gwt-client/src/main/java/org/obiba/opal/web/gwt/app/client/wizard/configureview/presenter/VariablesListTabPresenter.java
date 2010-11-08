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

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;

/**
 * Variables tab used to specify a view's variables by defining each variable using a Javascript expression.
 */
public class VariablesListTabPresenter extends WidgetPresenter<VariablesListTabPresenter.Display> {

  private ViewDto viewDto;

  private List<VariableDto> variables;

  private int currentSelectedVariableIndex;

  public interface Display extends WidgetDisplay {
    void clearVariableListSuggestions();

    void addVariableNameSuggestion(String variableName);

    void setSelectedVariableName(String variableName);

    HandlerRegistration addPreviousVariableNameClickHandler(ClickHandler handler);

    HandlerRegistration addNextVariableNameClickHandler(ClickHandler handler);

    HandlerRegistration addVariableNameChangedHandler(ValueChangeHandler<String> handler);

    HandlerRegistration addVariableNameSelectedHandler(SelectionHandler<Suggestion> handler);

  }

  @Inject
  public VariablesListTabPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  protected void onBind() {

    // test
    String[] names = new String[] { "avariable1", "avariable2", "rvariable", "zvariable" };
    JsArray<VariableDto> variablesArray = VariableDto.createArray();
    for(int i = 0; i < names.length; i++) {
      VariableDto variable = VariableDto.create();
      variable.setName(names[i]);
      variablesArray.push(variable);
    }
    variables = JsArrays.toList(variablesArray);

    initDisplayComponents();
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void initDisplayComponents() {
    VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
    // variables = JsArrays.toList(variableListDto.getVariablesArray());
    refreshVariableSuggestions();
    getDisplay().setSelectedVariableName("");
  }

  private void updateSelectedVariableName() {
    GWT.log("name is " + variables.get(currentSelectedVariableIndex).getName());
    getDisplay().setSelectedVariableName(!variables.isEmpty() ? variables.get(currentSelectedVariableIndex).getName() : "");
  }

  private void refreshVariableSuggestions() {
    getDisplay().clearVariableListSuggestions();
    for(VariableDto variableDto : variables) {
      getDisplay().addVariableNameSuggestion(variableDto.getName());
    }
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addPreviousVariableNameClickHandler(new PreviousVariableClickHandler()));
    super.registerHandler(getDisplay().addNextVariableNameClickHandler(new NextVariableClickHandler()));
    super.registerHandler(getDisplay().addVariableNameChangedHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        GWT.log("Value changed to " + event.getValue());

      }
    }));
    super.registerHandler(getDisplay().addVariableNameSelectedHandler(new SelectionHandler<Suggestion>() {

      @Override
      public void onSelection(SelectionEvent<Suggestion> event) {
        GWT.log("Selection changed to " + event.getSelectedItem().getDisplayString());
      }
    }));
  }

  private class PreviousVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(currentSelectedVariableIndex > 0) {
        currentSelectedVariableIndex--;
      }
      updateSelectedVariableName();
    }

  }

  private class NextVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(currentSelectedVariableIndex < variables.size() - 1) {
        currentSelectedVariableIndex++;
      }
      updateSelectedVariableName();
    }

  }

}