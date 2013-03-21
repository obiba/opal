/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.wizard.variablestoview.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.CopyVariablesToViewEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsVariableCopyColumn;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.CategoricalVariableDerivationHelper;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.VariableDuplicationHelper;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class VariablesToViewPresenter extends PresenterWidget<VariablesToViewPresenter.Display> {

  private static final Translations translations = GWT.create(Translations.class);

  private TableDto table;

  private List<VariableDto> variables = new LinkedList<VariableDto>();

  private VariableCopyValidationHandler variableCopyValidationHandler;

  JsArray<DatasourceDto> datasources;

  @Inject
  public VariablesToViewPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  private void addEventHandlers() {
    // Event CopyVariablesToViewEvent
    registerHandler(
        getEventBus().addHandler(CopyVariablesToViewEvent.getType(), new CopyVariablesToViewEventHandler()));

    // Remove action
    getView().getActions().setActionHandler(new ActionHandler<VariableDto>() {
      @Override
      public void doAction(VariableDto object, String actionName) {
        if(actionName.equals(ActionsVariableCopyColumn.REMOVE_ACTION)) {
          getView().removeVariable(object);
        }
      }
    });

    // Recode category
    registerHandler(getView().getRenameButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // Prepare the array of variableDto
        boolean renameCategory = getView().isRenameSelected();

        JsArray<VariableDto> derivedVariables = JsArrays.create();
        if(renameCategory) {
          for(VariableDto variable : variables) {
            // Keep new name if changed
            derivedVariables.push(getDerivedVariable(variable, renameCategory));
          }
        } else {
          for(VariableDto variable : variables) {
            // Keep new name if changed
            derivedVariables.push(new VariableDuplicationHelper(variable).getDerivedVariable());
          }
        }
//        getView().clear();
        getView().renderRows(variables, derivedVariables);

      }
    }));
    // Save button
    registerHandler(getView().getSaveButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        variableCopyValidationHandler = new VariableCopyValidationHandler(getEventBus());
        if(variableCopyValidationHandler.validate()) {
          createOrUpdateViewWithVariables();
        }
      }

      private void createOrUpdateViewWithVariables() {
        ViewDto view = ViewDtoBuilder.newBuilder().setName(getView().getViewName().getText()).fromTables(table)
            .defaultVariableListView().build();
        VariableListViewDto derivedVariables = (VariableListViewDto) view
            .getExtension(VariableListViewDto.ViewDtoExtensions.view);

        JsArray<VariableDto> variablesDto = JsArrays.create();
        for(VariableDto v : getView().getVariables()) {
          // Push with the right name if it was changed
          GWT.log("NAME " + v.getName());
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

    }));

    // Cancel button
    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));
  }

  private VariableDto getDerivedVariable(VariableDto variable, boolean recodeName) {
    if(VariableDtos.hasCategories(variable) && ("text".equals(variable.getValueType()) ||
        "integer".equals(variable.getValueType()) && !VariableDtos.allCategoriesMissing(variable))) {
      CategoricalVariableDerivationHelper d = new CategoricalVariableDerivationHelper(variable);
      d.initializeValueMapEntries(recodeName);
      return d.getDerivedVariable();
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
          .withCallback(callbackHandler, Response.SC_OK, Response.SC_CREATED, Response.SC_FORBIDDEN,
              Response.SC_INTERNAL_SERVER_ERROR)//
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

      JsArray<VariableDto> existingVariables = derivedVariables.getVariablesArray();
      for(int i = 0; i < variables.length(); i++) {
        int index = getVariableIndex(derivedVariables.getVariablesArray(), variables.get(i).getName());
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
      if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_CREATED) {
        getView().hideDialog();
        getEventBus().fireEvent(NotificationEvent.newBuilder().info(message).build());
        getEventBus().fireEvent(new DatasourceUpdatedEvent(view.getDatasourceName()));

      } else if(response.getStatusCode() == Response.SC_FORBIDDEN) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("UnauthorizedOperation").build());
      } else {
        try {
          ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          getEventBus().fireEvent(
              NotificationEvent.newBuilder().error(errorDto.getStatus()).args(errorDto.getArgumentsArray()).build());
        } catch(Exception nothing) {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
        }
      }
    }
  }

  class CopyVariablesToViewEventHandler implements CopyVariablesToViewEvent.Handler {

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

    @Override
    public void onVariableCopy(CopyVariablesToViewEvent event) {
      table = event.getTable();
      variables = event.getSelection();

      refreshDatasources();

      // Prepare the array of variableDto
      JsArray<VariableDto> derivedVariables = JsArrays.create();
      for(VariableDto variable : variables) {
        derivedVariables.push(new VariableDuplicationHelper(variable).getDerivedVariable());
      }

//      getView().clear();
      getView().renderRows(variables, derivedVariables);
      getView().showDialog();
    }
  }

  public interface Display extends PopupView {

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    void showDialog();

    void hideDialog();

    void setDatasources(JsArray<DatasourceDto> datasources, String name);

    void renderRows(List<VariableDto> originalVariables, JsArray<VariableDto> rows);

//    void clear();

    ActionsVariableCopyColumn<VariableDto> getActions();

    void removeVariable(VariableDto object);

    HasText getViewName();

    List<VariableDto> getVariables();

    String getDatasourceName();

    boolean isRenameSelected();

    HasClickHandlers getRenameButton();
  }

  private class VariableCopyValidationHandler extends AbstractValidationHandler {

    private VariableCopyValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getViewName(), "ViewNameRequired"));

        for(VariableDto v : getView().getVariables()) {
          validators.add(new ConditionValidator(variableNameEmptyCondition(v.getName()), "CopyVariableNameRequired"));

          @SuppressWarnings("unchecked")
          List<String> args = new ArrayList();
          args.add(v.getName());
          validators.add(new ConditionValidator(variableNameColonCondition(v.getName()),
              TranslationsUtils.replaceArguments(translations.userMessageMap().get("CopyVariableNameColon"), args)));

        }

        validators.add(new VariableNameUniqueCondition(getView().getVariables()));

      }
      return validators;
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
  }

  private static class VariableNameUniqueCondition extends AbstractFieldValidator {

    List<VariableDto> variables;

    private VariableNameUniqueCondition(List<VariableDto> variables) {
      super("CopyVariableNameAlreadyExists");
      this.variables = variables;
    }

    @Override
    protected boolean hasError() {
      Collection<String> names = new HashSet<String>();

      for(VariableDto var : variables) {
        if(!names.add(var.getName())) {
          List<String> args = new ArrayList<String>();
          args.add(var.getName());
          setArgs(args);
          return true;
        }
      }
      return false;
    }
  }
}
