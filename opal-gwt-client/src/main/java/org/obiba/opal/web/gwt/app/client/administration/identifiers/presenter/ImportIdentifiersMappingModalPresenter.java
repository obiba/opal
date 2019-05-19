/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.event.IdentifiersTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

/**
 *
 */
public class ImportIdentifiersMappingModalPresenter
    extends ModalPresenterWidget<ImportIdentifiersMappingModalPresenter.Display>
    implements ImportIdentifiersMappingModalUiHandlers {

  private final ValidationHandler validationHandler;

  private TableDto table;

  private JsArray<VariableDto> variables;

  @Inject
  public ImportIdentifiersMappingModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
    validationHandler = new PropertiesValidationHandler();
    getView().setUiHandlers(this);
  }

  public void initialize(TableDto table) {
    this.table = table;

    String uri = UriBuilders.IDENTIFIERS_TABLE_VARIABLES.create().build(table.getName());
    ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder() //
        .forResource(uri) //
        .withCallback(new ResourceCallback<JsArray<VariableDto>>() {
          @Override
          public void onResource(Response response, JsArray<VariableDto> resource) {
            variables = JsArrays.toSafeArray(resource);
            getView().setVariables(variables);
          }
        }) //
        .get().send();
  }

  @Override
  public void onSubmit(String name, String systemIdentifiers, String identifiers) {
    if(!validate(systemIdentifiers, identifiers)) return;

    getView().setBusy(true);
    // create variable if missing
    if(variables == null || variables.length() == 0) {
      doCreateAndImport(name, systemIdentifiers, identifiers);
    } else {
      for(VariableDto variable : JsArrays.toIterable(variables)) {
        if(variable.getName().equals(name)) {
          doImport(name, systemIdentifiers, identifiers);
          return;
        }
      }
      doCreateAndImport(name, systemIdentifiers, identifiers);
    }
  }

  private boolean validate(String systemIdentifiers, String identifiers) {
    if(!validationHandler.validate()) return false;

    String[] systemIds = systemIdentifiers.split("\\n");
    String[] mappedIds = identifiers.split("\\n");

    if(systemIds.length > mappedIds.length) {
      getView().showError("IdentifiersAreMissing", Display.FormField.IDENTIFIERS);
      return false;
    }

    if(systemIds.length < mappedIds.length) {
      getView().showError("SystemIdentifiersAreMissing", Display.FormField.SYSTEM_IDENTIFIERS);
      return false;
    }

    return true;
  }

  private void doCreateAndImport(final String name, final String systemIdentifiers, final String identifiers) {
    VariableDto newVariable = VariableDto.create();
    newVariable.setName(name);
    newVariable.setIsNewVariable(true);
    newVariable.setEntityType(table.getEntityType());
    newVariable.setIsRepeatable(false);
    newVariable.setValueType("text");

    UriBuilder uriBuilder = UriBuilders.IDENTIFIERS_TABLE_VARIABLES.create();
    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build(table.getName())) //
        .post() //
        .withResourceBody("[" + VariableDto.stringify(newVariable) + "]") //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == Response.SC_OK) {
              doImport(name, systemIdentifiers, identifiers);
            } else {
              getView().showError(response.getText(), null);
            }
          }
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
  }

  private void doImport(String name, String systemIdentifiers, String identifiers) {
    String[] systemIds = systemIdentifiers.split("\\n");
    String[] mappedIds = identifiers.split("\\n");

    // not empty and same length
    StringBuilder str = new StringBuilder();
    for(int i = 0; i < systemIds.length; i++) {
      String sId = systemIds[i].trim();
      String mId = mappedIds[i].trim();
      if(!Strings.isNullOrEmpty(sId) && !Strings.isNullOrEmpty(mId)) {
        str.append(sId).append(",").append(mId).append('\n');
      }
    }

    String uri = UriBuilder.create().segment("identifiers", "mapping", "{}", "_import")
        .query("type", table.getEntityType(), "separator", ",").build(name);
    ResourceRequestBuilderFactory.newBuilder().forResource(uri) //
        .post() //
        .withBody("text/plain", str.toString()) //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setBusy(false);
            if(response.getStatusCode() == Response.SC_OK) {
              getView().hide();
              fireEvent(new IdentifiersTableSelectionEvent.Builder().dto(table).build());
            } else {
              getView().showError(response.getText(), null);
            }
          }
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK, Response.SC_NOT_FOUND).send();
  }

  public interface Display extends PopupView, HasUiHandlers<ImportIdentifiersMappingModalUiHandlers> {

    enum FormField {
      NAME, SYSTEM_IDENTIFIERS, IDENTIFIERS
    }

    void setVariables(JsArray<VariableDto> variables);

    void setBusy(boolean busy);

    void showError(String message, @Nullable FormField id);

    HasText getSystemIdentifiers();

    HasText getIdentifiers();

    HasText getVariableName();
  }

  private class PropertiesValidationHandler extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators
          .add(new RequiredTextValidator(getView().getVariableName(), "NameIsRequired", Display.FormField.NAME.name()));
      validators.add(new RequiredTextValidator(getView().getSystemIdentifiers(), "SystemIdentifiersAreRequired",
          Display.FormField.SYSTEM_IDENTIFIERS.name()));
      validators.add(new RequiredTextValidator(getView().getIdentifiers(), "IdentifiersAreRequired",
          Display.FormField.IDENTIFIERS.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(message, Display.FormField.valueOf(id));
    }
  }

}
