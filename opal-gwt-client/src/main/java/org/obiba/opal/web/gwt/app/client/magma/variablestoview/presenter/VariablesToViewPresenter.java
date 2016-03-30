/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.CategoricalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.VariableDuplicationHelper;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsVariableCopyColumn;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import static org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter.Display.FormField;

public class VariablesToViewPresenter extends ModalPresenterWidget<VariablesToViewPresenter.Display>
    implements VariablesToViewUiHandlers {

  private static final Translations translations = GWT.create(Translations.class);

  private final PlaceManager placeManager;

  private TableDto table;

  private List<VariableDto> variables = new LinkedList<>();

  private VariableCopyValidationHandler variableCopyValidationHandler;

  JsArray<DatasourceDto> datasources;

  @Inject
  public VariablesToViewPresenter(Display display, EventBus eventBus, PlaceManager placeManager) {
    super(eventBus, display);
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  @Override
  public void saveVariable() {
    getView().clearErrors();

    variableCopyValidationHandler = new VariableCopyValidationHandler();
    if(variableCopyValidationHandler.validate()) {
      createOrUpdateViewWithVariables();
    }
  }

  @Override
  public void rename() {
    boolean renameCategory = getView().isRenameSelected();
    JsArray<VariableDto> derivedVariables = JsArrays.create();

    for(VariableDto variable : variables) {
      if(renameCategory) {
        // Keep new name if changed
        derivedVariables.push(getDerivedVariable(variable, renameCategory));
      } else {
        derivedVariables.push(new VariableDuplicationHelper(variable).getDerivedVariable());
      }
    }

    getView().renderRows(variables, derivedVariables, false);
  }

  public void initialize(TableDto table, List<VariableDto> variables) {
    this.table = table;
    this.variables = variables;

    refreshDatasources();

    // Prepare the array of variableDto
    JsArray<VariableDto> derivedVariables = JsArrays.create();
    for(VariableDto variable : variables) {
      derivedVariables.push(new VariableDuplicationHelper(variable).getDerivedVariable());
    }

    getView().renderRows(variables, derivedVariables, true);
    getView().showDialog();
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  private void addEventHandlers() {
    // Remove action
    getView().getActions().setActionHandler(new ActionHandler<VariableDto>() {
      @Override
      public void doAction(VariableDto object, String actionName) {
        if(actionName.equals(ActionsVariableCopyColumn.REMOVE_ACTION)) {
          getView().removeVariable(object);

          updateVariableList();

          // Prepare the array of variableDto
          boolean renameCategory = getView().isRenameSelected();
          JsArray<VariableDto> derivedVariables = JsArrays.create();

          for(VariableDto variable : variables) {
            if(renameCategory) {
              // Keep new name if changed
              derivedVariables.push(getDerivedVariable(variable, renameCategory));
            } else {
              derivedVariables.push(new VariableDuplicationHelper(variable).getDerivedVariable());
            }
          }

          getView().renderRows(variables, derivedVariables, false);
        }
      }

      private void updateVariableList() {
        List<VariableDto> viewVariables = getView().getVariables(false);
        Collection<VariableDto> removeVariables = new LinkedList<>();
        for(VariableDto v : variables) {
          boolean keep = false;
          for(VariableDto viewVariable : viewVariables) {
            if(v.getName().equals(viewVariable.getName())) {
              keep = true;
              break;
            }
          }

          if(!keep) {
            removeVariables.add(v);
          }
        }
        variables.removeAll(removeVariables);
      }
    });
  }

  private void createOrUpdateViewWithVariables() {
    ViewDto view = ViewDtoBuilder.newBuilder().setName(getView().getViewName().getText()).fromTables(table)
        .defaultVariableListView().build();
    VariableListViewDto derivedVariables = (VariableListViewDto) view
        .getExtension(VariableListViewDto.ViewDtoExtensions.view);

    JsArray<VariableDto> variablesDto = JsArrays.create();
    for(VariableDto v : getView().getVariables(true)) {
      // Push with the right name if it was changed
      variablesDto.push(v);
    }
    derivedVariables.setVariablesArray(variablesDto);

    ResponseCodeCallback createCodingViewCallback = new CreateViewCallBack(view);
    ResourceCallback<ViewDto> alreadyExistCodingViewCallback = new UpdateExistViewCallBack(variablesDto);
    UriBuilder uriBuilder = UriBuilder.create();
    uriBuilder.segment("datasource", getView().getDatasourceName(), "view", getView().getViewName().getText());

    ResourceRequestBuilderFactory.<ViewDto>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(alreadyExistCodingViewCallback)//
        .withCallback(Response.SC_NOT_FOUND, createCodingViewCallback)//
        .send();
  }

  private VariableDto getDerivedVariable(VariableDto variable, boolean recodeName) {
    if(VariableDtos.hasCategories(variable) && ("text".equals(variable.getValueType()) ||
        "integer".equals(variable.getValueType()) && !VariableDtos.allCategoriesMissing(variable))) {
      CategoricalVariableDerivationHelper derivationHelper = new CategoricalVariableDerivationHelper(variable,
          recodeName);
      derivationHelper.initializeValueMapEntries();
      return derivationHelper.getDerivedVariable();
    }
    return new VariableDuplicationHelper(variable).getDerivedVariable();
  }

  private class CreateViewCallBack implements ResponseCodeCallback {
    ViewDto view;

    private CreateViewCallBack(ViewDto view) {
      this.view = view;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      ResponseCodeCallback callbackHandler = new CreatedViewCallBack(view, translations.addViewSuccess());
      UriBuilder uriBuilder = UriBuilder.create();
      uriBuilder.segment("datasource", getView().getDatasourceName(), "views");
      ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).post()
          .withResourceBody(ViewDto.stringify(view))//
          .withCallback(callbackHandler, Response.SC_OK, Response.SC_CREATED)//
          .withCallback(new ErrorResponseCallback(getView().asWidget()), Response.SC_FORBIDDEN,
              Response.SC_INTERNAL_SERVER_ERROR, Response.SC_BAD_REQUEST)//
          .send();
    }
  }

  private class UpdateExistViewCallBack implements ResourceCallback<ViewDto> {
    JsArray<VariableDto> variables;

    private UpdateExistViewCallBack(JsArray<VariableDto> variables) {
      this.variables = variables;
    }

    @Override
    public void onResource(Response response, ViewDto viewDto) {
      // Proceed with view creation
      VariableListViewDto derivedVariables = (VariableListViewDto) viewDto
          .getExtension(VariableListViewDto.ViewDtoExtensions.view);
      updateFromTables(viewDto);

      JsArray<VariableDto> existingVariables = JsArrays.toSafeArray(derivedVariables.getVariablesArray());
      for(int i = 0; i < variables.length(); i++) {
        int index = getVariableIndex(existingVariables, variables.get(i).getName());
        if(index == -1) {
          existingVariables.push(variables.get(i));
        } else {
          existingVariables.set(index, variables.get(i));
        }
      }
      derivedVariables.setVariablesArray(existingVariables);

      ResponseCodeCallback callbackHandler = new CreatedViewCallBack(viewDto, translations.updateViewSuccess());
      UriBuilder builder = UriBuilder.create();
      builder.segment("datasource", getView().getDatasourceName(), "view", viewDto.getName());
      ResourceRequestBuilderFactory.newBuilder().forResource(builder.build()).put()
          .withResourceBody(ViewDto.stringify(viewDto))//
          .withCallback(callbackHandler, Response.SC_OK, Response.SC_CREATED, Response.SC_FORBIDDEN,
              Response.SC_BAD_REQUEST)//
          .send();
    }

    private void updateFromTables(ViewDto viewDto) {
      // Update from tables
      JsArrayString fromTables = viewDto.getFromArray();
      String newFrom = table.getDatasourceName() + "." + table.getName();
      GWT.log("" + newFrom);
      boolean addTable = true;
      for(int i = 0; i < fromTables.length(); i++) {
        if(fromTables.get(i).equals(newFrom)) {
          addTable = false;
          break;
        }
      }
      if(addTable) {
        fromTables.push(newFrom);
      }
    }

    private int getVariableIndex(JsArray<VariableDto> vars, String name) {
      for(int i = 0; i < vars.length(); i++) {
        if(vars.get(i).getName().equals(name)) {
          return i;
        }
      }
      return -1;
    }
  }

  private class CreatedViewCallBack implements ResponseCodeCallback {

    ViewDto view;

    String message;

    private CreatedViewCallBack(ViewDto view, String message) {
      this.view = view;
      this.message = message;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      getEventBus().fireEvent(NotificationEvent.newBuilder().info(message).build());
      getEventBus().fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));
      selectView();
    }

    private void selectView() {
      placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(getView().getDatasourceName(), view.getName()));
    }
  }

  private void refreshDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            datasources = JsArrays.toSafeArray(resource);
            for(int i = 0; i < datasources.length(); i++) {
              DatasourceDto d = datasources.get(i);
              d.setViewArray(JsArrays.toSafeArray(d.getViewArray()));
            }

            getView().setDatasources(datasources, table.getDatasourceName());
          }
        }).send();
  }

  public interface Display extends PopupView, HasUiHandlers<VariablesToViewUiHandlers> {

    enum FormField {
      NAME,
      VARIABLE,
      VARIABLES
    }

    void showDialog();

    void hideDialog();

    void setDatasources(JsArray<DatasourceDto> datasources, String name);

    void renderRows(List<VariableDto> originalVariables, JsArray<VariableDto> rows, boolean clearNames);

    ActionsVariableCopyColumn<VariableDto> getActions();

    void removeVariable(VariableDto object);

    HasText getViewName();

    List<VariableDto> getVariables(boolean withNewNames);

    String getDatasourceName();

    boolean isRenameSelected();

    void updateRenameCheckboxVisibility(List<VariableDto> originalVariables);

    void clearErrors();

    void showError(@Nullable FormField formField, String message);
  }

  private class VariableCopyValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<>();

        validators.add(new RequiredTextValidator(getView().getViewName(), "ViewNameRequired", FormField.NAME.name()));
        validators.add(new ConditionValidator(
            viewNotCurrentCondition(getView().getDatasourceName(), getView().getViewName().getText()),
            "CopyVariableCurrentView", FormField.NAME.name()));

        List<VariableDto> list = getView().getVariables(true);
        String variableGroupName = list.size() == 1 ? FormField.VARIABLE.name() : FormField.VARIABLES.name();
        for(VariableDto v : list) {
          validators.add(new ConditionValidator(variableNameEmptyCondition(v.getName()), "CopyVariableNameRequired",
              variableGroupName));

          @SuppressWarnings("unchecked")
          List<String> args = new ArrayList();
          args.add(v.getName());
          validators.add(new ConditionValidator(variableNameColonCondition(v.getName()),
              TranslationsUtils.replaceArguments(translations.userMessageMap().get("CopyVariableNameColon"), args),
              variableGroupName));
        }

        validators.add(new VariableNameUniqueCondition(list));
      }
      return validators;
    }

    private HasValue<Boolean> viewNotCurrentCondition(final String datasourceName, final String viewName) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !(datasourceName + "." + viewName).equals(table.getDatasourceName() + "." + table.getName());
        }
      };
    }

    private HasValue<Boolean> variableNameEmptyCondition(final String name) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !name.isEmpty();
        }
      };
    }

    private HasValue<Boolean> variableNameColonCondition(final String name) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !name.contains(":");
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(FormField.valueOf(id), message);
    }
  }

  private static class VariableNameUniqueCondition extends AbstractFieldValidator {

    List<VariableDto> variables;

    private VariableNameUniqueCondition(List<VariableDto> variables) {
      super("CopyVariableNameAlreadyExists");
      this.variables = variables;
    }

    @Override
    protected boolean hasError() {
      Collection<String> names = new HashSet<>();

      for(VariableDto var : variables) {
        if(!names.add(var.getName())) {
          List<String> args = new ArrayList<>();
          args.add(var.getName());
          setArgs(args);
          return true;
        }
      }
      return false;
    }
  }
}
