/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.VariableDuplicationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VcsCommitInfoReceivedEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.CategoriesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.NamespacedAttributesTableUiHandlers;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.support.JSErrorNotificationEventBuilder;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;
import static org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalPresenter.Mode;

@SuppressWarnings("OverlyCoupledClass")
public class VariablePresenter extends PresenterWidget<VariablePresenter.Display>
    implements VariableUiHandlers, NamespacedAttributesTableUiHandlers, VariableSelectionChangeEvent.Handler {

  private final PlaceManager placeManager;

  private final SummaryTabPresenter summaryTabPresenter;

  private final ValuesTablePresenter valuesTablePresenter;

  private final ScriptEditorPresenter scriptEditorPresenter;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final ModalProvider<VariablesToViewPresenter> variablesToViewProvider;

  private final VariableVcsCommitHistoryPresenter variableVcsCommitHistoryPresenter;

  private final ModalProvider<CategoriesEditorModalPresenter> categoriesEditorModalProvider;

  private final ModalProvider<VariablePropertiesModalPresenter> propertiesEditorModalProvider;

  private final ModalProvider<VariableAttributeModalPresenter> attributeModalProvider;

  private TableDto table;

  private VariableDto variable;

  private VariableDto nextVariable;

  private VariableDto previousVariable;

  private boolean variableUpdatePending = false;

  private Runnable removeConfirmation;

  @SuppressWarnings({ "PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters" })
  @Inject
  public VariablePresenter(Display display, EventBus eventBus, PlaceManager placeManager,
      ValuesTablePresenter valuesTablePresenter, SummaryTabPresenter summaryTabPresenter,
      ScriptEditorPresenter scriptEditorPresenter, Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
      VariableVcsCommitHistoryPresenter variableVcsCommitHistoryPresenter,
      ModalProvider<VariablesToViewPresenter> variablesToViewProvider,
      ModalProvider<CategoriesEditorModalPresenter> categoriesEditorModalProvider,
      ModalProvider<VariablePropertiesModalPresenter> propertiesEditorModalProvider,
      ModalProvider<VariableAttributeModalPresenter> attributeModalProvider) {
    super(eventBus, display);
    this.placeManager = placeManager;
    this.valuesTablePresenter = valuesTablePresenter;
    this.summaryTabPresenter = summaryTabPresenter;
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.variableVcsCommitHistoryPresenter = variableVcsCommitHistoryPresenter;
    this.scriptEditorPresenter = scriptEditorPresenter;
    this.variablesToViewProvider = variablesToViewProvider.setContainer(this);
    this.categoriesEditorModalProvider = categoriesEditorModalProvider.setContainer(this);
    this.propertiesEditorModalProvider = propertiesEditorModalProvider.setContainer(this);
    this.attributeModalProvider = attributeModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
    // Prevent this event to be executed twice
    if(variable == null || !variable.getLink().equals(event.getSelection().getLink())) {
      resetView(event.getTable());

      if(event.hasTable()) {
        updateDisplay(event.getTable(), event.getSelection(), event.getPrevious(), event.getNext());
      } else {
        updateDisplay(event.getDatasourceName(), event.getTableName(), event.getVariableName());
      }
    }
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Values, valuesTablePresenter);
    setInSlot(Display.Slots.ScriptEditor, scriptEditorPresenter);
    setInSlot(Display.Slots.History, variableVcsCommitHistoryPresenter);

    addRegisteredHandler(VariableSelectionChangeEvent.getType(), this);
    addRegisteredHandler(VariableRefreshEvent.getType(), new VariableRefreshEvent.Handler() {
      @Override
      public void onVariableRefresh(VariableRefreshEvent event) {
        if(variableUpdatePending) return;
        ResourceRequestBuilderFactory.<VariableDto>newBuilder().forResource(variable.getLink()).get()
            .withCallback(new ResourceCallback<VariableDto>() {
              @Override
              public void onResource(Response response, VariableDto resource) {
                updateVariableDisplay(resource);
                updateDerivedVariableDisplay();
                variableUpdatePending = false;
              }
            }).send();
      }
    });
    addRegisteredHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler());

    summaryTabPresenter.bind();
    getView().setSummaryTabWidget(summaryTabPresenter.getView());
    addRegisteredHandler(VcsCommitInfoReceivedEvent.getType(), new VcsCommitInfoReceivedEvent.Handler() {
      @Override
      public void onVcsCommitInfoReceived(VcsCommitInfoReceivedEvent event) {
        getView().goToEditScript();
        scriptEditorPresenter.setScript(event.getCommitInfoDto().getBlob());
      }
    });
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    summaryTabPresenter.unbind();
  }

  private void updateDisplay(String datasourceName, String tableName, String variableName) {
    if(table != null && table.getDatasourceName().equals(datasourceName) && table.getName().equals(tableName) &&
        variable != null && variable.getName().equals(variableName)) return;

    if(variableUpdatePending) return;
    ResourceRequestBuilderFactory.<VariableDto>newBuilder()
        .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create().build(datasourceName, tableName, variableName))
        .get().withCallback(new ResourceCallback<VariableDto>() {
      @Override
      public void onResource(Response response, VariableDto resource) {
        variableUpdatePending = false;
      }
    }).send();

  }

  private void updateDisplay(TableDto tableDto, VariableDto variableDto, @Nullable VariableDto previous,
      @Nullable VariableDto next) {
    table = tableDto;
    variable = variableDto;
    nextVariable = next;
    previousVariable = previous;

    if(variable.getLink().isEmpty()) {
      variable.setLink(variable.getParentLink().getLink() + "/variable/" + variable.getName());
    }
    updateVariableDisplay(variableDto);
    updateMenuDisplay(previous, next);
    updateDerivedVariableDisplay();
    updateValuesDisplay();

    authorize();
  }

  private void updateValuesDisplay() {
    valuesTablePresenter.setTable(table, variable);
  }

  private void updateVariableDisplay(VariableDto variableDto) {
    variable = variableDto;
    getView().setVariable(variable);
    if(variable.getLink().isEmpty()) {
      variable.setLink(variable.getParentLink().getLink() + "/variable/" + variable.getName());
    }

    getView().renderCategoryRows(variable.getCategoriesArray());

    // Attributes editable depending on authorization
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
        .build(table.getDatasourceName(), table.getName(), variable.getName())).put()
        .authorize(getView().getVariableAttributesAuthorizer(variable)).send();
  }

  private void updateMenuDisplay(@Nullable VariableDto previous, @Nullable VariableDto next) {
    getView().setPreviousName(previous == null ? "" : previous.getName());
    getView().setNextName(next == null ? "" : next.getName());

    getView().setCategorizeMenuAvailable(!"binary".equals(variable.getValueType()));
    getView().setDeriveFromMenuVisibility(table.hasViewLink());
  }

  private void updateDerivedVariableDisplay() {
    // if table is a view, check for a script attribute
    if(table == null || !table.hasViewLink()) {
      getView().setDerivedVariable(false, "");
      return;
    }
    String script = VariableDtos.getScript(variable);
    getView().setDerivedVariable(true, script);
    scriptEditorPresenter.setTable(table);
    scriptEditorPresenter.setScript(script);
    scriptEditorPresenter.setRepeatable(variable.getIsRepeatable());
    scriptEditorPresenter.setValueEntityType(variable.getValueType());
  }

  private void authorize() {
    // summary
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(variable.getLink() + "/summary").get()
        .authorize(new CompositeAuthorizer(getView().getSummaryAuthorizer(), new SummaryUpdate())).send();

    // values
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(variable.getParentLink().getLink() + "/valueSets").get().authorize(getView().getValuesAuthorizer())
        .send();

    // edit variable
    if(table.hasViewLink()) {
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASOURCE_VIEW_VARIABLE.create()
          .build(table.getDatasourceName(), table.getName(), variable.getName())).put()
          .authorize(getView().getEditAuthorizer()).send();
    } else {
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
          .build(table.getDatasourceName(), table.getName(), variable.getName())).put()
          .authorize(getView().getEditAuthorizer()).send();
    }

    // set permissions
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.PROJECT_PERMISSIONS_VARIABLE.create()
            .build(table.getDatasourceName(), table.getName(), variable.getName())) //
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())) //
        .post().send();
  }

  private String getViewLink() {
    return variable.getParentLink().getLink().replaceFirst("/table/", "/view/");
  }

  @Override
  public void onNextVariable() {
    placeManager.revealPlace(
        ProjectPlacesHelper.getVariablePlace(table.getDatasourceName(), table.getName(), nextVariable.getName()));
  }

  @Override
  public void onPreviousVariable() {
    placeManager.revealPlace(
        ProjectPlacesHelper.getVariablePlace(table.getDatasourceName(), table.getName(), previousVariable.getName()));
  }

  @Override
  public void onEdit() {
    ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(getViewLink()).get()
        .withCallback(new ResourceCallback<ViewDto>() {

          @Override
          public void onResource(Response response, ViewDto viewDto) {
            fireEvent(new ViewConfigurationRequiredEvent(viewDto, variable));
          }
        }).send();
  }

  @Override
  public void onEditScript() {
    updateDerivedVariableDisplay();
  }

  @Override
  public void onSaveScript() {
    VariableDuplicationHelper variableDuplicationHelper = new VariableDuplicationHelper(variable);
    VariableDto newVariable = variableDuplicationHelper.getDerivedVariable();
    VariableDtos.setScript(newVariable, scriptEditorPresenter.getScript());
    newVariable.setValueType(scriptEditorPresenter.getValueEntityType().getLabel());
    newVariable.setIsRepeatable(scriptEditorPresenter.isRepeatable());

    compileScript(newVariable);
  }

  private void compileScript(final VariableDto newVariable) {
    UriBuilder ub = UriBuilder.create().segment("datasource", table.getDatasourceName(), "table", table.getName())
        .query("counts", "false");
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, TableDto resource) {
            String script = VariableDtos.getScript(newVariable);
            String uri = resource.getViewLink() + "/from/variable/_transient/_compile?valueType=" +
                newVariable.getValueType() + "&repeatable=" + newVariable.getIsRepeatable();
            ResourceRequestBuilderFactory.newBuilder().forResource(uri) //
                .post() //
                .withFormBody("script", script) //
                .withCallback(new ScriptEvaluationCallback(newVariable), SC_BAD_REQUEST, SC_OK) //
                .send();

          }
        }).send();
  }

  @Override
  public void onHistory() {
    variableVcsCommitHistoryPresenter.retrieveCommitInfos(table, variable);
  }

  @Override
  public void onRemove() {
    removeConfirmation = new RemoveRunnable();

    ConfirmationRequiredEvent event;
    event = table.hasViewLink()
        ? ConfirmationRequiredEvent
        .createWithKeys(removeConfirmation, "removeDerivedVariable", "confirmRemoveDerivedVariable")
        : ConfirmationRequiredEvent.createWithKeys(removeConfirmation, "removeVariable", "confirmRemoveVariable");

    fireEvent(event);
  }

  @Override
  public void onAddToView() {
    List<VariableDto> variables = new ArrayList<VariableDto>();
    variables.add(variable);
    VariablesToViewPresenter variablesToViewPresenter = variablesToViewProvider.get();
    variablesToViewPresenter.initialize(table, variables);
  }

  @Override
  public void onCategorizeToAnother() {
    fireEvent(new WizardRequiredEvent(DeriveVariablePresenter.CategorizeWizardType, variable, table));
  }

  @Override
  public void onCategorizeToThis() {
    fireEvent(new WizardRequiredEvent(DeriveVariablePresenter.FromWizardType, variable, table));
  }

  @Override
  public void onDeriveCustom() {
    fireEvent(new WizardRequiredEvent(DeriveVariablePresenter.CustomWizardType, variable, table));
  }

  @Override
  public void onShowSummary() {
    summaryTabPresenter.onReset();
  }

  @Override
  public void onShowValues() {
    updateValuesDisplay();
  }

  @Override
  public void onEditCategories() {
    CategoriesEditorModalPresenter categoriesEditorPresenter = categoriesEditorModalProvider.get();
    categoriesEditorPresenter.initialize(variable, table);
  }

  @Override
  public void onAddAttribute() {
    VariableAttributeModalPresenter attributeEditorPresenter = attributeModalProvider.get();
    attributeEditorPresenter.setDialogMode(Mode.CREATE);
    attributeEditorPresenter.initialize(table, variable);
  }

  @Override
  public void onEditProperties() {
    VariablePropertiesModalPresenter propertiesEditorPresenter = propertiesEditorModalProvider.get();
    propertiesEditorPresenter.initialize(variable, table);
  }

  private void resetView(TableDto tableDto) {
    getView().backToViewScript();
    if(tableChanged(tableDto)) {
      getView().resetTabs();
    }
  }

  @Override
  public void onDeleteAttribute(List<JsArray<AttributeDto>> selectedItems) {
    VariableDto dto = getVariableDto();

    JsArray<AttributeDto> filteredAttributes = JsArrays.create().cast();
    List<AttributeDto> allAttributes = JsArrays.toList(dto.getAttributesArray());

    for(AttributeDto attribute : allAttributes) {
      boolean keep = true;
      for(JsArray<AttributeDto> toRemove : selectedItems) {
        if(attribute.getName().equals(toRemove.get(0).getName())) {
          keep = false;
          break;
        }
      }

      if(keep) {
        filteredAttributes.push(attribute);
      }
    }

    dto.setAttributesArray(filteredAttributes);

    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
            .build(table.getDatasourceName(), table.getName(), variable.getName())) //
        .withResourceBody(VariableDto.stringify(dto)) //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(new VariableRefreshEvent());
          }
        }) //
        .withCallback(Response.SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
        .put().send();
  }

  @Override
  public void onEditAttributes(List<JsArray<AttributeDto>> selectedItems) {
    VariableAttributeModalPresenter attributeEditorPresenter = attributeModalProvider.get();
    attributeEditorPresenter.setDialogMode(selectedItems.size() == 1 ? Mode.UPDATE_SINGLE : Mode.UPDATE_MULTIPLE);
    attributeEditorPresenter.initialize(table, variable, selectedItems);
  }

  private VariableDto getVariableDto() {
    VariableDto dto = VariableDto.create();
    dto.setLink(variable.getLink());
    dto.setIndex(variable.getIndex());
    dto.setIsNewVariable(variable.getIsNewVariable());
    dto.setParentLink(variable.getParentLink());
    dto.setName(variable.getName());
    dto.setEntityType(variable.getEntityType());
    dto.setValueType(variable.getValueType());
    dto.setIsRepeatable(variable.getIsRepeatable());
    dto.setUnit(variable.getUnit());
    dto.setReferencedEntityType(variable.getReferencedEntityType());
    dto.setMimeType(variable.getMimeType());
    dto.setOccurrenceGroup(variable.getOccurrenceGroup());
    dto.setAttributesArray(variable.getAttributesArray());

    if(variable.getCategoriesArray() != null) {
      dto.setCategoriesArray(variable.getCategoriesArray());
    }

    return dto;
  }

  private boolean tableChanged(TableDto tableDto) {
    String curTableName = table != null ? table.getName() : "";
    String newTableName = tableDto != null ? tableDto.getName() : "";
    return !curTableName.equals(newTableName);
  }

  //
  // Interfaces and classes
  //

  private class RemoveRunnable implements Runnable {
    @Override
    public void run() {
      if(table.hasViewLink()) {
        removeDerivedVariable();
      } else {
        removeVariable();
      }
    }

    private void gotoTable() {
      placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(table.getDatasourceName(), table.getName()));
    }

    private void removeDerivedVariable() {

      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            gotoTable();
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      String uri = UriBuilders.DATASOURCE_VIEW_VARIABLE.create()
          .build(table.getDatasourceName(), table.getName(), variable.getName());
      ResourceRequestBuilderFactory.newBuilder().forResource(uri).delete().withCallback(SC_OK, callbackHandler)
          .withCallback(SC_FORBIDDEN, callbackHandler).withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler)
          .withCallback(SC_NOT_FOUND, callbackHandler).send();
    }

    private void removeVariable() {

      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            gotoTable();
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      String uri = UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
          .build(table.getDatasourceName(), table.getName(), variable.getName());
      ResourceRequestBuilderFactory.newBuilder().forResource(uri).delete().withCallback(SC_OK, callbackHandler)
          .withCallback(SC_FORBIDDEN, callbackHandler).withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler)
          .withCallback(SC_NOT_FOUND, callbackHandler).send();
    }

  }

  private class RemoveConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      }
    }
  }

  private final class ScriptEvaluationCallback implements ResponseCodeCallback {

    private final VariableDto newVariable;

    /**
     * @param newVariable
     */
    private ScriptEvaluationCallback(VariableDto newVariable) {
      this.newVariable = newVariable;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == SC_OK) {
        ResponseCodeCallback updateVariableCallbackHandler = new UpdateVariableCallbackHandler();

        String uri = UriBuilder.create().segment("datasource", "{}", "view", "{}", "variable", "{}")
            .query("comment", getView().getComment())
            .build(table.getDatasourceName(), table.getName(), variable.getName());

        ResourceRequestBuilderFactory.newBuilder().forResource(uri).put()
            .withResourceBody(VariableDto.stringify(newVariable))
            .withCallback(Response.SC_OK, updateVariableCallbackHandler)
            .withCallback(Response.SC_BAD_REQUEST, updateVariableCallbackHandler).send();
      } else {
        NotificationEvent notificationEvent = new JSErrorNotificationEventBuilder()
            .build((ClientErrorDto) JsonUtils.unsafeEval(response.getText()));
        fireEvent(notificationEvent);
      }
    }
  }

  private class UpdateVariableCallbackHandler implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      switch(response.getStatusCode()) {
        case SC_OK:
          variableUpdatePending = false;
          fireEvent(new VariableRefreshEvent());
          getView().backToViewScript();
          break;
        case SC_NOT_FOUND:
          break;
      }
    }
  }

  /**
   * Update summary on authorization.
   */
  private final class SummaryUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      requestSummary(variable);
      if(getView().isSummaryTabSelected()) {
        summaryTabPresenter.onReset();
      }
    }

    private void requestSummary(VariableDto selection) {
      fireEvent(
          new SummaryRequiredEvent(UriBuilders.DATASOURCE_TABLE_VARIABLE_SUMMARY.create(), table.getDatasourceName(),
              table.getName(), selection.getName()));
    }
  }

  /**
   * Update permissions on authorization.
   */
  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter.initialize(ResourcePermissionType.VARIABLE,
          ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_TABLE_VARIABLE, table.getDatasourceName(),
          table.getName(), variable.getName());
      setInSlot(Display.Slots.Permissions, resourcePermissionsPresenter);
    }
  }

  public interface Display extends View, HasUiHandlers<VariableUiHandlers> {

    enum Slots {
      Permissions, Values, ScriptEditor, History
    }

    void setVariable(VariableDto variable);

    void setCategorizeMenuAvailable(boolean available);

    void setDerivedVariable(boolean derived, String script);

    void setPreviousName(String name);

    void setNextName(String name);

    void renderCategoryRows(JsArray<CategoryDto> rows);

    void goToEditScript();

    String getComment();

    void backToViewScript();

    void setSummaryTabWidget(View widget);

    boolean isSummaryTabSelected();

    HasAuthorization getSummaryAuthorizer();

    HasAuthorization getValuesAuthorizer();

    HasAuthorization getEditAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    void setDeriveFromMenuVisibility(boolean visible);

    void resetTabs();

    HasAuthorization getVariableAttributesAuthorizer(VariableDto variableDto);

  }
}
