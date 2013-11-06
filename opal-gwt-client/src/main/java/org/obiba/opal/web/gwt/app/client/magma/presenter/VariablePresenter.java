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

import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.VariableDuplicationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.SiblingVariableSelectionEvent.Direction;
import org.obiba.opal.web.gwt.app.client.magma.event.SummaryRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.VcsCommitInfoReceivedEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.CategoriesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.PropertiesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
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
import org.obiba.opal.web.model.client.opal.LocaleDto;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;
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

public class VariablePresenter extends PresenterWidget<VariablePresenter.Display>
    implements VariableUiHandlers, VariableSelectionChangeEvent.Handler {

  private final PlaceManager placeManager;

  private final SummaryTabPresenter summaryTabPresenter;

  private final ValuesTablePresenter valuesTablePresenter;

  private final ScriptEditorPresenter scriptEditorPresenter;

  private final Provider<AuthorizationPresenter> authorizationPresenter;

  private final ModalProvider<VariablesToViewPresenter> variablesToViewProvider;

  private final VariableVcsCommitHistoryPresenter variableVcsCommitHistoryPresenter;

  private final ModalProvider<CategoriesEditorModalPresenter> categoriesEditorModalProvider;

  private final ModalProvider<PropertiesEditorModalPresenter> propertiesEditorModalProvider;

  private VariableDto variable;

  private TableDto table;

  private boolean variableUpdatePending = false;

  private Runnable removeConfirmation;

  @Inject
  public VariablePresenter(Display display, EventBus eventBus, PlaceManager placeManager,
      ValuesTablePresenter valuesTablePresenter, SummaryTabPresenter summaryTabPresenter,
      ScriptEditorPresenter scriptEditorPresenter, Provider<AuthorizationPresenter> authorizationPresenter,
      VariableVcsCommitHistoryPresenter variableVcsCommitHistoryPresenter,
      ModalProvider<VariablesToViewPresenter> variablesToViewProvider,
      ModalProvider<CategoriesEditorModalPresenter> categoriesEditorModalProvider,
      ModalProvider<PropertiesEditorModalPresenter> propertiesEditorModalProvider) {
    super(eventBus, display);
    this.placeManager = placeManager;
    this.valuesTablePresenter = valuesTablePresenter;
    this.summaryTabPresenter = summaryTabPresenter;
    this.authorizationPresenter = authorizationPresenter;
    this.variableVcsCommitHistoryPresenter = variableVcsCommitHistoryPresenter;
    this.scriptEditorPresenter = scriptEditorPresenter;
    this.variablesToViewProvider = variablesToViewProvider.setContainer(this);
    this.categoriesEditorModalProvider = categoriesEditorModalProvider.setContainer(this);
    this.propertiesEditorModalProvider = propertiesEditorModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
    resetView(event.getTable());

    if(event.hasTable()) {
      updateDisplay(event.getTable(), event.getSelection(), event.getPrevious(), event.getNext());
    } else {
      updateDisplay(event.getDatasourceName(), event.getTableName(), event.getVariableName(), null, null);
    }
    ResourceRequestBuilderFactory.<JsArray<LocaleDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCE_LOCALES.create().build(event.getDatasourceName())).get()
        .withCallback(new ResourceCallback<JsArray<LocaleDto>>() {
          @Override
          public void onResource(Response response, JsArray<LocaleDto> resource) {
            getView().setLanguages(JsArrays.toSafeArray(resource));
          }
        }).send();
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Values, valuesTablePresenter);
    setInSlot(Display.Slots.ScriptEditor, scriptEditorPresenter);
    setInSlot(Display.Slots.History, variableVcsCommitHistoryPresenter);

    addRegisteredHandler(VariableSelectionChangeEvent.getType(), this);
    addRegisteredHandler(ViewSavedEvent.getType(), new ViewSavedEventHandler());
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
        updateDerivedVariableByCommitInfo(event.getCommitInfoDto());
      }
    });
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    summaryTabPresenter.unbind();
  }

  private void updateDisplay(String datasourceName, String tableName, String variableName, @Nullable String previous,
      @Nullable String next) {
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

    if(variable.getLink().isEmpty()) {
      variable.setLink(variable.getParentLink().getLink() + "/variable/" + variable.getName());
    }
    updateVariableDisplay(variableDto);
    updateMenuDisplay(previous, next);
    updateDerivedVariableDisplay();

    authorize();
  }

  private void updateVariableDisplay(VariableDto variableDto) {
    variable = variableDto;
    getView().setVariable(variable);
    if(variable.getLink().isEmpty()) {
      variable.setLink(variable.getParentLink().getLink() + "/variable/" + variable.getName());
    }

    getView().renderCategoryRows(variable.getCategoriesArray());
    getView().renderAttributeRows(variable.getAttributesArray());
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

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void updateDerivedVariableByCommitInfo(VcsCommitInfoDto commitInfo) {
    getView().goToEditScript();
    scriptEditorPresenter.setScript(commitInfo.getBlob());
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
      ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(table.getViewLink()).put()
          .authorize(getView().getEditAuthorizer()).send();
    }
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private boolean isCurrentVariable(VariableDto variableDto) {
    return variableDto.getName().equals(variable.getName()) &&
        variableDto.getParentLink().getLink().equals(variable.getParentLink().getLink());
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private boolean isCurrentTable(ViewDto viewDto) {
    return table != null && table.getDatasourceName().equals(viewDto.getDatasourceName()) &&
        table.getName().equals(viewDto.getName());
  }

  /**
   * @param selection
   */
  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void requestSummary(VariableDto selection) {
    getEventBus().fireEvent(new SummaryRequiredEvent(selection.getLink() + "/summary"));
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private String getViewLink() {
    return variable.getParentLink().getLink().replaceFirst("/table/", "/view/");
  }

  @Override
  public void onNextVariable() {
    getEventBus().fireEvent(new SiblingVariableSelectionEvent(variable, Direction.NEXT));
  }

  @Override
  public void onPreviousVariable() {
    getEventBus().fireEvent(new SiblingVariableSelectionEvent(variable, Direction.PREVIOUS));
  }

  @Override
  public void onEdit() {
    ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(getViewLink()).get()
        .withCallback(new ResourceCallback<ViewDto>() {

          @Override
          public void onResource(Response response, ViewDto viewDto) {
            getEventBus().fireEvent(new ViewConfigurationRequiredEvent(viewDto, variable));
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
    getEventBus().fireEvent(new WizardRequiredEvent(DeriveVariablePresenter.CategorizeWizardType, variable, table));
  }

  @Override
  public void onCategorizeToThis() {
    getEventBus().fireEvent(new WizardRequiredEvent(DeriveVariablePresenter.FromWizardType, variable, table));
  }

  @Override
  public void onDeriveCustom() {
    getEventBus().fireEvent(new WizardRequiredEvent(DeriveVariablePresenter.CustomWizardType, variable, table));
  }

  @Override
  public void onShowSummary() {
    summaryTabPresenter.onReset();
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
  public void onEditProperties() {
    PropertiesEditorModalPresenter propertiesEditorPresenter = propertiesEditorModalProvider.get();
    propertiesEditorPresenter.initialize(variable, table);
  }

  private void resetView(TableDto tableDto) {
    getView().backToViewScript();
    if (tableChanged(tableDto)) {
      getView().resetTabs();
    }
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

  class ViewSavedEventHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      if(isVisible() && isCurrentTable(event.getView())) {
        ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(table.getLink() + "/variables")
            .get().withCallback(new ResourceCallback<JsArray<VariableDto>>() {

          @Override
          public void onResource(Response response, JsArray<VariableDto> resource) {
            JsArray<VariableDto> variables = JsArrays.toSafeArray(resource);
            for(int i = 0; i < variables.length(); i++) {
              if(isCurrentVariable(variables.get(i))) {
                variable = null;
                updateDisplay(table, variables.get(i), i > 0 ? variables.get(i - 1) : null,
                    i < variables.length() + 1 ? variables.get(i + 1) : null);
                break;
              }
            }
          }
        }).send();
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
        ResponseCodeCallback updateVariableCallbackHandler = new UpdateVariableCallbackHandler(newVariable);

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
        getEventBus().fireEvent(notificationEvent);
      }
    }
  }

  private class UpdateVariableCallbackHandler implements ResponseCodeCallback {

    private final VariableDto variable;

    public UpdateVariableCallbackHandler(VariableDto variable) {
      this.variable = variable;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      switch(response.getStatusCode()) {
        case SC_OK:
          variableUpdatePending = false;
          getEventBus().fireEvent(new VariableRefreshEvent());
          getView().backToViewScript();
          break;
        case Response.SC_NOT_FOUND:
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
  }

  public interface Display extends View, HasUiHandlers<VariableUiHandlers> {

    void setLanguages(JsArray<LocaleDto> languages);

    enum Slots {
      Permissions, Values, ScriptEditor, History
    }

    void setVariable(VariableDto variable);

    void setCategorizeMenuAvailable(boolean available);

    void setDerivedVariable(boolean derived, String script);

    void setPreviousName(String name);

    void setNextName(String name);

    void renderCategoryRows(JsArray<CategoryDto> rows);

    void renderAttributeRows(JsArray<AttributeDto> rows);

    void goToEditScript();

    String getComment();

    void backToViewScript();

    void setSummaryTabWidget(View widget);

    boolean isSummaryTabSelected();

    HasAuthorization getSummaryAuthorizer();

    HasAuthorization getValuesAuthorizer();

    HasAuthorization getEditAuthorizer();

    void setDeriveFromMenuVisibility(boolean visible);

    void resetTabs();
  }
}
