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
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.VariablesListTabView;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter.Mode;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.AttributeDto;
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

  /** The name of the variable currently displayed. */
  private String displayedVariableName;

  private boolean addVariable;

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
    scriptWidget.setTableIsView(true);
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

    displayedVariableName = getVariableList().isEmpty() ? null : getVariableList().get(0).getName();
    refreshVariableSuggestions();

    if(getVariableList().isEmpty()) {
      // Clear variable selection.
      getDisplay().setSelectedVariableName(null, null, getNextVariableName());
      formClear();
      formEnabled(false);
    } else {
      updateSelectedVariableName();
      getDisplay().removeButtonEnabled(true);
    }
  }

  /**
   * Returns an up-to-date list of variables.
   */
  private List<VariableDto> getVariableList() {
    VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
    return JsArrays.toList(variableListDto.getVariablesArray());
  }

  private void updateSelectedVariableName() {
    if(!getVariableList().isEmpty()) {
      getDisplay().setSelectedVariableName(displayedVariableName, getPreviousVariableName(), getNextVariableName());
      eventBus.fireEvent(new DerivedVariableConfigurationRequiredEvent(getVariableList().get(getVariableIndex(displayedVariableName))));
    }
  }

  private String getPreviousVariableName() {
    if(getVariableList().isEmpty()) {
      return null;
    } else {
      if(displayedVariableName == null) return getVariableList().get(0).getName();
      int index = getVariableIndex(displayedVariableName);
      index--;
      if(index >= 0) {
        return getVariableList().get(index).getName();
      } else {
        return null; // At the beginning of the list.
      }
    }
  }

  private String getNextVariableName() {
    if(getVariableList().isEmpty()) {
      return null;
    } else {
      if(displayedVariableName == null) return getVariableList().get(0).getName();
      int index = getVariableIndex(displayedVariableName);
      index++;
      if(index < getVariableList().size()) {
        return getVariableList().get(index).getName();
      } else {
        return null; // At the end of the list.
      }
    }
  }

  private void refreshVariableSuggestions() {
    getDisplay().clearVariableNameSuggestions();
    for(VariableDto variableDto : getVariableList()) {
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
    registerFormChangedHandler();
  }

  private void registerFormChangedHandler() {
    FormChangedHandler formChangedHandler = new FormChangedHandler();

    super.registerHandler(getDisplay().addNameChangedHandler(formChangedHandler));
    super.registerHandler(getDisplay().addValueTypeChangedHandler(formChangedHandler));
    super.registerHandler(getDisplay().addScriptChangeHandler(formChangedHandler));
    super.registerHandler(getDisplay().addRepeatableValueChangeHandler(formChangedHandler));
    super.registerHandler(getDisplay().addOccurrenceGroupChangedHandler(formChangedHandler));
    super.registerHandler(getDisplay().addUnitChangedHandler(formChangedHandler));
    super.registerHandler(getDisplay().addMimeTypeChangedHandler(formChangedHandler));
    super.registerHandler(eventBus.addHandler(CategoryUpdateEvent.getType(), formChangedHandler));
    super.registerHandler(eventBus.addHandler(AttributeUpdateEvent.getType(), formChangedHandler));
    super.registerHandler(eventBus.addHandler(LocalizableDeleteEvent.getType(), formChangedHandler));
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
    int result = -1;
    for(int i = 0; i < getVariableList().size(); i++) {
      if(getVariableList().get(i).getName().equals(variableName)) {
        result = i;
        break;
      }
    }
    return result;
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

    void variableNameEnabled(boolean enabled);

    HandlerRegistration addNameChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addValueTypeChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addOccurrenceGroupChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addUnitChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addMimeTypeChangedHandler(ChangeHandler changeHandler);

    void formEnable(boolean enabled);

    void formClear();

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
      formEnabled(true);
      getDisplay().saveChangesEnabled(false);
      getDisplay().setNewVariable(event.getVariable());
    }
  }

  class PreviousVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(getPreviousVariableName() != null) {
        displayedVariableName = getPreviousVariableName();
      }
      updateSelectedVariableName();
    }
  }

  class NextVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(getNextVariableName() != null) {
        displayedVariableName = getNextVariableName();
      }
      updateSelectedVariableName();
    }

  }

  class VariableNameSelectedHandler implements SelectionHandler<Suggestion> {

    @Override
    public void onSelection(SelectionEvent<Suggestion> event) {
      displayedVariableName = event.getSelectedItem().getReplacementString();
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
      variableListViewDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
      variableListViewDto.setVariablesArray(JsArrays.toSafeArray(variableListViewDto.getVariablesArray()));

      currentVariableDto = getDisplay().getVariableDto();
      if(isEmptyVariable()) {
        // This view has no variables. Clear the variable list and save.
        variableListViewDto.clearVariablesArray();
        eventBus.fireEvent(new ViewSaveRequiredEvent(viewDto));
      } else {
        // Validate current variable and save to variable list.
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
    }

    private void updateViewDto() {
      updateCategories();
      updateAttributes();
      if(!addVariable) {
        deleteVariable(displayedVariableName); // The current variable is deleted for update and delete operations only.
      }
      updateVariable();
      displayedVariableName = currentVariableDto.getName(); // Must note this before form is refreshed.
      eventBus.fireEvent(new ViewSaveRequiredEvent(viewDto));
    }

    private void updateCategories() {
      // Set variable categories, if they exist.
      if(categoriesPresenter.getVariableDto().getCategoriesArray() != null) {
        currentVariableDto.setCategoriesArray(categoriesPresenter.getVariableDto().getCategoriesArray());
      }
    }

    private void updateAttributes() {
      AttributeDto currentVariableScriptAttribute = getAttributeByName(VariablesListTabView.SCRIPT_NAME, currentVariableDto.getAttributesArray());
      AttributeDto existingVariableScriptAttribute = getAttributeByName(VariablesListTabView.SCRIPT_NAME, attributesPresenter.getVariableDto().getAttributesArray());
      if(existingVariableScriptAttribute != null) {
        // Duplicate 'script' attribute exists. Overwrite the existing 'script' attribute.
        existingVariableScriptAttribute.setValue(currentVariableScriptAttribute.getValue());
        currentVariableDto.setAttributesArray(attributesPresenter.getVariableDto().getAttributesArray());
      } else {
        // No duplicate 'script' attribute. Add the 'script' attribute.
        currentVariableDto.setAttributesArray(attributesPresenter.getVariableDto().getAttributesArray());
        currentVariableDto.getAttributesArray().push(currentVariableScriptAttribute);
      }
    }

    private AttributeDto getAttributeByName(String attributeName, JsArray<AttributeDto> attributes) {
      if(attributes == null) return null;
      for(int i = 0; i < attributes.length(); i++) {
        if(attributes.get(i).getName().equals(attributeName)) return attributes.get(i);
      }
      return null;
    }

    private boolean isEmptyVariable() {
      return currentVariableDto.getName().equals("") && variableListViewDto.getVariablesArray().length() == 0;
    }

    private void updateVariable() {
      variableListViewDto.getVariablesArray().push(currentVariableDto);
    }

    private void deleteVariable(String variableName) {
      @SuppressWarnings("unchecked")
      JsArray<VariableDto> result = (JsArray<VariableDto>) JsArray.createArray();
      for(int i = 0; i < variableListViewDto.getVariablesArray().length(); i++) {
        if(!variableName.equals(variableListViewDto.getVariablesArray().get(i).getName())) result.push(variableListViewDto.getVariablesArray().get(i));
      }
      variableListViewDto.setVariablesArray(result);
    }

  }

  class VariableAddRequiredHandler implements VariableAddRequiredEvent.Handler {

    private String newDerivedVariableName;

    // View is guaranteed to have one table. We will obtain the entity type from it.
    private String[] firstTableInViewParts;

    @Override
    public void onVariableAddRequired(VariableAddRequiredEvent event) {
      newDerivedVariableName = event.getVariableName();
      if(isNewDerivedVariableNameSameAsExistingDerivedVariableName()) {
        newDerivedVariableIsCopyOfExistingDerivedVariable();
      } else {
        checkIfNewDerivedVariableNameSameAsExistingVariableName();
      }
      addVariable = true;
    }

    private void checkIfNewDerivedVariableNameSameAsExistingVariableName() {
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
        /**/.withCallback(Response.SC_NOT_FOUND, newDerivedVariableResponseCodeCallback())
        /**/.send();

      }
    }

    private ResponseCodeCallback newDerivedVariableResponseCodeCallback() {
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

    private void newDerivedVariableIsCopyOfExistingDerivedVariable() {
      ResourceRequestBuilderFactory.<ViewDto> newBuilder()
      /**/.forResource("/datasource/" + viewDto.getDatasourceName() + "/view/" + viewDto.getName())
      /**/.get()
      /**/.withCallback(new ResourceCallback<ViewDto>() {

        List<VariableDto> variablesList;

        @Override
        public void onResource(Response response, ViewDto viewDto) {
          VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
          variablesList = JsArrays.toList(variableListDto.getVariablesArray());
          VariableDto variableDto = getVariableDto();
          variableDto.setName(translations.copyOf() + variableDto.getName());
          eventBus.fireEvent(new DerivedVariableConfigurationRequiredEvent(variableDto));
          setButtonsWhenAddingVariable();
        }

        private VariableDto getVariableDto() {
          VariableDto result = null;
          for(VariableDto variableDto : variablesList) {
            if(newDerivedVariableName.equals(variableDto.getName())) result = variableDto;
          }
          return result;
        }
      }).send();
    }

    private boolean isNewDerivedVariableNameSameAsExistingDerivedVariableName() {
      for(VariableDto variableDto : getVariableList()) {
        if(newDerivedVariableName.equals(variableDto.getName())) return true;
      }
      return false;
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
    variableListViewDto.setVariablesArray(newVariables); // Updates the viewDto.
    updateAndDisplayVariable(nextVariableName);
  }

  private void updateAndDisplayVariable(String nextVariableName) {
    if(nextVariableName != null) {
      displayedVariableName = nextVariableName;
      updateSelectedVariableName();
    } else {
      displayedVariableName = null;
      getDisplay().setSelectedVariableName(null, null, getNextVariableName());
      formClear();
      formEnabled(false);
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
      addVariable = false;
      getDisplay().saveChangesEnabled(false);
      getDisplay().addButtonEnabled(true);
      getDisplay().navigationEnabled(true);
      updateSelectedVariableName();
      if(getVariableList().size() > 0) getDisplay().removeButtonEnabled(true);
    }

  }

  public class UniqueVariableNameValidator extends AbstractFieldValidator {

    public UniqueVariableNameValidator(String errorMessageKey) {
      super(errorMessageKey);
    }

    @Override
    protected boolean hasError() {
      if(displayedVariableName != null) {
        // Edits can have the same name.
        if(getDisplay().getName().getText().equals(displayedVariableName)) return false;
      }
      for(VariableDto variableDto : getVariableList()) {
        if(getDisplay().getName().getText().equals(variableDto.getName())) return true;
      }
      return false;
    }

  }

  /**
   * Enables or disables the variable list form. The "Add" variable button is always enabled regardless of state.
   */
  private void formEnabled(boolean enabled) {
    getDisplay().formEnable(enabled);
    categoriesPresenter.getDisplay().formEnable(enabled);
    attributesPresenter.getDisplay().formEnable(enabled);
  }

  /**
   * Clears the variable list form.
   */
  private void formClear() {
    getDisplay().formClear();
    categoriesPresenter.formClear();
    attributesPresenter.formClear();
  }
}