/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

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
public class IdentifiersMappingModalPresenter extends ModalPresenterWidget<IdentifiersMappingModalPresenter.Display>
    implements IdentifiersMappingModalUiHandlers {

  private final Translations translations;

  private VariableDto variable;

  private TableDto tableDto;

  private final ValidationHandler validationHandler;

  @Inject
  public IdentifiersMappingModalPresenter(EventBus eventBus, Display display, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    validationHandler = new PropertiesValidationHandler();
    getView().setUiHandlers(this);
  }

  /**
   * Will create a new variable in the given table.
   *
   * @param table
   */
  public void initialize(TableDto table) {
    initialize(null, table);
  }

  /**
   * Will update given variable in associated table.
   *
   * @param dto
   * @param table
   */
  public void initialize(VariableDto dto, TableDto table) {
    variable = dto;
    tableDto = table;

    getView().renderProperties(dto, variable == null);
  }

  @Override
  public void onSave() {
    if(!validationHandler.validate()) return;

    VariableDto newVariable = getVariableDto();

    if(variable != null) {
      onUpdate(newVariable);
    } else {
      onCreate(newVariable);
    }
  }

  public void onUpdate(VariableDto updatedVariable) {
    UriBuilder uriBuilder = UriBuilders.IDENTIFIERS_TABLE_VARIABLE.create();
    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build(tableDto.getName(), variable.getName())) //
        .put() //
        .withResourceBody(VariableDto.stringify(updatedVariable)) //
        .withCallback(new VariableUpdateCallback(updatedVariable), Response.SC_BAD_REQUEST,
            Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
  }

  public void onCreate(final VariableDto newVariable) {
    // make sure it does not exist
    ResourceRequestBuilderFactory.newBuilder().forResource(
        UriBuilders.IDENTIFIERS_TABLE_VARIABLE.create().build(tableDto.getName(), newVariable.getName())) //
        .get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == Response.SC_NOT_FOUND) {
              doCreate(newVariable);
            } else if(response.getStatusCode() == Response.SC_OK) {
              getView().showError(translations.identifiersMappingNameAlreadyExists(), Display.FormField.NAME);
            }
          }
        }, Response.SC_OK, Response.SC_NOT_FOUND)//
        .send();
  }

  public void doCreate(VariableDto newVariable) {
    UriBuilder uriBuilder;
    uriBuilder = UriBuilders.IDENTIFIERS_TABLE_VARIABLES.create();

    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build(tableDto.getName())) //
        .post() //
        .withResourceBody("[" + VariableDto.stringify(newVariable) + "]") //
        .withCallback(new VariableCreateCallback(newVariable), Response.SC_BAD_REQUEST,
            Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
  }

  private VariableDto getVariableDto() {
    VariableDto variableDto = VariableDto.create();
    variableDto.setIsNewVariable(variable == null);
    variableDto.setEntityType(tableDto.getEntityType());
    variableDto.setIsRepeatable(false);
    variableDto.setValueType("text");
    if(variable != null) {
      variableDto.setLink(variable.getLink());
      variableDto.setIndex(variable.getIndex());

      variableDto.setParentLink(variable.getParentLink());
      variableDto.setName(variable.getName());
      variableDto.setValueType(variable.getValueType());

      if(variable.getAttributesArray() != null && variable.getAttributesArray().length() > 0) {
        variableDto.setAttributesArray(variable.getAttributesArray());
      }

      if(variable.getCategoriesArray() != null && variable.getCategoriesArray().length() > 0) {
        variableDto.setCategoriesArray(variable.getCategoriesArray());
      }
    }

    // Update info from view
    updateInfoFromView(variableDto);
    return variableDto;
  }

  private void updateInfoFromView(VariableDto variableDto) {
    variableDto.setName(getView().getName());

    if(variable == null || variable.getAttributesArray() == null) {
      JsArray<AttributeDto> attributes = JsArrays.create().cast();
      variableDto.setAttributesArray(attributes);
    }

    AttributeDto labelAttr = null;
    if(variable != null) {
      for(AttributeDto attr : JsArrays.toIterable(variable.getAttributesArray())) {
        if(attr.getName().equals("description")) {
          labelAttr = attr;
          break;
        }
      }
    }

    if(labelAttr == null) {
      labelAttr = AttributeDto.create();
      labelAttr.setName("description");
      variableDto.getAttributesArray().push(labelAttr);
    }
    labelAttr.setValue(getView().getDescription());
  }

  public interface Display extends PopupView, HasUiHandlers<IdentifiersMappingModalUiHandlers> {

    String getName();

    String getDescription();

    enum FormField {
      NAME
    }

    void renderProperties(VariableDto variable, boolean modifyName);

    void showError(String message, @Nullable FormField id);

    HasText getVariableName();
  }

  private class VariableUpdateCallback implements ResponseCodeCallback {

    protected final VariableDto updatedVariable;

    private VariableUpdateCallback(VariableDto updatedVariable) {
      this.updatedVariable = updatedVariable;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_OK) {
        getView().hide();
        onSuccess();
      } else {
        getView().showError(response.getText(), null);
      }
    }

    protected void onSuccess() {
      fireEvent(new IdentifiersTableSelectionEvent.Builder().dto(tableDto).build());
    }
  }

  private class VariableCreateCallback extends VariableUpdateCallback {

    private VariableCreateCallback(VariableDto newVariable) {
      super(newVariable);
    }

    @Override
    protected void onSuccess() {
      fireEvent(new IdentifiersTableSelectionEvent.Builder().dto(tableDto).build());
    }
  }

  private class PropertiesValidationHandler extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators
          .add(new RequiredTextValidator(getView().getVariableName(), "NameIsRequired", Display.FormField.NAME.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(message, Display.FormField.valueOf(id));
    }
  }

}
