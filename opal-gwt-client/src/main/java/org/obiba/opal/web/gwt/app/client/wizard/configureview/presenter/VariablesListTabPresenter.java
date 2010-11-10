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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.DerivedVariableConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.VariableAddRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.TableDto;
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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Variables tab used to specify a view's variables by defining each variable using a Javascript expression.
 */
public class VariablesListTabPresenter extends WidgetPresenter<VariablesListTabPresenter.Display> {
  //
  // Constants
  //

  private static final int ATTRIBUTES_TAB_INDEX = 1;

  private static final int CATEGORIES_TAB_INDEX = 0;

  //
  // Instance Variables
  //

  private ViewDto viewDto;

  private VariableDto newVariableDto;

  private List<VariableDto> variables;

  private int currentSelectedVariableIndex;

  @Inject
  private CategoriesPresenter categoriesPresenter;

  @Inject
  private AttributesPresenter attributesPresenter;

  @Inject
  private AddDerivedVariableDialogPresenter addDerivedVariableDialogPresenter;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Translations translations = GWT.create(Translations.class);

  //
  // Constructors
  //

  @Inject
  public VariablesListTabPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    categoriesPresenter.bind();
    categoriesPresenter.getDisplay().setAddButtonText("Add New Category");
    getDisplay().addCategoriesTabWidget(categoriesPresenter.getDisplay().asWidget());

    attributesPresenter.bind();
    attributesPresenter.getDisplay().setAddButtonText("Add New Attribute");
    getDisplay().addAttributesTabWidget(attributesPresenter.getDisplay().asWidget());

    addDerivedVariableDialogPresenter.bind();

    initDisplayComponents();
    addEventHandlers();
    addValidators();
  }

  @Override
  protected void onUnbind() {
    categoriesPresenter.unbind();
    attributesPresenter.unbind();
    addDerivedVariableDialogPresenter.unbind();
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

  //
  // Methods
  //

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;

    categoriesPresenter.refreshDisplay();
    attributesPresenter.refreshDisplay();
  }

  private void initDisplayComponents() {
    variables = getVariableList();
    refreshVariableSuggestions();

    // Clear variable selection.
    currentSelectedVariableIndex = -1;
    getDisplay().setSelectedVariableName(null, null, getNextVariableName());

    // Initialize the newVariableDto field (for creation of a new derived variable) and
    // announce to the world that this is the VariableDto currently being configured.
    newVariableDto = VariableDto.create();
    newVariableDto.setName("new");
    eventBus.fireEvent(new DerivedVariableConfigurationRequiredEvent(newVariableDto));
  }

  private List<VariableDto> getVariableList() {
    VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
    return JsArrays.toList(variableListDto.getVariablesArray());
  }

  private void updateSelectedVariableName() {
    if(!variables.isEmpty()) {
      getDisplay().setSelectedVariableName(getSelectedVariableName(), getPreviousVariableName(), getNextVariableName());
      eventBus.fireEvent(new DerivedVariableConfigurationRequiredEvent(variables.get(currentSelectedVariableIndex)));
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

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));
    super.registerHandler(getDisplay().addPreviousVariableNameClickHandler(new PreviousVariableClickHandler()));
    super.registerHandler(getDisplay().addNextVariableNameClickHandler(new NextVariableClickHandler()));
    super.registerHandler(getDisplay().addVariableNameSelectedHandler(new VariableNameSelectedHandler()));
    super.registerHandler(getDisplay().addVariableNameEnterKeyPressed(new VariableNameEnterKeyPressedHandler()));
    super.registerHandler(getDisplay().addRepeatableValueChangeHandler(new RepeatableClickHandler()));
    super.registerHandler(getDisplay().addSaveChangesClickHandler(new SaveChangesClickHandler()));
    super.registerHandler(getDisplay().addAddVariableClickHandler(new AddVariableClickHandler()));
    super.registerHandler(getDisplay().addRemoveVariableClickHandler(new RemoveVariableClickHandler()));
    super.registerHandler(getDisplay().getDetailTabs().addBeforeSelectionHandler(new DetailTabsBeforeSelectionHandler()));
    super.registerHandler(eventBus.addHandler(VariableAddRequiredEvent.getType(), new VariableAddRequiredHandler()));
  }

  private void addValidators() {
    validators.add(new ConditionalValidator(getDisplay().getRepeatable(), new RequiredTextValidator(getDisplay().getOccurenceGroup(), "OccurrenceGroupIsRequired")));
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

  //
  // Inner Classes / Interfaces
  //

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

    HandlerRegistration addSaveChangesClickHandler(ClickHandler handler);

    HandlerRegistration addAddVariableClickHandler(ClickHandler handler);

    HandlerRegistration addRemoveVariableClickHandler(ClickHandler handler);

    void setEnabledOccurenceGroup(Boolean enabled);

    HasValue<Boolean> getRepeatable();

    void clearOccurrenceGroup();

    HasText getOccurenceGroup();

    HandlerRegistration addVariableClickHandler(ClickHandler handler);

    void setNewVariable(VariableDto variableDto);

    VariableDto getVariableDto();
  }

  class ViewConfigurationRequiredEventHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      VariablesListTabPresenter.this.setViewDto(event.getView());
    }
  }

  class PreviousVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(currentSelectedVariableIndex > 0) {
        currentSelectedVariableIndex--;
      }
      updateSelectedVariableName();
    }
  }

  class NextVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(currentSelectedVariableIndex < variables.size() - 1) {
        currentSelectedVariableIndex++;
      }
      updateSelectedVariableName();
    }

  }

  class VariableNameSelectedHandler implements SelectionHandler<Suggestion> {

    @Override
    public void onSelection(SelectionEvent<Suggestion> event) {
      currentSelectedVariableIndex = getVariableIndex(event.getSelectedItem().getReplacementString());
      updateSelectedVariableName();
    }
  }

  class AddVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      addDerivedVariableDialogPresenter.getDisplay().showDialog();

    }
  }

  class VariableNameEnterKeyPressedHandler implements KeyDownHandler {

    @Override
    public void onKeyDown(KeyDownEvent event) {
      if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        GWT.log("Selection changed to (enter) " + getDisplay().getSelectedVariableName() + ", handler: " + this);
      }
    }

  }

  class RepeatableClickHandler implements ValueChangeHandler<Boolean> {

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
      boolean enabled = event.getValue();
      getDisplay().setEnabledOccurenceGroup(enabled);
      if(!enabled) {
        getDisplay().clearOccurrenceGroup();
      }
    }

  }

  class DetailTabsBeforeSelectionHandler implements BeforeSelectionHandler<Integer> {

    @Override
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
      switch(event.getItem()) {
      case CATEGORIES_TAB_INDEX:
        categoriesPresenter.refreshDisplay();
        break;
      case ATTRIBUTES_TAB_INDEX:
        attributesPresenter.refreshDisplay();
        break;
      }
    }
  }

  class SaveChangesClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(validate()) {
        // TODO Save the view
      }
    }

  }

  class VariableAddRequiredHandler implements VariableAddRequiredEvent.Handler {

    private String newDerivedVariableName;

    // View is guaranteed to have one table. We will obtain the entity type from it.
    private String[] firstTableInViewParts;

    @Override
    public void onVariableAddRequired(VariableAddRequiredEvent event) {
      newDerivedVariableName = event.getVariableName();
      for(int i = 0; i < viewDto.getFromArray().length(); i++) {
        String[] tableParts = viewDto.getFromArray().get(i).split("\\.");
        if(i == 0) firstTableInViewParts = tableParts;
        ResourceRequestBuilderFactory.<VariableDto> newBuilder()
        /**/.forResource("/datasource/" + tableParts[0] + "/table/" + tableParts[1] + "/variable/" + newDerivedVariableName)
        /**/.get()
        /**/.withCallback(new ResourceCallback<VariableDto>() {
          @Override
          public void onResource(Response response, VariableDto variableDto) {
            variableDto.setName(translations.copyOf() + variableDto.getName());
            getDisplay().setNewVariable(variableDto);
          }
        })
        /**/.withCallback(Response.SC_NOT_FOUND, createResponseCodeCallback())
        /**/.send();

      }
    }

    private ResponseCodeCallback createResponseCodeCallback() {
      return new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          ResourceRequestBuilderFactory.<TableDto> newBuilder()
          /**/.forResource("/datasource/" + firstTableInViewParts[0] + "/table/" + firstTableInViewParts[1])
          /**/.get()
          /**/.withCallback(new ResourceCallback<TableDto>() {
            @Override
            public void onResource(Response response, TableDto firstTableDto) {
              getDisplay().setNewVariable(createEmptyDerivedVariable(firstTableDto.getEntityType()));
            }
          })
          /**/.send();

        }

      };
    }

    private VariableDto createEmptyDerivedVariable(String entityType) {
      VariableDto variableDto = VariableDto.create();
      variableDto.setName(newDerivedVariableName);
      variableDto.setEntityType(entityType);
      return variableDto;
    }
  }

  class RemoveVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {

    }

  }
}