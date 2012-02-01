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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoryUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.DerivedVariableConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.LocalizableDeleteEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.VariableAddRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Variables tab used to specify a view's variables by defining each variable using a Javascript expression.
 */
public class VariablesListTabPresenter extends PresenterWidget<VariablesListTabPresenter.Display> {

  private static enum Tabs {
    /* These should be in the same order as in the UI */
    SCRIPT, CATEGORIES, ATTRIBUTES, OPTIONS, SUMMARY;
  }

  private final CategoriesPresenter categoriesPresenter;

  private final AttributesPresenter attributesPresenter;

  private final SummaryTabPresenter summaryPresenter;

  private final AddDerivedVariableDialogPresenter addDerivedVariableDialogPresenter;

  private final Translations translations;

  /**
   * Widget for entering, and testing, the "select" script.
   */
  private final EvaluateScriptPresenter evaluateScriptPresenter;

  private ViewDto viewDto;

  /** The name of the variable currently displayed. */
  private String displayedVariableName;

  private boolean addVariable;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Runnable actionRequiringConfirmation;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public VariablesListTabPresenter(final Display display, final EventBus eventBus, //
  CategoriesPresenter categoriesPresenter, AttributesPresenter attributesPresenter, //
  SummaryTabPresenter summaryPresenter, AddDerivedVariableDialogPresenter addDerivedVariableDialogPresenter, //
  EvaluateScriptPresenter evaluateScriptPresenter, Translations translations) {
    super(eventBus, display);
    this.categoriesPresenter = categoriesPresenter;
    this.attributesPresenter = attributesPresenter;
    this.summaryPresenter = summaryPresenter;
    this.addDerivedVariableDialogPresenter = addDerivedVariableDialogPresenter;
    this.evaluateScriptPresenter = evaluateScriptPresenter;
    this.translations = translations;
  }

  @Override
  protected void onBind() {
    setInSlot(Display.Slots.Test, evaluateScriptPresenter);

    categoriesPresenter.bind();
    categoriesPresenter.getDisplay().setAddButtonText(translations.addNewCategory());
    getView().addCategoriesTabWidget(categoriesPresenter.getDisplay().asWidget());

    attributesPresenter.bind();
    attributesPresenter.getDisplay().setAddButtonText(translations.addNewAttribute());
    getView().addAttributesTabWidget(attributesPresenter.getDisplay().asWidget());

    summaryPresenter.bind();
    getView().addSummaryTabWidget(summaryPresenter.getDisplay().asWidget());

    addEventHandlers();
    addValidators();
  }

  @Override
  protected void onUnbind() {
    categoriesPresenter.unbind();
    attributesPresenter.unbind();
    addDerivedVariableDialogPresenter.unbind();
    summaryPresenter.unbind();
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;

    viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));

    VariableListViewDto variableListDto = (VariableListViewDto) viewDto.getExtension(VariableListViewDto.ViewDtoExtensions.view);
    variableListDto.setVariablesArray(JsArrays.toSafeArray(variableListDto.getVariablesArray()));

    TableDto tableDto = TableDto.create();
    tableDto.setDatasourceName(viewDto.getDatasourceName());
    tableDto.setName(viewDto.getName());
    tableDto.setViewLink("/datasource/" + viewDto.getDatasourceName() + "/view/" + viewDto.getName());
    evaluateScriptPresenter.setTable(tableDto);

    getView().saveChangesEnabled(false);

    // Always show the Categories detail tab first.
    getView().displayDetailTab(0);

    initDisplayComponents();
  }

  private void initDisplayComponents() {
    getView().addButtonEnabled(true);
    getView().navigationEnabled(true);

    displayedVariableName = getVariableList().isEmpty() ? null : getVariableList().get(0).getName();
    refreshVariableSuggestions();

    if(getVariableList().isEmpty()) {
      // Clear variable selection.
      getView().setSelectedVariableName(null, null, getNextVariableName());
      formClear();
      formEnabled(false);
    } else {
      updateSelectedVariableName();
      getView().removeButtonEnabled(true);
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
      getView().setSelectedVariableName(displayedVariableName, getPreviousVariableName(), getNextVariableName());
      getEventBus().fireEvent(new DerivedVariableConfigurationRequiredEvent(getVariableList().get(getVariableIndex(displayedVariableName))));
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
    getView().clearVariableNameSuggestions();
    for(VariableDto variableDto : getVariableList()) {
      getView().addVariableNameSuggestion(variableDto.getName());
    }
  }

  private void addEventHandlers() {
    super.registerHandler(getEventBus().addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));
    super.registerHandler(getView().addPreviousVariableNameClickHandler(new PreviousVariableClickHandler()));
    super.registerHandler(getView().addNextVariableNameClickHandler(new NextVariableClickHandler()));
    super.registerHandler(getView().addVariableNameSelectedHandler(new VariableNameSelectedHandler()));
    super.registerHandler(getView().addVariableNameEnterKeyPressed(new VariableNameEnterKeyPressedHandler()));
    super.registerHandler(getView().addRepeatableValueChangeHandler(new RepeatableClickHandler()));
    super.registerHandler(getView().addSaveChangesClickHandler(new SaveChangesClickHandler()));
    super.registerHandler(getView().addAddVariableClickHandler(new AddVariableClickHandler()));
    super.registerHandler(getView().addRemoveVariableClickHandler(new RemoveVariableClickHandler()));
    super.registerHandler(getView().getDetailTabs().addBeforeSelectionHandler(new DetailTabsBeforeSelectionHandler()));
    super.registerHandler(getEventBus().addHandler(VariableAddRequiredEvent.getType(), new VariableAddRequiredHandler()));
    super.registerHandler(getEventBus().addHandler(DerivedVariableConfigurationRequiredEvent.getType(), new DerivedVariableConfigurationRequiredHandler()));
    super.registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    super.registerHandler(getEventBus().addHandler(ViewSavedEvent.getType(), new ViewSavedHandler()));
    registerFormChangedHandler();
  }

  private void registerFormChangedHandler() {
    FormChangedHandler formChangedHandler = new FormChangedHandler();

    super.registerHandler(getView().addNameChangedHandler(formChangedHandler));
    super.registerHandler(getView().addValueTypeChangedHandler(formChangedHandler));
    super.registerHandler(evaluateScriptPresenter.getView().addScriptChangeHandler(formChangedHandler));
    super.registerHandler(getView().addRepeatableValueChangeHandler(formChangedHandler));
    super.registerHandler(getView().addOccurrenceGroupChangedHandler(formChangedHandler));
    super.registerHandler(getView().addUnitChangedHandler(formChangedHandler));
    super.registerHandler(getView().addMimeTypeChangedHandler(formChangedHandler));
    super.registerHandler(getEventBus().addHandler(CategoryUpdateEvent.getType(), formChangedHandler));
    super.registerHandler(getEventBus().addHandler(AttributeUpdateEvent.getType(), formChangedHandler));
    super.registerHandler(getEventBus().addHandler(LocalizableDeleteEvent.getType(), formChangedHandler));
  }

  private void addValidators() {
    validators.add(new ConditionalValidator(getView().getRepeatable(), new RequiredTextValidator(getView().getOccurenceGroup(), "OccurrenceGroupIsRequired")));
    validators.add(new RequiredTextValidator(getView().getName(), "NewVariableNameIsRequired"));
    validators.add(new UniqueVariableNameValidator("VariableNameNotUnique"));
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
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(messages).build());
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

  private boolean isTabSelected(Tabs tab) {
    return getView().getSelectedTab() == tab.ordinal();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    enum Slots {
      Test
    }

    HasBeforeSelectionHandlers<Integer> getDetailTabs();

    void displayDetailTab(int tabNumber);

    int getSelectedTab();

    void addCategoriesTabWidget(Widget categoriesTabWidget);

    void addAttributesTabWidget(Widget attributesTabWidget);

    void addSummaryTabWidget(Widget summaryTabWidget);

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

    VariableDto getVariableDto(String script);

    void setScriptWidgetVisible(boolean visible);

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
      if(event.getVariable() != null) {
        VariablesListTabPresenter.this.updateAndDisplayVariable(event.getVariable().getName());
      }
    }
  }

  class DerivedVariableConfigurationRequiredHandler implements DerivedVariableConfigurationRequiredEvent.Handler {

    @Override
    public void onDerivedVariableConfigurationRequired(DerivedVariableConfigurationRequiredEvent event) {
      formEnabled(true);
      getView().saveChangesEnabled(false);
      getView().setNewVariable(event.getVariable());
      setScript(event.getVariable());
      updateSummaryLink(false);
      if(isTabSelected(Tabs.SUMMARY)) {
        summaryPresenter.refreshDisplay();
      }
    }

    private void setScript(VariableDto variableDto) {
      AttributeDto attr = Variables.getScriptAttribute(variableDto);
      if(attr != null) {
        evaluateScriptPresenter.setScript(attr.getValue());
      } else {
        evaluateScriptPresenter.setScript("");
      }
    }
  }

  class PreviousVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(getPreviousVariableName() != null) {
        displayedVariableName = getPreviousVariableName();
        updateSelectedVariableName();
      }
    }
  }

  class NextVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(getNextVariableName() != null) {
        displayedVariableName = getNextVariableName();
        updateSelectedVariableName();
      }
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
      addDerivedVariableDialogPresenter.getView().getVariableName().setText("");
      addToPopupSlot(addDerivedVariableDialogPresenter);
    }
  }

  class VariableNameEnterKeyPressedHandler implements KeyDownHandler {

    @Override
    public void onKeyDown(KeyDownEvent event) {
      if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
        GWT.log("Selection changed to (enter) " + getView().getSelectedVariableName() + ", handler: " + this);
      }
    }

  }

  class RepeatableClickHandler implements ValueChangeHandler<Boolean> {

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
      boolean enabled = event.getValue();
      getView().setEnabledOccurenceGroup(enabled);
      if(!enabled) {
        getView().clearOccurrenceGroup();
      }
    }

  }

  class DetailTabsBeforeSelectionHandler implements BeforeSelectionHandler<Integer> {

    @Override
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
      switch(Tabs.values()[event.getItem()]) {
      case CATEGORIES:
        categoriesPresenter.refreshDisplay();
        break;
      case ATTRIBUTES:
        attributesPresenter.refreshDisplay();
        break;
      case SUMMARY:
        summaryPresenter.refreshDisplay();
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

      currentVariableDto = getView().getVariableDto(evaluateScriptPresenter.getScript());
      if(isEmptyVariable()) {
        // This view has no variables. Clear the variable list and save.
        variableListViewDto.clearVariablesArray();
        getEventBus().fireEvent(new ViewSaveRequiredEvent(viewDto));
      } else {
        // Validate current variable and save to variable list.
        if(validate()) {
          updateViewDto();
          refreshVariableSuggestions();
        }
      }
    }

    private void updateViewDto() {
      updateCategories();
      updateAttributes();
      if(!addVariable) {
        updateVariable();
      } else {
        addVariable();
      }
      displayedVariableName = currentVariableDto.getName(); // Must note this before form is refreshed.
      getEventBus().fireEvent(new ViewSaveRequiredEvent(viewDto));
    }

    private void updateCategories() {
      // Set variable categories, if they exist.
      if(categoriesPresenter.getVariableDto().getCategoriesArray() != null) {
        currentVariableDto.setCategoriesArray(categoriesPresenter.getVariableDto().getCategoriesArray());
      }
    }

    private void updateAttributes() {
      AttributeDto currentVariableScriptAttribute = Variables.getScriptAttribute(currentVariableDto);
      AttributeDto existingVariableScriptAttribute = Variables.getScriptAttribute(attributesPresenter.getVariableDto());
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

    private boolean isEmptyVariable() {
      return currentVariableDto.getName().equals("") && variableListViewDto.getVariablesArray().length() == 0;
    }

    private void updateVariable() {
      variableListViewDto.getVariablesArray().set(getVariableIndex(displayedVariableName), currentVariableDto);
    }

    private void addVariable() {
      variableListViewDto.getVariablesArray().push(currentVariableDto);
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
      firstTableInViewParts = viewDto.getFromArray().get(0).split("\\.");
      setEmptyDerivedVariable();
      for(int i = 0; i < viewDto.getFromArray().length(); i++) {
        String[] tableParts = viewDto.getFromArray().get(i).split("\\.");
        ResourceRequestBuilderFactory.<VariableDto> newBuilder()
        /**/.forResource("/datasource/" + tableParts[0] + "/table/" + tableParts[1] + "/variable/" + newDerivedVariableName)
        /**/.get()
        /**/.withCallback(new ResourceCallback<VariableDto>() {
          @Override
          public void onResource(Response response, VariableDto variableDto) {
            variableDto.setName(translations.copyOf() + variableDto.getName());
            getEventBus().fireEvent(new DerivedVariableConfigurationRequiredEvent(variableDto));
            setButtonsWhenAddingVariable();
          }
        })
        /**/.withCallback(Response.SC_NOT_FOUND, doNothingResponseCodeCallback())
        /**/.send();
      }
    }

    private void setEmptyDerivedVariable() {
      ResourceRequestBuilderFactory.<TableDto> newBuilder()
      /**/.forResource("/datasource/" + firstTableInViewParts[0] + "/table/" + firstTableInViewParts[1])
      /**/.get()
      /**/.withCallback(new ResourceCallback<TableDto>() {
        @Override
        public void onResource(Response response, TableDto firstTableDto) {
          VariableDto variableDto = createEmptyDerivedVariable(firstTableDto.getEntityType());
          getEventBus().fireEvent(new DerivedVariableConfigurationRequiredEvent(variableDto));
          setButtonsWhenAddingVariable();
        }
      })
      /**/.send();
    }

    private ResponseCodeCallback doNothingResponseCodeCallback() {
      return new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          // Do nothing.
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
          getEventBus().fireEvent(new DerivedVariableConfigurationRequiredEvent(variableDto));
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
      variableDto.setValueType("text");
      return variableDto;
    }

    private void setButtonsWhenAddingVariable() {
      getView().saveChangesEnabled(true);
      getView().removeButtonEnabled(false);
      getView().addButtonEnabled(false);
      getView().navigationEnabled(false);
      getEventBus().fireEvent(new ViewSavePendingEvent());
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
      getEventBus().fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, "deleteVariableTitle", "confirmVariableDelete"));
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
    VariableDto variableToDelete = getView().getVariableDto(evaluateScriptPresenter.getScript());
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
      getView().setSelectedVariableName(null, null, getNextVariableName());
      formClear();
      formEnabled(false);
    }
    getView().saveChangesEnabled(true);
    getView().addButtonEnabled(false);
    getView().navigationEnabled(false);
    getEventBus().fireEvent(new ViewSavePendingEvent());
  }

  private void updateSummaryLink(boolean hasChanges) {

    // TODO: this link should be built from VariableDto.getLink() but it's not initialised in ViewResource
    StringBuilder summaryLink = new StringBuilder("/datasource/" + viewDto.getDatasourceName() + "/table/" + viewDto.getName() + "/variable/" + this.displayedVariableName + "/summary");
    if(hasChanges) {

      // TODO: it would probably be simpler to add a VariableDto to the body instead of putting everything on the URL
      summaryLink = new StringBuilder("/datasource/" + viewDto.getDatasourceName() + "/view/" + viewDto.getName() + "/from/variable/_transient/summary?valueType=" + getView().getVariableDto(evaluateScriptPresenter.getScript()).getValueType() + "&script=" + URL.encodeQueryString(evaluateScriptPresenter.getScript()));

      if(categoriesPresenter.getVariableDto().getCategoriesArray() != null) {
        JsArray<CategoryDto> cats = categoriesPresenter.getVariableDto().getCategoriesArray();
        for(int i = 0; i < cats.length(); i++) {
          summaryLink.append("&category=" + URL.encodeQueryString(cats.get(i).getName()));
        }
      }
    }
    getEventBus().fireEvent(new SummaryRequiredEvent(summaryLink.toString()));

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
      getEventBus().fireEvent(new ViewSavePendingEvent());
      getView().saveChangesEnabled(true);
      getView().addButtonEnabled(false);
      getView().navigationEnabled(false);
      getView().removeButtonEnabled(false);

      updateSummaryLink(true);
      if(isTabSelected(Tabs.SUMMARY)) {
        summaryPresenter.refreshDisplay();
      }
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
      getView().saveChangesEnabled(false);
      getView().addButtonEnabled(true);
      getView().navigationEnabled(true);
      updateSelectedVariableName();
      if(getVariableList().size() > 0) getView().removeButtonEnabled(true);
      addDerivedVariableDialogPresenter.refreshVariableNameSuggestions(viewDto);
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
        if(getView().getName().getText().equals(displayedVariableName)) return false;
      }
      for(VariableDto variableDto : getVariableList()) {
        if(getView().getName().getText().equals(variableDto.getName())) return true;
      }
      return false;
    }

  }

  /**
   * Enables or disables the variable list form. The "Add" variable button is always enabled regardless of state.
   */
  private void formEnabled(boolean enabled) {
    getView().formEnable(enabled);
    evaluateScriptPresenter.getView().formEnable(enabled);
    categoriesPresenter.getDisplay().formEnable(enabled);
    attributesPresenter.getDisplay().formEnable(enabled);
  }

  /**
   * Clears the variable list form.
   */
  private void formClear() {
    getView().formClear();
    evaluateScriptPresenter.getView().formClear();
    categoriesPresenter.formClear();
    attributesPresenter.formClear();
  }
}