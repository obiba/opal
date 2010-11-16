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
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoryUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.DerivedVariableConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.LocalizableDeleteEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.VariableAddRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter.Mode;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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

  /**
   * Widget for entering, and testing, the "select" script.
   */
  @Inject
  private EvaluateScriptPresenter scriptWidget;

  private Runnable actionRequiringConfirmation;

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
    scriptWidget.bind();
    scriptWidget.setEvaluationMode(Mode.ENTITY_VALUE);
    getDisplay().setScriptWidget(scriptWidget.getDisplay());

    categoriesPresenter.bind();
    categoriesPresenter.getDisplay().setAddButtonText(translations.addNewCategory());
    getDisplay().addCategoriesTabWidget(categoriesPresenter.getDisplay().asWidget());

    attributesPresenter.bind();
    attributesPresenter.getDisplay().setAddButtonText(translations.addNewAttribute());
    getDisplay().addAttributesTabWidget(attributesPresenter.getDisplay().asWidget());

    addDerivedVariableDialogPresenter.bind();

    addEventHandlers();
    addValidators();
  }

  @Override
  protected void onUnbind() {
    scriptWidget.unbind();
    categoriesPresenter.unbind();
    attributesPresenter.unbind();
    addDerivedVariableDialogPresenter.unbind();
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

  //
  // Methods
  //

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;

    viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));

    VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
    variableListDto.setVariablesArray(JsArrays.toSafeArray(variableListDto.getVariablesArray()));

    TableDto tableDto = TableDto.create();
    tableDto.setDatasourceName(viewDto.getDatasourceName());
    tableDto.setName(viewDto.getName());
    scriptWidget.setTable(tableDto);
    scriptWidget.getDisplay().showResults(false);
    scriptWidget.getDisplay().clearResults();
    scriptWidget.getDisplay().showPaging(false);

    getDisplay().saveChangesEnabled(false);

    // Always show the Categories detail tab first.
    getDisplay().displayDetailTab(0);

    initDisplayComponents();
  }

  private void initDisplayComponents() {
    getDisplay().addButtonEnabled(true);
    getDisplay().navigationEnabled(true);

    variables = getVariableList();
    refreshVariableSuggestions();

    if(variables.isEmpty()) {
      // Clear variable selection.
      currentSelectedVariableIndex = -1;
      getDisplay().setSelectedVariableName(null, null, getNextVariableName());

      // Initialize the newVariableDto field (for creation of a new derived variable) and
      // announce to the world that this is the VariableDto currently being configured.
      newVariableDto = VariableDto.create();
      newVariableDto.setName("");
      eventBus.fireEvent(new DerivedVariableConfigurationRequiredEvent(newVariableDto));
      getDisplay().removeButtonEnabled(false);
    } else {
      currentSelectedVariableIndex = 0;
      updateSelectedVariableName();
      getDisplay().removeButtonEnabled(true);
    }
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
    super.registerHandler(eventBus.addHandler(DerivedVariableConfigurationRequiredEvent.getType(), new DerivedVariableConfigurationRequiredHandler()));
    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    super.registerHandler(eventBus.addHandler(ViewSavedEvent.getType(), new ViewSavedHandler()));
    super.registerHandler(getDisplay().addNameChangedHandler(new FormChangedHandler()));
    super.registerHandler(getDisplay().addValueTypeChangedHandler(new FormChangedHandler()));
    super.registerHandler(getDisplay().addScriptChangeHandler(new FormChangedHandler()));
    super.registerHandler(getDisplay().addRepeatableValueChangeHandler(new FormChangedHandler()));
    super.registerHandler(getDisplay().addOccurrenceGroupChangedHandler(new FormChangedHandler()));
    super.registerHandler(getDisplay().addUnitChangedHandler(new FormChangedHandler()));
    super.registerHandler(getDisplay().addMimeTypeChangedHandler(new FormChangedHandler()));
    super.registerHandler(eventBus.addHandler(CategoryUpdateEvent.getType(), new FormChangedHandler()));
    super.registerHandler(eventBus.addHandler(AttributeUpdateEvent.getType(), new FormChangedHandler()));
    super.registerHandler(eventBus.addHandler(LocalizableDeleteEvent.getType(), new FormChangedHandler()));
  }

  private void addValidators() {
    validators.add(new ConditionalValidator(getDisplay().getRepeatable(), new RequiredTextValidator(getDisplay().getOccurenceGroup(), "OccurrenceGroupIsRequired")));
    validators.add(new RequiredTextValidator(getDisplay().getName(), "NewVariableNameIsRequired"));
    validators.add(new UniqueVariableNameValidator("VariableNameNotUnique"));
    validators.add(new RequiredTextValidator(getDisplay().getScriptText(), "ScriptIsRequired"));
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

    HasText getName();

    HandlerRegistration addVariableClickHandler(ClickHandler handler);

    void setNewVariable(VariableDto variableDto);

    VariableDto getVariableDto();

    void setScriptWidget(EvaluateScriptPresenter.Display scriptWidgetDisplay);

    void setScriptWidgetVisible(boolean visible);

    void setScript(String script);

    String getScript();

    HasText getScriptText();

    HandlerRegistration addScriptChangeHandler(ChangeHandler changeHandler);

    void saveChangesEnabled(boolean enabled);

    void removeButtonEnabled(boolean enabled);

    void addButtonEnabled(boolean enabled);

    void navigationEnabled(boolean enabled);

    HandlerRegistration addNameChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addValueTypeChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addOccurrenceGroupChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addUnitChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addMimeTypeChangedHandler(ChangeHandler changeHandler);

  }

  class ViewConfigurationRequiredEventHandler implements ViewConfigurationRequiredEvent.Handler {

    @Override
    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      VariablesListTabPresenter.this.setViewDto(event.getView());
    }
  }

  class DerivedVariableConfigurationRequiredHandler implements DerivedVariableConfigurationRequiredEvent.Handler {

    @Override
    public void onDerivedVariableConfigurationRequired(DerivedVariableConfigurationRequiredEvent event) {
      getDisplay().setNewVariable(event.getVariable());
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
      addDerivedVariableDialogPresenter.getDisplay().getNewVariableName().setText("");
      addDerivedVariableDialogPresenter.getDisplay().getCopyFromVariableName().setText("");
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

    private VariableListViewDto variableListViewDto;

    private VariableDto currentVariableDto;

    @Override
    public void onClick(ClickEvent event) {
      if(validate()) {
        scriptWidget.evaluateScript(new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            int statusCode = response.getStatusCode();
            if(statusCode == Response.SC_OK) {
              updateViewDto();
            } else {
              eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, translations.scriptContainsErrorsAndWasNotSaved(), null));
            }
          }
        });
      }
    }

    private void updateViewDto() {
      variableListViewDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
      currentVariableDto = getDisplay().getVariableDto();
      updateCategories();
      updateAttributes();
      updateView(variableListViewDto);
      eventBus.fireEvent(new ViewSaveRequiredEvent(viewDto));
      newVariableDto = currentVariableDto;
    }

    private void updateCategories() {
      // Set variable categories, if they exist.
      if(categoriesPresenter.getVariableDto().getCategoriesArray() != null) {
        currentVariableDto.setCategoriesArray(categoriesPresenter.getVariableDto().getCategoriesArray());
      }
    }

    private void updateAttributes() {
      // Add attributes to current attributes. (The 'script' is stored as an attribute, so one attribute will always
      // exist.)
      if(attributesPresenter.getVariableDto().getAttributesArray() != null) {
        for(int i = 0; i < attributesPresenter.getVariableDto().getAttributesArray().length(); i++) {
          currentVariableDto.getAttributesArray().push(attributesPresenter.getVariableDto().getAttributesArray().get(i));
        }
      }
    }

    private void updateView(VariableListViewDto variableListViewDto) {
      boolean update = false;
      JsArray<VariableDto> variables = createAndGetVariableListFromView();

      for(int i = 0; i < variables.length(); i++) {
        VariableDto variableDto = variables.get(i);
        if(currentVariableDto.getName().equals(variableDto.getName())) {
          variables.set(i, currentVariableDto); // Update variable.
          update = true;
          break;
        }
      }
      if(!update) {
        if(!isEmptyVariable()) {
          variables.push(currentVariableDto); // Add new variable.
        }
      }
    }

    private boolean isEmptyVariable() {
      return currentVariableDto.getName().equals("") && variableListViewDto.getVariablesArray().length() == 0;
    }

    @SuppressWarnings("unchecked")
    private JsArray<VariableDto> createAndGetVariableListFromView() {
      JsArray<VariableDto> variables = variableListViewDto.getVariablesArray();
      if(variables == null) {
        variables = (JsArray<VariableDto>) JsArray.createArray();
        variableListViewDto.setVariablesArray(variables);
      }
      return variables;
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
            eventBus.fireEvent(new DerivedVariableConfigurationRequiredEvent(variableDto));
            setButtonsWhenAddingVariable();
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
              VariableDto variableDto = createEmptyDerivedVariable(firstTableDto.getEntityType());
              eventBus.fireEvent(new DerivedVariableConfigurationRequiredEvent(variableDto));
              setButtonsWhenAddingVariable();
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

    private void setButtonsWhenAddingVariable() {
      getDisplay().saveChangesEnabled(true);
      getDisplay().removeButtonEnabled(false);
      getDisplay().addButtonEnabled(false);
      getDisplay().navigationEnabled(false);
      eventBus.fireEvent(new ViewSavePendingEvent());
    }
  }

  class RemoveVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      actionRequiringConfirmation = new Runnable() {
        public void run() {
          deleteCurrentVariable();
        }
      };
      eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "deleteVariableTitle", "confirmVariableDelete"));
    }

  }

  private class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) && event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private void deleteCurrentVariable() {
    String nextVariableName = variableToDisplayAfterCurrentVariableDeleted();
    VariableListViewDto variableListViewDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
    VariableDto variableToDelete = getDisplay().getVariableDto();
    @SuppressWarnings("unchecked")
    JsArray<VariableDto> newVariables = (JsArray<VariableDto>) JsArray.createArray();
    for(int i = 0; i < variableListViewDto.getVariablesArray().length(); i++) {
      if(!variableListViewDto.getVariablesArray().get(i).getName().equals(variableToDelete.getName())) {
        newVariables.push(variableListViewDto.getVariablesArray().get(i));
      }
    }
    variableListViewDto.setVariablesArray(newVariables);
    variables = JsArrays.toList(newVariables);
    updateAndDisplayVariable(nextVariableName);
  }

  private void updateAndDisplayVariable(String nextVariableName) {
    if(nextVariableName != null) {
      currentSelectedVariableIndex = getVariableIndex(nextVariableName);
      updateSelectedVariableName();
    } else {
      currentSelectedVariableIndex = -1;
      getDisplay().setSelectedVariableName(null, null, getNextVariableName());
      VariableDto emptyVariableDto = VariableDto.create();
      emptyVariableDto.setName("");
      eventBus.fireEvent(new DerivedVariableConfigurationRequiredEvent(emptyVariableDto));
      getDisplay().removeButtonEnabled(false);
    }
    getDisplay().saveChangesEnabled(true);
    getDisplay().addButtonEnabled(false);
    getDisplay().navigationEnabled(false);
    eventBus.fireEvent(new ViewSavePendingEvent());
  }

  private String variableToDisplayAfterCurrentVariableDeleted() {
    String nextVariable = getNextVariableName();
    if(nextVariable != null) return nextVariable;
    String previousVariable = getPreviousVariableName();
    if(previousVariable != null) return previousVariable;
    return null;
  }

  class FormChangedHandler implements ChangeHandler, ValueChangeHandler<Boolean>, AttributeUpdateEvent.Handler, CategoryUpdateEvent.Handler, LocalizableDeleteEvent.Handler {

    @Override
    public void onChange(ChangeEvent arg0) {
      formChange();
    }

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> arg0) {
      formChange();
    }

    private void formChange() {
      eventBus.fireEvent(new ViewSavePendingEvent());
      getDisplay().saveChangesEnabled(true);
      getDisplay().addButtonEnabled(false);
      getDisplay().navigationEnabled(false);
      getDisplay().removeButtonEnabled(false);
    }

    @Override
    public void onAttributeUpdate(AttributeUpdateEvent event) {
      formChange();
    }

    @Override
    public void onCategoryUpdate(CategoryUpdateEvent event) {
      formChange();
    }

    @Override
    public void onLocalizableDelete(LocalizableDeleteEvent event) {
      formChange();
    }

  }

  class ViewSavedHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      getDisplay().saveChangesEnabled(false);
      getDisplay().addButtonEnabled(true);
      getDisplay().navigationEnabled(true);
      if(newVariableDto != null) {
        currentSelectedVariableIndex = getVariableIndex(newVariableDto.getName());
        updateSelectedVariableName();
        newVariableDto = null;
      }
      if(variables.size() > 0) getDisplay().removeButtonEnabled(true);
    }

  }

  public class UniqueVariableNameValidator extends AbstractFieldValidator {

    public UniqueVariableNameValidator(String errorMessageKey) {
      super(errorMessageKey);
    }

    @Override
    protected boolean hasError() {
      // Edits can have the same name.
      if(getDisplay().getName().getText().equals(variables.get(currentSelectedVariableIndex).getName())) return false;
      for(VariableDto variableDto : variables) {
        if(getDisplay().getName().getText().equals(variableDto.getName())) return true;
      }
      return false;
    }

  }
}