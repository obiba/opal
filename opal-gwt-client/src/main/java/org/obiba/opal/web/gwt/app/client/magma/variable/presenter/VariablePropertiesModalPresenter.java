/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.variable.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 *
 */
public class VariablePropertiesModalPresenter extends ModalPresenterWidget<VariablePropertiesModalPresenter.Display>
    implements VariablePropertiesModalUiHandlers {

  private final Translations translations;

  private final PlaceManager placeManager;

  private VariableDto variable;

  private TableDto tableDto;

  private final ValidationHandler validationHandler;

  @Inject
  public VariablePropertiesModalPresenter(EventBus eventBus, Display display, Translations translations,
      PlaceManager placeManager) {
    super(eventBus, display);
    this.translations = translations;
    this.placeManager = placeManager;
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

    getView().renderProperties(dto, variable == null || table.hasViewLink(),
        table.hasViewLink() || (table.hasValueSetCount() && table.getValueSetCount() == 0));
  }

  @Override
  public void onSave(String name, String valueType, boolean repeatable, String unit, String mimeType,
      String occurrenceGroup, String referencedEntityType) {
    if(!validationHandler.validate()) return;

    VariableDto newVariable = getVariableDto(name, valueType, repeatable, unit, mimeType, occurrenceGroup,
        referencedEntityType);

    if(variable != null) {
      onUpdate(newVariable);
    } else {
      onCreate(newVariable);
    }
  }

  public void onUpdate(VariableDto updatedVariable) {
    UriBuilder uriBuilder;
    if(Strings.isNullOrEmpty(tableDto.getViewLink())) {
      uriBuilder = UriBuilders.DATASOURCE_TABLE_VARIABLE.create();
    } else {
      // variable from a view
      uriBuilder = UriBuilders.DATASOURCE_VIEW_VARIABLE.create().query("comment",
          TranslationsUtils.replaceArguments(translations.updateVariableProperties(), variable.getName()));
    }

    ResourceRequestBuilderFactory.newBuilder()
        .forResource(uriBuilder.build(tableDto.getDatasourceName(), tableDto.getName(), variable.getName())) //
        .put() //
        .withResourceBody(VariableDto.stringify(updatedVariable)).accept("application/json") //
        .withCallback(new VariableCreateUpdateCallback(updatedVariable), Response.SC_BAD_REQUEST,
            Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
  }

  public void onCreate(final VariableDto newVariable) {
    // make sure it does not exist
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASOURCE_TABLE_VARIABLE.create()
        .build(tableDto.getDatasourceName(), tableDto.getName(), newVariable.getName())) //
        .get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == Response.SC_NOT_FOUND) {
              doCreate(newVariable);
            } else if(response.getStatusCode() == Response.SC_OK) {
              getView().showError(translations.variableNameAlreadyExists(), Display.FormField.NAME);
            }
          }
        }, Response.SC_OK, Response.SC_NOT_FOUND)//
        .send();
  }

  public void doCreate(VariableDto newVariable) {
    UriBuilder uriBuilder;
    if(Strings.isNullOrEmpty(tableDto.getViewLink())) {
      uriBuilder = UriBuilders.DATASOURCE_TABLE_VARIABLES.create();
    } else {
      // variable from a view
      uriBuilder = UriBuilders.DATASOURCE_VIEW_VARIABLES.create()
          .query("comment", TranslationsUtils.replaceArguments(translations.createVariable(), newVariable.getName()));
    }

    ResourceRequestBuilderFactory.newBuilder()
        .forResource(uriBuilder.build(tableDto.getDatasourceName(), tableDto.getName())) //
        .post() //
        .withResourceBody("[" + VariableDto.stringify(newVariable) + "]").accept("application/json") //
        .withCallback(new VariableCreateUpdateCallback(newVariable), Response.SC_BAD_REQUEST,
            Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
  }

  private VariableDto getVariableDto(String name, String valueType, boolean repeatable, String unit, String mimeType,
      String occurrenceGroup, String referencedEntityType) {
    VariableDto v = VariableDto.create();
    v.setIsNewVariable(variable == null);
    v.setEntityType(tableDto.getEntityType());
    if(variable != null) {
      v.setLink(variable.getLink());
      v.setIndex(variable.getIndex());

      v.setParentLink(variable.getParentLink());
      v.setName(variable.getName());
      v.setValueType(variable.getValueType());

      if(variable.getAttributesArray() != null && variable.getAttributesArray().length() > 0) {
        v.setAttributesArray(variable.getAttributesArray());
      }

      if(variable.getCategoriesArray() != null && variable.getCategoriesArray().length() > 0) {
        v.setCategoriesArray(variable.getCategoriesArray());
      }
    }

    // Update info from view
    v.setName(name);
    v.setValueType(valueType);
    v.setUnit(unit);
    v.setIsRepeatable(repeatable);
    v.setReferencedEntityType(referencedEntityType);
    v.setMimeType(mimeType);
    v.setOccurrenceGroup(repeatable ? occurrenceGroup : "");
    return v;
  }

  public interface Display extends PopupView, HasUiHandlers<VariablePropertiesModalUiHandlers> {

    enum FormField {
      NAME
    }

    void renderProperties(VariableDto variable, boolean modifyName, boolean modifyValueType);

    void showError(String message, @Nullable FormField id);

    HasText getVariableName();
  }

  private class VariableCreateUpdateCallback implements ResponseCodeCallback {

    private final VariableDto updatedVariable;

    private VariableCreateUpdateCallback(VariableDto updatedVariable) {
      this.updatedVariable = updatedVariable;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_OK) {
        getView().hide();
        placeManager.revealPlace(ProjectPlacesHelper
            .getVariablePlace(tableDto.getDatasourceName(), tableDto.getName(), updatedVariable.getName()));
      } else {
        getView().showError(response.getText(), null);
      }
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
