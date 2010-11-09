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
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Variables tab used to specify a view's variables by defining each variable using a Javascript expression.
 */
public class VariablesListTabPresenter extends WidgetPresenter<VariablesListTabPresenter.Display> {

  private ViewDto viewDto;

  private List<VariableDto> variables;

  private int currentSelectedVariableIndex;

  @Inject
  private CategoriesPresenter categoriesPresenter;

  @Inject
  private AttributesPresenter attributesPresenter;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  public interface Display extends WidgetDisplay {

    HasBeforeSelectionHandlers<Integer> getDetailTabs();

    void displayDetailTab(int tabNumber);

    void addCategoriesTabWidget(Widget categoriesTabWidget);

    void addAttributesTabWidget(Widget attributesTabWidget);

    void clearVariableNameSuggestions();

    void addVariableNameSuggestion(String variableName);

    void setSelectedVariableName(String variableName, String previousVariableName, String nextVariableName);

    String getSelectedVariableName();

    HandlerRegistration addPreviousVariableNameClickHandler(ClickHandler handler);

    HandlerRegistration addNextVariableNameClickHandler(ClickHandler handler);

    HandlerRegistration addVariableNameChangedHandler(ValueChangeHandler<String> handler);

    HandlerRegistration addVariableNameSelectedHandler(SelectionHandler<Suggestion> handler);

    HandlerRegistration addVariableNameEnterKeyPressed(KeyDownHandler keyDownHandler);

    HandlerRegistration addRepeatableValueChangeHandler(ValueChangeHandler<Boolean> handler);

    void setEnabledOccurenceGroup(Boolean enabled);

    void clearOccurrenceGroup();

    HasText getOccurenceGroup();

  }

  @Inject
  public VariablesListTabPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  protected void onBind() {
    categoriesPresenter.bind();
    categoriesPresenter.getDisplay().setAddButtonText("Add New Category");
    getDisplay().addCategoriesTabWidget(categoriesPresenter.getDisplay().asWidget());

    attributesPresenter.bind();
    attributesPresenter.getDisplay().setAddButtonText("Add New Attribute");
    getDisplay().addAttributesTabWidget(attributesPresenter.getDisplay().asWidget());

    initDisplayComponents();
    addEventHandlers();
    addValidators();
  }

  @Override
  protected void onUnbind() {
    categoriesPresenter.unbind();
    attributesPresenter.unbind();
  }

  @Override
  public void revealDisplay() {
    // Always show the Categories detail tab first.
    getDisplay().displayDetailTab(0);
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
    variables = JsArrays.toList(variableListDto.getVariablesArray());
    refreshVariableSuggestions();
    currentSelectedVariableIndex = -1;
    getDisplay().setSelectedVariableName(null, null, getNextVariableName());
  }

  private void updateSelectedVariableName() {
    if(!variables.isEmpty()) {
      getDisplay().setSelectedVariableName(getSelectedVariableName(), getPreviousVariableName(), getNextVariableName());
    }
  }

  private String getSelectedVariableName() {
    return variables.get(currentSelectedVariableIndex).getName();
  }

  private String getPreviousVariableName() {
    String previousVariable = null;
    if(currentSelectedVariableIndex > 0) {
      previousVariable = variables.get(currentSelectedVariableIndex - 1).getName();
    }
    return previousVariable;
  }

  private String getNextVariableName() {
    String nextVariable = null;
    if(currentSelectedVariableIndex < variables.size() - 1) {
      nextVariable = variables.get(currentSelectedVariableIndex + 1).getName();
    }
    return nextVariable;
  }

  private void refreshVariableSuggestions() {
    getDisplay().clearVariableNameSuggestions();
    for(VariableDto variableDto : variables) {
      getDisplay().addVariableNameSuggestion(variableDto.getName());
    }
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;

    categoriesPresenter.setViewDto(viewDto);
    categoriesPresenter.refreshDisplay();

    attributesPresenter.setViewDto(viewDto);
    attributesPresenter.refreshDisplay();
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addPreviousVariableNameClickHandler(new PreviousVariableClickHandler()));
    super.registerHandler(getDisplay().addNextVariableNameClickHandler(new NextVariableClickHandler()));
    super.registerHandler(getDisplay().addVariableNameSelectedHandler(new VariableNameSelectedHandler()));
    super.registerHandler(getDisplay().addVariableNameEnterKeyPressed(new VariableNameEnterKeyPressedHandler()));
    super.registerHandler(getDisplay().addRepeatableValueChangeHandler(new RepeatableClickHandler()));

    super.registerHandler(getDisplay().getDetailTabs().addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

      @Override
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        switch(event.getItem()) {
        case 0:
          categoriesPresenter.refreshDisplay();
          break;
        case 1:
          attributesPresenter.refreshDisplay();
          break;
        }
        // TODO: case 2 (options tab)
      }
    }));
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getDisplay().getOccurenceGroup(), "OccurrenceGroupIsRequired"));
  }

  private boolean validate() {
    List<String> messages = new ArrayList<String>();
    String message;
    for(FieldValidator validator : validators) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
      }
    }

    if(messages.size() > 0) {
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, messages, null));
      return false;
    } else {
      return true;
    }
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

  private class VariableNameSelectedHandler implements SelectionHandler<Suggestion> {

    @Override
    public void onSelection(SelectionEvent<Suggestion> event) {
      currentSelectedVariableIndex = getVariableIndex(event.getSelectedItem().getReplacementString());
      updateSelectedVariableName();
    }
  }

  public class VariableNameEnterKeyPressedHandler implements KeyDownHandler {

    @Override
    public void onKeyDown(KeyDownEvent event) {
      if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        GWT.log("Selection changed to (enter) " + getDisplay().getSelectedVariableName() + ", handler: " + this);
      }
    }

  }

  public class RepeatableClickHandler implements ValueChangeHandler<Boolean> {

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
      boolean enabled = event.getValue();
      getDisplay().setEnabledOccurenceGroup(enabled);
      if(!enabled) {
        getDisplay().clearOccurrenceGroup();
      }
    }

  }

  private int getVariableIndex(String variableName) {
    int i = 0;
    for(VariableDto variable : variables) {
      if(variable.getName().equals(variableName)) {
        break;
      }
      i++;
    }
    return i;
  }

}