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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.google.common.base.Strings;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 *
 */
public class PropertiesEditorModalPresenter extends ModalPresenterWidget<PropertiesEditorModalPresenter.Display>
    implements PropertiesEditorModalUiHandlers {

  private final Translations translations;

  private final PlaceManager placeManager;

  private VariableDto variable;

  private TableDto tableDto;

  @Inject
  public PropertiesEditorModalPresenter(EventBus eventBus, Display display, Translations translations,
      PlaceManager placeManager) {
    super(eventBus, display);
    this.translations = translations;
    this.placeManager = placeManager;
  }

  public void initialize(VariableDto dto, TableDto table) {
    variable = dto;
    tableDto = table;

    getView().setUiHandlers(this);
    getView().renderProperties(dto, table.hasViewLink());
  }

  @Override
  public void onSave(String name, String valueType, boolean repeatable, String unit, String mimeType,
      String occurrenceGroup, String referencedEntityType) {
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

  public void onCreate(VariableDto newVariable) {
    UriBuilder uriBuilder;
    if(Strings.isNullOrEmpty(tableDto.getViewLink())) {
      uriBuilder = UriBuilders.DATASOURCE_TABLE_VARIABLES.create();
    } else {
      // variable from a view
      uriBuilder = UriBuilders.DATASOURCE_VIEW_VARIABLES.create().query("comment",
          TranslationsUtils.replaceArguments(translations.updateVariableProperties(), variable.getName()));
    }

    ResourceRequestBuilderFactory.newBuilder()
        .forResource(uriBuilder.build(tableDto.getDatasourceName(), tableDto.getName())) //
        .post() //
        .withResourceBody(VariableDto.stringify(newVariable)).accept("application/json") //
        .withCallback(new VariableCreateUpdateCallback(newVariable), Response.SC_BAD_REQUEST,
            Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
  }

  private VariableDto getVariableDto(String name, String valueType, boolean repeatable, String unit, String mimeType,
      String occurrenceGroup, String referencedEntityType) {
    VariableDto v = VariableDto.create();
    v.setIsNewVariable(variable == null);
    if(variable != null) {
      v.setLink(variable.getLink());
      v.setIndex(variable.getIndex());

      v.setParentLink(variable.getParentLink());
      v.setName(variable.getName());
      v.setEntityType(variable.getEntityType());
      v.setValueType(variable.getValueType());

      if(variable.getAttributesArray().length() > 0) {
        v.setAttributesArray(variable.getAttributesArray());
      }

      if(variable.getCategoriesArray().length() > 0) {
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
    v.setOccurrenceGroup(occurrenceGroup);
    return v;
  }

  public interface Display extends PopupView, HasUiHandlers<PropertiesEditorModalUiHandlers> {
    void renderProperties(VariableDto variable, boolean derived);

    void showError(String message, @Nullable ControlGroup group);
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
      } else {
        getView().showError(response.getText(), null);
      }
      placeManager.revealPlace(ProjectPlacesHelper
          .getVariablePlace(tableDto.getDatasourceName(), tableDto.getName(), updatedVariable.getName()));
    }
  }
}
