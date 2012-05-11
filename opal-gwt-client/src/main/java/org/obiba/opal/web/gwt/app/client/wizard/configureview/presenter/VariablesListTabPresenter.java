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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributeUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoryUpdateEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.DerivedVariableConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.UpdateType;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.VariableAddRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter.ScriptEvaluationCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Variables tab used to specify a view's variables by defining each variable using a Javascript expression.
 */
public class VariablesListTabPresenter extends PresenterWidget<VariablesListTabPresenter.Display> {

  private enum Tabs {
    /* These should be in the same order as in the UI */
    SCRIPT, CATEGORIES, ATTRIBUTES, OPTIONS, SUMMARY
  }

  private static final Translations translations = GWT.create(Translations.class);

  @Inject
  private Provider<CategoryDialogPresenter> categoryDialogPresenterProvider;

  @Inject
  private Provider<AttributeDialogPresenter> attributeDialogPresenterProvider;

  private final SummaryTabPresenter summaryPresenter;

  private final AddDerivedVariableDialogPresenter addDerivedVariableDialogPresenter;

  /**
   * Widget for entering, and testing, the "select" script.
   */
  private final EvaluateScriptPresenter evaluateScriptPresenter;

  private final ScriptEvaluationPresenter scriptEvaluationPresenter;

  private ViewDto viewDto;

  /**
   * The variable currently displayed.
   */
  private VariableDto currentVariable;

  private boolean addVariable;

  private final Collection<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Runnable actionRequiringConfirmation;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public VariablesListTabPresenter(Display display, EventBus eventBus, SummaryTabPresenter summaryPresenter,
      AddDerivedVariableDialogPresenter addDerivedVariableDialogPresenter,
      EvaluateScriptPresenter evaluateScriptPresenter, ScriptEvaluationPresenter scriptEvaluationPresenter) {
    super(eventBus, display);
    this.summaryPresenter = summaryPresenter;
    this.addDerivedVariableDialogPresenter = addDerivedVariableDialogPresenter;
    this.evaluateScriptPresenter = evaluateScriptPresenter;
    this.scriptEvaluationPresenter = scriptEvaluationPresenter;
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Test, evaluateScriptPresenter);

    setScriptEvaluationOnSaveCallback();

    summaryPresenter.bind();
    getView().addSummaryTabWidget(summaryPresenter.getDisplay().asWidget());

    addEventHandlers();
    addValidators();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    addDerivedVariableDialogPresenter.unbind();
    summaryPresenter.unbind();
  }

  public void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;

    viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));

    VariableListViewDto variableListDto = (VariableListViewDto) viewDto
        .getExtension(VariableListViewDto.ViewDtoExtensions.view);
    variableListDto.setVariablesArray(JsArrays.toSafeArray(variableListDto.getVariablesArray()));

    evaluateScriptPresenter.setTable(viewDto);

    getView().saveChangesEnabled(false);

    // Always show the Categories detail tab first.
    getView().displayDetailTab(0);

    initDisplayComponents();
  }

  private void initDisplayComponents() {
    getView().addButtonEnabled(true);
    getView().navigationEnabled(true);

    currentVariable = getVariableList().isEmpty() ? null : getVariableList().get(0);
    refreshVariableSuggestions();

    if(getVariableList().isEmpty()) {
      // Clear variable selection.
      getView().setSelectedVariableName(null, null, getNextVariable());
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
    VariableListViewDto variableListDto = (VariableListViewDto) viewDto
        .getExtension(VariableListViewDto.ViewDtoExtensions.view);
    return JsArrays.toList(variableListDto.getVariablesArray());
  }

  private void updateSelectedVariableName() {
    if(!getVariableList().isEmpty()) {
      getView().setSelectedVariableName(currentVariable, getPreviousVariable(), getNextVariable());
      getView().renderCategoryRows(currentVariable.getCategoriesArray());
      getView().renderAttributeRows(currentVariable.getAttributesArray());
      getEventBus().fireEvent(new DerivedVariableConfigurationRequiredEvent(currentVariable));
    }
  }

  private VariableDto getPreviousVariable() {
    if(getVariableList().isEmpty()) return null;
    if(currentVariable == null) return getVariableList().get(0);
    int index = getVariableIndex(currentVariable) - 1;
    return index >= 0 ? getVariableList().get(index) : null;
  }

  private VariableDto getNextVariable() {
    if(getVariableList().isEmpty()) return null;
    if(currentVariable == null) return getVariableList().get(0);
    int index = getVariableIndex(currentVariable) + 1;
    return index < getVariableList().size() ? getVariableList().get(index) : null;
  }

  private void refreshVariableSuggestions() {
    getView().clearVariableNameSuggestions();
    for(VariableDto variableDto : getVariableList()) {
      getView().addVariableNameSuggestion(variableDto.getName());
    }
  }

  @SuppressWarnings({"PMD.NcssMethodCount", "OverlyLongMethod"})
  private void addEventHandlers() {
    registerHandler(getEventBus()
        .addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));

    registerHandler(getView().addPreviousVariableNameClickHandler(new PreviousVariableClickHandler()));
    registerHandler(getView().addNextVariableNameClickHandler(new NextVariableClickHandler()));
    registerHandler(getView().addVariableNameSelectedHandler(new VariableNameSelectedHandler()));

    registerHandler(getView().addRepeatableValueChangeHandler(new RepeatableClickHandler()));
    registerHandler(getView().addSaveChangesClickHandler(new SaveChangesClickHandler()));

    registerHandler(getView().addAddVariableClickHandler(new AddVariableClickHandler()));
    registerHandler(getView().addRemoveVariableClickHandler(new RemoveVariableClickHandler()));

    // categories handlers
    registerHandler(getView().addAddCategoryHandler(new AddCategoryHandler()));
    registerHandler(getView().addAddAttributeHandler(new AddAttributeHandler()));

    // attributes handlers
    getView().setEditAttributeActionHandler(new EditAttributeActionHandler());
    DeleteAttributeActionHandler deleteAttributeActionHandler = new DeleteAttributeActionHandler();
    getView().setDeleteAttributeActionHandler(deleteAttributeActionHandler);
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), deleteAttributeActionHandler));

    registerHandler(getView().getDetailTabs().addBeforeSelectionHandler(new DetailTabsBeforeSelectionHandler()));
    registerHandler(getEventBus().addHandler(VariableAddRequiredEvent.getType(), new VariableAddRequiredHandler()));
    registerHandler(getEventBus().addHandler(DerivedVariableConfigurationRequiredEvent.getType(),
        new DerivedVariableConfigurationRequiredHandler()));
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    registerHandler(getEventBus().addHandler(ViewSavedEvent.getType(), new ViewSavedHandler()));

    registerFormChangedHandler();
  }

  private void registerFormChangedHandler() {
    FormChangedHandler formChangedHandler = new FormChangedHandler();

    registerHandler(getView().addNameChangedHandler(formChangedHandler));
    registerHandler(getView().addValueTypeChangedHandler(formChangedHandler));
    registerHandler(evaluateScriptPresenter.getView().addScriptChangeHandler(formChangedHandler));
    registerHandler(getView().addRepeatableValueChangeHandler(formChangedHandler));
    registerHandler(getView().addOccurrenceGroupChangedHandler(formChangedHandler));
    registerHandler(getView().addUnitChangedHandler(formChangedHandler));
    registerHandler(getView().addMimeTypeChangedHandler(formChangedHandler));
    registerHandler(getEventBus().addHandler(CategoryUpdateEvent.getType(), formChangedHandler));
    registerHandler(getEventBus().addHandler(AttributeUpdateEvent.getType(), new AttributeUpdateEventHandler()));
  }

  private void addValidators() {
    validators.add(new ConditionalValidator(getView().getRepeatable(),
        new RequiredTextValidator(getView().getOccurrenceGroup(), "OccurrenceGroupIsRequired")));
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
    }
    return true;
  }

  private int getVariableIndex(VariableDto variableDto) {
    for(int i = 0; i < getVariableList().size(); i++) {
      if(getVariableList().get(i).getName().equals(variableDto.getName())) {
        return i;
      }
    }
    return -1;
  }

  private boolean isTabSelected(Tabs tab) {
    return getView().getSelectedTab() == tab.ordinal();
  }

  private void prepareCategoryDialog(CategoryDto categoryDto) {
    CategoryDialogPresenter categoryDialogPresenter = categoryDialogPresenterProvider.get();
    categoryDialogPresenter.bind();
    categoryDialogPresenter.setViewDto(viewDto);
    categoryDialogPresenter.setCategories(currentVariable.getCategoriesArray());
    categoryDialogPresenter.revealDisplay();
  }

  private void prepareAttributeDialog(AttributeDto attributeDto) {
    AttributeDialogPresenter attributeDialogPresenter = attributeDialogPresenterProvider.get();
    attributeDialogPresenter.bind();
    attributeDialogPresenter.setViewDto(viewDto);
    attributeDialogPresenter.setAttribute(attributeDto);
    attributeDialogPresenter.setAttributes(currentVariable.getAttributesArray());
    attributeDialogPresenter.revealDisplay();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    void setEditAttributeActionHandler(ActionHandler<AttributeDto> editAttributeActionHandler);

    void setDeleteAttributeActionHandler(ActionHandler<AttributeDto> deleteAttributeActionHandler);

    enum Slots {
      Test
    }

    void renderCategoryRows(JsArray<CategoryDto> rows);

    void renderAttributeRows(JsArray<AttributeDto> rows);

    HasBeforeSelectionHandlers<Integer> getDetailTabs();

    void displayDetailTab(int tabNumber);

    int getSelectedTab();

    void addSummaryTabWidget(Widget summaryTabWidget);

    void clearVariableNameSuggestions();

    void addVariableNameSuggestion(String variableName);

    void setSelectedVariableName(VariableDto variable, VariableDto previousVariable, VariableDto nextVariable);

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

    HandlerRegistration addAddCategoryHandler(ClickHandler addCategoryHandler);

    HandlerRegistration addAddAttributeHandler(ClickHandler addAttributeHandler);

    void setEnabledOccurrenceGroup(Boolean enabled);

    HasValue<Boolean> getRepeatable();

    void clearOccurrenceGroup();

    HasText getOccurrenceGroup();

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
      setViewDto(event.getView());
      if(event.getVariable() != null) {
        updateAndDisplayVariable(event.getVariable());
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
      String script = VariableDtos.getScript(variableDto);
      evaluateScriptPresenter.setScript(script == null ? "" : script);
      evaluateScriptPresenter.setRepeatable(variableDto.getIsRepeatable());
    }
  }

  class PreviousVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(getPreviousVariable() != null) {
        currentVariable = getPreviousVariable();
        updateSelectedVariableName();
      }
    }
  }

  class NextVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(getNextVariable() != null) {
        currentVariable = getNextVariable();
        updateSelectedVariableName();
      }
    }

  }

  class VariableNameSelectedHandler implements SelectionHandler<Suggestion> {

    @Override
    public void onSelection(SelectionEvent<Suggestion> event) {
      String name = event.getSelectedItem().getReplacementString();
      currentVariable = findByName(name);
      updateSelectedVariableName();
    }

    private VariableDto findByName(String name) {
      for(VariableDto variable : getVariableList()) {
        if(variable.getName().equals(name)) return variable;
      }
      return null;
    }
  }

  class AddVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      addDerivedVariableDialogPresenter.getView().getVariableName().setText("");
      addToPopupSlot(addDerivedVariableDialogPresenter);
    }
  }

  class AddCategoryHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      prepareCategoryDialog(null);
    }
  }

  class AddAttributeHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      prepareAttributeDialog(null);
    }
  }

  class EditAttributeActionHandler implements ActionHandler<AttributeDto> {

    @Override
    public void doAction(AttributeDto attributeDto, String actionName) {
      if(ActionsColumn.EDIT_ACTION.equals(actionName)) {
        prepareAttributeDialog(attributeDto);
      }
    }
  }

  class DeleteAttributeActionHandler implements ActionHandler<AttributeDto>, ConfirmationEvent.Handler {

    private Runnable runDelete;

    @Override
    public void doAction(final AttributeDto deletedAttribute, String actionName) {
      if(!ActionsColumn.DELETE_ACTION.equals(actionName)) return;
      runDelete = new Runnable() {

        @Override
        public void run() {
          @SuppressWarnings("unchecked")
          JsArray<AttributeDto> deletedAttributes = (JsArray<AttributeDto>) JsArray.createArray();
          deletedAttributes.push(deletedAttribute);
          fireEvent(new AttributeUpdateEvent(deletedAttributes, UpdateType.DELETE));
        }

      };
      fireEvent(ConfirmationRequiredEvent
          .createWithMessages(runDelete, translations.deleteAttribute(), translations.confirmDeleteAttribute()));
    }

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(event.getSource() == runDelete) {
        runDelete.run();
      }
    }
  }

  class RepeatableClickHandler implements ValueChangeHandler<Boolean> {

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
      boolean enabled = event.getValue();
      getView().setEnabledOccurrenceGroup(enabled);
      if(!enabled) {
        getView().clearOccurrenceGroup();
      }
      evaluateScriptPresenter.setRepeatable(enabled);
    }

  }

  class DetailTabsBeforeSelectionHandler implements BeforeSelectionHandler<Integer> {

    @Override
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
      switch(Tabs.values()[event.getItem()]) {
        case SUMMARY:
          summaryPresenter.refreshDisplay();
          break;
      }
    }
  }

  class SaveChangesClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      VariableListViewDto variableListViewDto = (VariableListViewDto) viewDto
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);
      variableListViewDto.setVariablesArray(JsArrays.toSafeArray(variableListViewDto.getVariablesArray()));

      final VariableDto currentVariableDto = getView().getVariableDto(evaluateScriptPresenter.getScript());

      UriBuilder ub = UriBuilder.create()
          .segment("datasource", viewDto.getDatasourceName(), "table", viewDto.getName());
      ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<TableDto>() {
            @Override
            public void onResource(Response response, TableDto resource) {
              scriptEvaluationPresenter.setOriginalTable(resource);
              scriptEvaluationPresenter.setOriginalVariable(currentVariableDto);
            }
          }).send();
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
        UriBuilder ub = UriBuilder.create()
            .segment("datasource", tableParts[0], "table", tableParts[1], "variable", newDerivedVariableName);
        ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(ub.build()).get()
            .withCallback(new ResourceCallback<VariableDto>() {
              @Override
              public void onResource(Response response, VariableDto variableDto) {
                variableDto.setName(translations.copyOf() + variableDto.getName());
                getEventBus().fireEvent(new DerivedVariableConfigurationRequiredEvent(variableDto));
                setButtonsWhenAddingVariable();
              }
            }).withCallback(Response.SC_NOT_FOUND, doNothingResponseCodeCallback()).send();
      }
    }

    private void setEmptyDerivedVariable() {
      UriBuilder ub = UriBuilder.create()
          .segment("datasource", firstTableInViewParts[0], "table", firstTableInViewParts[1]);
      ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<TableDto>() {
            @Override
            public void onResource(Response response, TableDto firstTableDto) {
              VariableDto variableDto = createEmptyDerivedVariable(firstTableDto.getEntityType());
              getEventBus().fireEvent(new DerivedVariableConfigurationRequiredEvent(variableDto));
              setButtonsWhenAddingVariable();
            }

            private VariableDto createEmptyDerivedVariable(String entityType) {
              VariableDto variableDto = VariableDto.create();
              variableDto.setName(newDerivedVariableName);
              variableDto.setEntityType(entityType);
              variableDto.setValueType("text");
              return variableDto;
            }
          }).send();
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
      UriBuilder ub = UriBuilder.create().segment("datasource", viewDto.getDatasourceName(), "view", viewDto.getName());
      ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<ViewDto>() {

            List<VariableDto> variablesList;

            @Override
            public void onResource(Response response, ViewDto viewDto) {
              VariableListViewDto variableListDto = (VariableListViewDto) viewDto
                  .getExtension(VariableListViewDto.ViewDtoExtensions.view);
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
        @Override
        public void run() {
          deleteCurrentVariable();
        }

        private void deleteCurrentVariable() {
          VariableDto nextVariable = variableToDisplayAfterCurrentVariableDeleted();
          VariableListViewDto variableListViewDto = (VariableListViewDto) viewDto
              .getExtension(VariableListViewDto.ViewDtoExtensions.view);
          VariableDto variableToDelete = getView().getVariableDto(evaluateScriptPresenter.getScript());
          @SuppressWarnings("unchecked")
          JsArray<VariableDto> newVariables = (JsArray<VariableDto>) JsArray.createArray();
          for(int i = 0; i < variableListViewDto.getVariablesArray().length(); i++) {
            if(!variableListViewDto.getVariablesArray().get(i).getName().equals(variableToDelete.getName())) {
              newVariables.push(variableListViewDto.getVariablesArray().get(i));
            }
          }
          variableListViewDto.setVariablesArray(newVariables); // Updates the viewDto.
          updateAndDisplayVariable(nextVariable);
        }

        private VariableDto variableToDisplayAfterCurrentVariableDeleted() {
          VariableDto nextVariable = getNextVariable();
          if(nextVariable != null) return nextVariable;
          VariableDto previousVariable = getPreviousVariable();
          if(previousVariable != null) return previousVariable;
          return null;
        }

      };
      getEventBus().fireEvent(ConfirmationRequiredEvent
          .createWithKeys(actionRequiringConfirmation, "deleteVariableTitle", "confirmVariableDelete"));
    }

  }

  private class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) && event
          .isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private void updateAndDisplayVariable(VariableDto nextVariable) {
    currentVariable = nextVariable;
    if(currentVariable == null) {
      getView().setSelectedVariableName(null, null, getNextVariable());
      formClear();
      formEnabled(false);
    } else {
      updateSelectedVariableName();
    }
    getView().saveChangesEnabled(true);
    getView().addButtonEnabled(false);
    getView().navigationEnabled(false);
    getEventBus().fireEvent(new ViewSavePendingEvent());
  }

  private void updateSummaryLink(boolean hasChanges) {

    // TODO: this link should be built from VariableDto.getLink() but it's not initialised in ViewResource
    StringBuilder summaryLink = new StringBuilder(
        "/datasource/" + viewDto.getDatasourceName() + "/table/" + viewDto.getName() + "/variable/" + currentVariable
            .getName() + "/summary");
    if(hasChanges) {

      // TODO: it would probably be simpler to add a VariableDto to the body instead of putting everything on the URL
      summaryLink = new StringBuilder("/datasource/" + viewDto.getDatasourceName() + "/view/" + viewDto
          .getName() + "/from/variable/_transient/summary?valueType=" + getView()
          .getVariableDto(evaluateScriptPresenter.getScript()).getValueType() + "&script=" + URL
          .encodeQueryString(evaluateScriptPresenter.getScript()));

      if(currentVariable.getCategoriesArray() != null) {
        JsArray<CategoryDto> cats = currentVariable.getCategoriesArray();
        for(int i = 0; i < cats.length(); i++) {
          summaryLink.append("&category=").append(URL.encodeQueryString(cats.get(i).getName()));
        }
      }
    }
    getEventBus().fireEvent(new SummaryRequiredEvent(summaryLink.toString()));

  }

  class FormChangedHandler implements ChangeHandler, ValueChangeHandler<Boolean>, CategoryUpdateEvent.Handler {

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
    public void onCategoryUpdate(CategoryUpdateEvent event) {
      formChange();
    }

  }

  class AttributeUpdateEventHandler implements AttributeUpdateEvent.Handler {

    @Override
    public void onAttributeUpdate(AttributeUpdateEvent event) {
      switch(event.getUpdateType()) {
        case ADD:
          addAttribute(event);
          break;
        case EDIT:
          replaceAttribute(event);
          break;
        case DELETE:
          deleteAttributes(event.getAttributes());
          break;
      }
      getView().renderAttributeRows(currentVariable.getAttributesArray());
    }

    @SuppressWarnings("unchecked")
    private void addAttribute(AttributeUpdateEvent event) {
      if(currentVariable.getAttributesArray() == null) {
        currentVariable.setAttributesArray((JsArray<AttributeDto>) JsArray.createArray());
      }
      for(int i = 0; i < event.getAttributes().length(); i++) {
        AttributeDto newAttribute = event.getAttributes().get(i);
        currentVariable.getAttributesArray().push(newAttribute);
      }
    }

    private void replaceAttribute(AttributeUpdateEvent event) {
      deleteAttribute(event.getOriginalNamespace(), event.getOriginalName());
      for(int i = 0; i < event.getAttributes().length(); i++) {
        AttributeDto updatedAttribute = event.getAttributes().get(i);
        currentVariable.getAttributesArray().push(updatedAttribute);
      }
    }

    private void deleteAttributes(JsArray<AttributeDto> attributeDtos) {
      for(int i = 0; i < attributeDtos.length(); i++) {
        AttributeDto attributeDto = attributeDtos.get(i);
        deleteAttribute(attributeDto.getNamespace(), attributeDto.getName());
      }
    }

    private void deleteAttribute(String namespace, String name) {
      String safeNamespace = Strings.nullToEmpty(namespace);
      String safeName = Strings.nullToEmpty(name);
      @SuppressWarnings("unchecked")
      JsArray<AttributeDto> result = (JsArray<AttributeDto>) JsArray.createArray();
      for(int i = 0; i < currentVariable.getAttributesArray().length(); i++) {
        AttributeDto attribute = currentVariable.getAttributesArray().get(i);
        if(!(attribute.getNamespace().equals(safeNamespace) && attribute.getName().equals(safeName))) {
          result.push(attribute);
        }
      }
      currentVariable.setAttributesArray(result);
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
      if(currentVariable != null && getView().getName().getText().equals(currentVariable.getName())) {
        // Edits can have the same name.
        return false;
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

  }

  /**
   * Clears the variable list form.
   */
  private void formClear() {
    getView().formClear();
    evaluateScriptPresenter.getView().formClear();
  }

  private void setScriptEvaluationOnSaveCallback() {
    scriptEvaluationPresenter.setScriptEvaluationCallback(new ScriptEvaluationCallback() {

      private VariableListViewDto variableListViewDto;

      private VariableDto currentVariableDto;

      @Override
      public void onSuccess(VariableDto variable) {
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

      @Override
      public void onFailure(VariableDto variable) {
      }

      private void updateViewDto() {
        updateCategories();
        updateAttributes();
        if(addVariable) {
          addVariable();
        } else {
          updateVariable();
        }
        currentVariable = currentVariableDto; // Must do this before form is refreshed.
        getEventBus().fireEvent(new ViewSaveRequiredEvent(viewDto));
      }

      private void updateCategories() {
        // Set variable categories, if they exist.
        if(currentVariable.getCategoriesArray() != null) {
          currentVariableDto.setCategoriesArray(currentVariable.getCategoriesArray());
        }
      }

      private void updateAttributes() {
        String currentVariableScript = VariableDtos.getScript(currentVariableDto);
        currentVariableDto.setAttributesArray(currentVariable.getAttributesArray());
        VariableDtos.setScript(currentVariableDto, currentVariableScript);
      }

      private boolean isEmptyVariable() {
        return "".equals(currentVariableDto.getName()) && variableListViewDto.getVariablesArray().length() == 0;
      }

      private void updateVariable() {
        variableListViewDto.getVariablesArray().set(getVariableIndex(currentVariable), currentVariableDto);
      }

      private void addVariable() {
        variableListViewDto.getVariablesArray().push(currentVariableDto);
      }
    });
  }
}
