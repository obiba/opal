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
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.VariableDuplicationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VcsCommitInfoReceivedEvent;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.CategoriesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.NamespacedAttributesTableUiHandlers;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectPresenter;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

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

  private final TranslationMessages translationMessages;

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

  @Inject
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters" })
  public VariablePresenter(Display display, EventBus eventBus, PlaceManager placeManager,
      ValuesTablePresenter valuesTablePresenter, SummaryTabPresenter summaryTabPresenter,
      ScriptEditorPresenter scriptEditorPresenter, Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
      VariableVcsCommitHistoryPresenter variableVcsCommitHistoryPresenter,
      ModalProvider<VariablesToViewPresenter> variablesToViewProvider,
      ModalProvider<CategoriesEditorModalPresenter> categoriesEditorModalProvider,
      ModalProvider<VariablePropertiesModalPresenter> propertiesEditorModalProvider,
      ModalProvider<VariableAttributeModalPresenter> attributeModalProvider, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.placeManager = placeManager;
    this.valuesTablePresenter = valuesTablePresenter;
    this.summaryTabPresenter = summaryTabPresenter;
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.variableVcsCommitHistoryPresenter = variableVcsCommitHistoryPresenter;
    this.scriptEditorPresenter = scriptEditorPresenter;
    this.translationMessages = translationMessages;
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
    setInSlot(Display.Slots.Summary, summaryTabPresenter);

    addRegisteredHandler(VariableSelectionChangeEvent.getType(), this);
    addRegisteredHandler(VariableRefreshEvent.getType(), new VariableRefreshEvent.Handler() {
      @Override
      public void onVariableRefresh(VariableRefreshEvent event) {
        if(variableUpdatePending || variable == null) return;
        ResourceRequestBuilderFactory.<VariableDto>newBuilder() //
            .forResource(variable.getLink()) //
            .withCallback(new ResourceCallback<VariableDto>() {
              @Override
              public void onResource(Response response, VariableDto resource) {
                updateVariableDisplay(resource);
                updateDerivedVariableDisplay();
                variableUpdatePending = false;
              }
            }) //
            .get().send();
      }
    });
    addRegisteredHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler());

    addRegisteredHandler(VcsCommitInfoReceivedEvent.getType(), new VcsCommitInfoReceivedEvent.Handler() {
      @Override
      public void onVcsCommitInfoReceived(VcsCommitInfoReceivedEvent event) {
        getView().goToEditScript();
        scriptEditorPresenter.setScript(event.getCommitInfoDto().getBlob());
      }
    });
  }

  private void updateDisplay(final String datasourceName, final String tableName, final String variableName) {
    if(table != null && table.getDatasourceName().equals(datasourceName) && table.getName().equals(tableName) &&
        variable != null && variable.getName().equals(variableName)) return;

    if(variableUpdatePending) return;
    ResourceRequestBuilderFactory.<VariableDto>newBuilder()
        .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create().build(datasourceName, tableName, variableName))
        .withCallback(new ResourceCallback<VariableDto>() {
          @Override
          public void onResource(Response response, VariableDto resource) {
            variableUpdatePending = false;
          }
        }) //
        .withCallback(Response.SC_NOT_FOUND, new VariableNotFoundCallback(variableName, datasourceName, tableName)).get().send();

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
    if(getView().isValuesTabSelected()) {
      valuesTablePresenter.setTable(table, variable);
    }
  }

  private void updateVariableDisplay(VariableDto variableDto) {
    variable = variableDto;
    getView().setVariable(variable);
    if(variable.getLink().isEmpty()) {
      variable.setLink(variable.getParentLink().getLink() + "/variable/" + variable.getName());
    }

    getView().renderCategoryRows(variable.getCategoriesArray());

    // Attributes editable depending on authorization
    UriBuilder builder = table.hasViewLink()
        ? UriBuilders.DATASOURCE_VIEW_VARIABLE.create()
        : UriBuilders.DATASOURCE_TABLE_VARIABLE.create();
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(builder.build(table.getDatasourceName(), table.getName(), variable.getName())).put()
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
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build()) //
        .withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, TableDto resource) {
            String uri = UriBuilder.create().fromPath(resource.getViewLink()) //
                .segment("variable", "_transient", "_compile") //
                .query("valueType", newVariable.getValueType(), //
                    "repeatable", String.valueOf(newVariable.getIsRepeatable())) //
                .build();

            ResourceRequestBuilderFactory.newBuilder().forResource(uri) //
                .withFormBody("script", VariableDtos.getScript(newVariable)) //
                .withCallback(new ScriptEvaluationCallback(newVariable), SC_BAD_REQUEST, SC_OK) //
                .post() //
                .send();

          }
        }) //
        .get().send();
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
        ? ConfirmationRequiredEvent.createWithMessages(removeConfirmation, translationMessages.removeDerivedVariable(),
        translationMessages.confirmRemoveDerivedVariable())
        : ConfirmationRequiredEvent.createWithMessages(removeConfirmation, translationMessages.removeVariable(),
            translationMessages.confirmRemoveVariable());

    fireEvent(event);
  }

  @Override
  public void onAddToView() {
    List<VariableDto> variables = new ArrayList<>();
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
    fireSummaryRequiredEvent(variable);
  }

  @Override
  public void onShowValues() {
    valuesTablePresenter.setTable(table, variable);
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
        if(attribute.getName().equals(toRemove.get(0).getName()) &&
            attribute.getNamespace().equals(toRemove.get(0).getNamespace())) {
          keep = false;
          break;
        }
      }

      if(keep) {
        filteredAttributes.push(attribute);
      }
    }

    dto.setAttributesArray(filteredAttributes);

    UriBuilder uriBuilder = table.hasViewLink()
        ? UriBuilders.DATASOURCE_VIEW_VARIABLE.create()
        : UriBuilders.DATASOURCE_TABLE_VARIABLE.create();

    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(uriBuilder.build(table.getDatasourceName(), table.getName(), variable.getName())) //
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
    dto.setIndex(variable.getIndex());
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

      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.DATASOURCE_VIEW_VARIABLE.create()
              .build(table.getDatasourceName(), table.getName(), variable.getName())) //
          .withCallback(callbackHandler, SC_OK, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND) //
          .delete().send();
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

      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
              .build(table.getDatasourceName(), table.getName(), variable.getName())) //
          .withCallback(callbackHandler, SC_OK, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND) //
          .delete().send();
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
        String uri = UriBuilder.create().segment("datasource", "{}", "view", "{}", "variable", "{}")
            .query("comment", getView().getComment())
            .build(table.getDatasourceName(), table.getName(), variable.getName());

        ResourceRequestBuilderFactory.newBuilder() //
            .forResource(uri) //
            .withResourceBody(VariableDto.stringify(newVariable)) //
            .withCallback(new UpdateVariableCallbackHandler(), SC_OK, SC_BAD_REQUEST) //
            .put().send();
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
      if(getView().isSummaryTabSelected()) {
        requestSummary(variable);
        summaryTabPresenter.init();
      }
    }

    private void requestSummary(VariableDto selection) {
      fireSummaryRequiredEvent(selection);
    }
  }

  private void fireSummaryRequiredEvent(final VariableDto selection) {
    String tableUri = UriBuilders.DATASOURCE_TABLE.create().query("counts", "true")
        .build(table.getDatasourceName(), table.getName());
    ResourceRequestBuilderFactory.<TableDto>newBuilder() //
        .forResource(tableUri) //
        .withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, TableDto resource) {
            fireEvent(new SummaryRequiredEvent(UriBuilders.DATASOURCE_TABLE_VARIABLE_SUMMARY.create(),
                resource.getValueSetCount(), table.getDatasourceName(), table.getName(), selection.getName()));
          }
        }) //
        .get().send();
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
      Permissions, Values, ScriptEditor, Summary, History
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

    boolean isSummaryTabSelected();

    boolean isValuesTabSelected();

    HasAuthorization getSummaryAuthorizer();

    HasAuthorization getValuesAuthorizer();

    HasAuthorization getEditAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    void setDeriveFromMenuVisibility(boolean visible);

    void resetTabs();

    HasAuthorization getVariableAttributesAuthorizer(VariableDto variableDto);

  }

  private class VariableNotFoundCallback implements ResponseCodeCallback {
    private final String variableName;

    private final String datasourceName;

    private final String tableName;

    public VariableNotFoundCallback(String variableName, String datasourceName, String tableName) {
      this.variableName = variableName;
      this.datasourceName = datasourceName;
      this.tableName = tableName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(NotificationEvent.newBuilder().warn("NoSuchVariable").args(variableName).build());

      PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(Places.PROJECT)
          .with(ParameterTokens.TOKEN_NAME, datasourceName) //
          .with(ParameterTokens.TOKEN_TAB, ProjectPresenter.Display.ProjectTab.TABLES.toString())//
          .with(ParameterTokens.TOKEN_PATH, datasourceName + "." + tableName);

      placeManager.revealPlace(builder.build());
    }
  }
}
