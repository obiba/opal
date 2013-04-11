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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ScriptEditorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.AttributesUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.CategoriesUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.DerivedVariableConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.VariableAddRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavePendingEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSaveRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallbacks;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_OK;

/**
 * Variables tab used to specify a view's variables by defining each variable using a Javascript expression.
 */
@SuppressWarnings("OverlyCoupledClass")
public class VariablesListTabPresenter extends PresenterWidget<VariablesListTabPresenter.Display> {

  private enum Tabs {
    /* These should be in the same order as in the UI */
    SCRIPT, CATEGORIES, ATTRIBUTES, OPTIONS, SUMMARY
  }

  private static final Translations translations = GWT.create(Translations.class);

  private final SummaryTabPresenter summaryPresenter;

  private final AddDerivedVariableDialogPresenter addDerivedVariableDialogPresenter;

  /**
   * Widget for entering, and testing, the "select" script.
   */
  private final ScriptEditorPresenter scriptEditorPresenter;

  private final CategoriesPresenter categoriesPresenter;

  private final AttributesPresenter attributesPresenter;

  private ViewDto viewDto;

  /**
   * The variable currently displayed.
   */
  private VariableDto currentVariable;

  private boolean addVariable;

  private final Collection<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Runnable actionRequiringConfirmation;

  @Inject
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters" })
  public VariablesListTabPresenter(Display display, EventBus eventBus, SummaryTabPresenter summaryPresenter,
      AddDerivedVariableDialogPresenter addDerivedVariableDialogPresenter, ScriptEditorPresenter scriptEditorPresenter,
      CategoriesPresenter categoriesPresenter, AttributesPresenter attributesPresenter) {
    super(eventBus, display);
    this.summaryPresenter = summaryPresenter;
    this.addDerivedVariableDialogPresenter = addDerivedVariableDialogPresenter;
    this.scriptEditorPresenter = scriptEditorPresenter;
    this.categoriesPresenter = categoriesPresenter;
    this.attributesPresenter = attributesPresenter;
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Test, scriptEditorPresenter);
    setInSlot(Display.Slots.Categories, categoriesPresenter);
    setInSlot(Display.Slots.Attributes, attributesPresenter);

    summaryPresenter.bind();
    getView().addSummaryTabWidget(summaryPresenter.getDisplay().asWidget());

    registerEventHandlers();
    addValidators();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    addDerivedVariableDialogPresenter.unbind();
    summaryPresenter.unbind();
  }

  void setCurrentVariable(@Nullable VariableDto currentVariable) {
    this.currentVariable = currentVariable;
    categoriesPresenter.setCurrentVariable(currentVariable);
    attributesPresenter.setCurrentVariable(currentVariable);
  }

  void setViewDto(ViewDto viewDto) {
    this.viewDto = viewDto;
    categoriesPresenter.setViewDto(viewDto);
    attributesPresenter.setViewDto(viewDto);

    viewDto.setFromArray(JsArrays.toSafeArray(viewDto.getFromArray()));

    VariableListViewDto variableListDto = (VariableListViewDto) viewDto
        .getExtension(VariableListViewDto.ViewDtoExtensions.view);
    variableListDto.setVariablesArray(JsArrays.toSafeArray(variableListDto.getVariablesArray()));

    scriptEditorPresenter.setView(viewDto);

    getView().saveChangesEnabled(false);

    // Always show the Categories detail tab first.
    getView().displayDetailTab(0);

    initDisplayComponents();
  }

  private void initDisplayComponents() {
    getView().addButtonEnabled(true);
    getView().navigationEnabled(true);

    setCurrentVariable(getVariableList().isEmpty() ? null : getVariableList().get(0));
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
      categoriesPresenter.renderCategories();
      attributesPresenter.renderAttributes();
      getEventBus().fireEvent(new DerivedVariableConfigurationRequiredEvent(currentVariable));
    }
  }

  @Nullable
  private VariableDto getPreviousVariable() {
    if(getVariableList().isEmpty()) return null;
    if(currentVariable == null) return getVariableList().get(0);
    int index = getVariableIndex(currentVariable) - 1;
    return index >= 0 ? getVariableList().get(index) : null;
  }

  @Nullable
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

  private void registerEventHandlers() {
    registerNavigationEventHandlers();

    registerHandler(getEventBus()
        .addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredEventHandler()));

    registerHandler(getView().addRepeatableValueChangeHandler(new RepeatableClickHandler()));
    registerHandler(getView().addSaveChangesClickHandler(new SaveChangesClickHandler()));

    registerHandler(getView().addAddVariableClickHandler(new AddVariableClickHandler()));
    registerHandler(getView().addRemoveVariableClickHandler(new RemoveVariableClickHandler()));

    registerHandler(getView().getDetailTabs().addBeforeSelectionHandler(new DetailTabsBeforeSelectionHandler()));
    registerHandler(getEventBus().addHandler(VariableAddRequiredEvent.getType(), new VariableAddRequiredHandler()));
    registerHandler(getEventBus().addHandler(DerivedVariableConfigurationRequiredEvent.getType(),
        new DerivedVariableConfigurationRequiredHandler()));
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    registerHandler(getEventBus().addHandler(ViewSavedEvent.getType(), new ViewSavedHandler()));

    registerFormChangedHandler();
  }

  private void registerNavigationEventHandlers() {
    registerHandler(getView().addPreviousVariableNameClickHandler(new PreviousVariableClickHandler()));
    registerHandler(getView().addNextVariableNameClickHandler(new NextVariableClickHandler()));
    registerHandler(getView().addVariableNameSelectedHandler(new VariableNameSelectedHandler()));
  }

  private void registerFormChangedHandler() {
    FormChangedHandler formChangedHandler = new FormChangedHandler();

    registerHandler(getView().addNameChangedHandler(formChangedHandler));
    registerHandler(getView().addValueTypeChangedHandler(formChangedHandler));
    registerHandler(scriptEditorPresenter.getView().addScriptChangeHandler(formChangedHandler));
    registerHandler(getView().addRepeatableValueChangeHandler(formChangedHandler));
    registerHandler(getView().addOccurrenceGroupChangedHandler(formChangedHandler));
    registerHandler(getView().addUnitChangedHandler(formChangedHandler));
    registerHandler(getView().addMimeTypeChangedHandler(formChangedHandler));

    registerHandler(getEventBus().addHandler(CategoriesUpdatedEvent.getType(), formChangedHandler));
    registerHandler(getEventBus().addHandler(AttributesUpdatedEvent.getType(), formChangedHandler));
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

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    enum Slots {
      Test, Categories, Attributes
    }

    HasBeforeSelectionHandlers<Integer> getDetailTabs();

    void displayDetailTab(int tabNumber);

    int getSelectedTab();

    void addSummaryTabWidget(Widget summaryTabWidget);

    void clearVariableNameSuggestions();

    void addVariableNameSuggestion(String variableName);

    void setSelectedVariableName(@Nullable VariableDto variable, @Nullable VariableDto previousVariable,
        @Nullable VariableDto nextVariable);

    HandlerRegistration addPreviousVariableNameClickHandler(ClickHandler handler);

    HandlerRegistration addNextVariableNameClickHandler(ClickHandler handler);

    HandlerRegistration addVariableNameSelectedHandler(SelectionHandler<Suggestion> handler);

    HandlerRegistration addRepeatableValueChangeHandler(ValueChangeHandler<Boolean> handler);

    HandlerRegistration addSaveChangesClickHandler(ClickHandler handler);

    HandlerRegistration addAddVariableClickHandler(ClickHandler handler);

    HandlerRegistration addRemoveVariableClickHandler(ClickHandler handler);

    void setEnabledOccurrenceGroup(Boolean enabled);

    HasValue<Boolean> getRepeatable();

    void clearOccurrenceGroup();

    HasText getOccurrenceGroup();

    HasText getName();

    void setNewVariable(VariableDto variableDto);

    VariableDto getVariableDto(String script);

    void saveChangesEnabled(boolean enabled);

    void removeButtonEnabled(boolean enabled);

    void addButtonEnabled(boolean enabled);

    void navigationEnabled(boolean enabled);

    HandlerRegistration addNameChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addValueTypeChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addOccurrenceGroupChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addUnitChangedHandler(ChangeHandler changeHandler);

    HandlerRegistration addMimeTypeChangedHandler(ChangeHandler changeHandler);

    void formEnable(boolean enabled);

    void formClear();

    void setInProgress(boolean inProgress);

  }

  private class ViewConfigurationRequiredEventHandler implements ViewConfigurationRequiredEvent.Handler {

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
      setCurrentVariable(event.getVariable());
      formEnabled(true);
      getView().saveChangesEnabled(false);
      getView().setNewVariable(event.getVariable());
      categoriesPresenter.renderCategories();
      attributesPresenter.renderAttributes();
      setScript(event.getVariable());
      updateSummaryLink(false);
      if(isTabSelected(Tabs.SUMMARY)) {
        summaryPresenter.refreshDisplay();
      }
    }

    private void setScript(VariableDto variableDto) {
      String script = VariableDtos.getScript(variableDto);
      scriptEditorPresenter.setScript(Strings.nullToEmpty(script));
      scriptEditorPresenter.setRepeatable(variableDto.getIsRepeatable());
    }
  }

  private class PreviousVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(getPreviousVariable() != null) {
        setCurrentVariable(getPreviousVariable());
        updateSelectedVariableName();
      }
    }
  }

  private class NextVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(getNextVariable() != null) {
        setCurrentVariable(getNextVariable());
        updateSelectedVariableName();
      }
    }

  }

  class VariableNameSelectedHandler implements SelectionHandler<Suggestion> {

    @Override
    public void onSelection(SelectionEvent<Suggestion> event) {
      String name = event.getSelectedItem().getReplacementString();
      setCurrentVariable(findByName(name));
      updateSelectedVariableName();
    }

    @Nullable
    private VariableDto findByName(String name) {
      for(VariableDto variable : getVariableList()) {
        if(variable.getName().equals(name)) return variable;
      }
      return null;
    }
  }

  private class AddVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      addDerivedVariableDialogPresenter.getView().getVariableName().setText("");
      addToPopupSlot(addDerivedVariableDialogPresenter);
    }
  }

  private class RepeatableClickHandler implements ValueChangeHandler<Boolean> {

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> event) {
      boolean enabled = event.getValue();
      getView().setEnabledOccurrenceGroup(enabled);
      if(!enabled) {
        getView().clearOccurrenceGroup();
      }
      scriptEditorPresenter.setRepeatable(enabled);
    }

  }

  private class DetailTabsBeforeSelectionHandler implements BeforeSelectionHandler<Integer> {

    @Override
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
      switch(Tabs.values()[event.getItem()]) {
        case SUMMARY:
          summaryPresenter.refreshDisplay();
          break;
      }
    }
  }

  private class SaveChangesClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      VariableListViewDto variableListViewDto = (VariableListViewDto) viewDto
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);
      variableListViewDto.setVariablesArray(JsArrays.toSafeArray(variableListViewDto.getVariablesArray()));

      final VariableDto currentVariableDto = getCurrentVariableDto();

      getView().setInProgress(true);

      UriBuilder ub = UriBuilder.create()
          .segment("datasource", viewDto.getDatasourceName(), "table", viewDto.getName());
      ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<TableDto>() {
            @Override
            public void onResource(Response response, TableDto resource) {
              String script = VariableDtos.getScript(currentVariableDto);
              String uri = resource.getViewLink() + "/from/variable/_transient/_compile?valueType=" +
                  currentVariableDto.getValueType() + "&repeatable=" + currentVariableDto.getIsRepeatable();
              ResourceRequestBuilderFactory.newBuilder().forResource(uri) //
                  .post() //
                  .withFormBody("script", script) //
                  .withCallback(new ScriptEvaluationCallback(currentVariableDto), SC_BAD_REQUEST, SC_OK) //
                  .send();

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
            }) //
            .withCallback(Response.SC_NOT_FOUND, ResponseCodeCallbacks.NO_OP) //
            .send();
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

    private void newDerivedVariableIsCopyOfExistingDerivedVariable() {
      UriBuilder ub = UriBuilder.create().segment("datasource", viewDto.getDatasourceName(), "view", viewDto.getName());
      ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<ViewDto>() {

            private List<VariableDto> variablesList;

            @Override
            public void onResource(Response response, ViewDto dto) {
              VariableListViewDto variableListDto = (VariableListViewDto) dto
                  .getExtension(VariableListViewDto.ViewDtoExtensions.view);
              variablesList = JsArrays.toList(variableListDto.getVariablesArray());
              VariableDto variableDto = getVariableDto();
              variableDto.setName(translations.copyOf() + variableDto.getName());
              getEventBus().fireEvent(new DerivedVariableConfigurationRequiredEvent(variableDto));
              setButtonsWhenAddingVariable();
            }

            @Nullable
            private VariableDto getVariableDto() {
              for(VariableDto variableDto : variablesList) {
                if(newDerivedVariableName.equals(variableDto.getName())) {
                  return variableDto;
                }
              }
              return null;
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

  private class RemoveVariableClickHandler implements ClickHandler {

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
          VariableDto variableToDelete = getCurrentVariableDto();
          @SuppressWarnings("unchecked")
          JsArray<VariableDto> newVariables = (JsArray<VariableDto>) JsArray.createArray();
          for(int i = 0; i < variableListViewDto.getVariablesArray().length(); i++) {
            if(!variableListViewDto.getVariablesArray().get(i).getName().equals(variableToDelete.getName())) {
              newVariables.push(variableListViewDto.getVariablesArray().get(i));
            }
          }
          variableListViewDto.setVariablesArray(newVariables); // Updates the viewDto.
          updateAndDisplayVariable(nextVariable);

          getView().saveChangesEnabled(true);
        }

        @Nullable
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
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private void updateAndDisplayVariable(@Nullable VariableDto nextVariable) {
    setCurrentVariable(nextVariable);
    if(currentVariable == null) {
      getView().setSelectedVariableName(null, null, getNextVariable());
      formClear();
      formEnabled(false);
    } else {
      updateSelectedVariableName();
    }
    getView().addButtonEnabled(false);
    getView().navigationEnabled(false);
    getEventBus().fireEvent(new ViewSavePendingEvent());
  }

  private VariableDto getCurrentVariableDto() {
    return getView().getVariableDto(scriptEditorPresenter.getScript());
  }

  private void updateSummaryLink(boolean hasChanges) {

    String uri = null;
    String datasourceName = viewDto.getDatasourceName();
    String viewName = viewDto.getName();
    if(hasChanges) {
      // TODO: it would probably be simpler to add a VariableDto to the body instead of putting everything on the URL
      StringBuilder summaryLink = new StringBuilder("/datasource/" + datasourceName + "/view/" + viewName +
          "/from/variable/_transient/summary?valueType=" + getCurrentVariableDto().getValueType() +
          "&script=" + URL.encodeQueryString(scriptEditorPresenter.getScript()));

      if(currentVariable.getCategoriesArray() != null) {
        JsArray<CategoryDto> cats = currentVariable.getCategoriesArray();
        for(int i = 0; i < cats.length(); i++) {
          summaryLink.append("&category=").append(URL.encodeQueryString(cats.get(i).getName()));
        }
      }
      uri = summaryLink.toString();
    } else {
      // TODO: this link should be built from VariableDto.getLink() but it's not initialised in ViewResource
      uri = "/datasource/" + datasourceName + "/table/" + viewName + "/variable/" + currentVariable.getName() +
          "/summary";
    }

    getEventBus().fireEvent(new SummaryRequiredEvent(uri));
  }

  class FormChangedHandler implements ChangeHandler, ValueChangeHandler<Boolean>, CategoriesUpdatedEvent.Handler,
      AttributesUpdatedEvent.Handler {

    @Override
    public void onChange(ChangeEvent arg0) {
      formChange();
    }

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> arg0) {
      formChange();
    }

    @Override
    public void onAttributesCategories(CategoriesUpdatedEvent event) {
      formChange();
    }

    @Override
    public void onAttributesUpdate(AttributesUpdatedEvent event) {
      formChange();
    }

    private void formChange() {
      getEventBus().fireEvent(new ViewSavePendingEvent());
      getView().saveChangesEnabled(true);
      getView().addButtonEnabled(false);
      getView().navigationEnabled(false);
      getView().removeButtonEnabled(false);

      // TODO check if we really need to do that on all form changes, it seems expensive when editing the script
      updateSummaryLink(true);
      if(isTabSelected(Tabs.SUMMARY)) {
        summaryPresenter.refreshDisplay();
      }
    }
  }

  private class ViewSavedHandler implements ViewSavedEvent.Handler {

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
    categoriesPresenter.getView().formEnable(enabled);
    attributesPresenter.getView().formEnable(enabled);
    scriptEditorPresenter.getView().formEnable(enabled);
  }

  /**
   * Clears the variable list form.
   */
  private void formClear() {
    getView().formClear();
    scriptEditorPresenter.getView().formClear();
  }

  private final class ScriptEvaluationCallback implements ResponseCodeCallback {

    private final VariableDto currentVariableDto;

    /**
     * @param currentVariableDto
     */
    private ScriptEvaluationCallback(VariableDto currentVariableDto) {
      this.currentVariableDto = currentVariableDto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == SC_OK) {
        onSuccess();
      } else {
        onFailure(response);
      }
    }

    public void onSuccess() {
      VariableListViewDto variableListViewDto = (VariableListViewDto) viewDto
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);
      variableListViewDto.setVariablesArray(JsArrays.toSafeArray(variableListViewDto.getVariablesArray()));

      // get beautified script version now we know the Javascript is valid
      VariableDtos.setScript(currentVariableDto, scriptEditorPresenter.getBeautifiedScript());

      if(isEmptyVariable(variableListViewDto)) {
        // This view has no variables. Clear the variable list and save.
        variableListViewDto.clearVariablesArray();
        getEventBus().fireEvent(new ViewSaveRequiredEvent(viewDto));
      } else {
        // Validate current variable and save to variable list.
        if(validate()) {
          updateViewDto(variableListViewDto);
          refreshVariableSuggestions();
        }
      }
      getView().setInProgress(false);
    }

    public void onFailure(Response response) {
      try {
        ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
        @SuppressWarnings("unchecked")
        JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) errorDto
            .getExtension(ClientErrorDtoExtensions.errors);
        List<String> messages = new ArrayList<String>();
        for(JavaScriptErrorDto error : JsArrays.toIterable(errors)) {
          String msg = error.getMessage() + " (line #" + error.getLineNumber() + ")";
          messages.add(msg);
          GWT.log(msg);
        }
        if(messages.isEmpty()) {
          getEventBus()
              .fireEvent(NotificationEvent.newBuilder().error(JsArrays.toList(errorDto.getArgumentsArray())).build());
        } else {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(messages).build());
        }
      } catch(Exception e) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("Script compilation failed.").build());
      }
      getView().setInProgress(false);
    }

    private void updateViewDto(VariableListViewDto variableListViewDto) {
      updateCategories();
      updateAttributes();
      if(addVariable) {
        addVariable(variableListViewDto);
      } else {
        updateVariable(variableListViewDto);
      }
      setCurrentVariable(currentVariableDto); // Must do this before form is refreshed.
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

    private boolean isEmptyVariable(VariableListViewDto variableListViewDto) {
      return "".equals(currentVariableDto.getName()) && variableListViewDto.getVariablesArray().length() == 0;
    }

    private void updateVariable(VariableListViewDto variableListViewDto) {
      variableListViewDto.getVariablesArray().set(getVariableIndex(currentVariable), currentVariableDto);
    }

    private void addVariable(VariableListViewDto variableListViewDto) {
      variableListViewDto.getVariablesArray().push(currentVariableDto);
    }

  }

}
