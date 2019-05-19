/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.table.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

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
public class TablePropertiesModalPresenter extends ModalPresenterWidget<TablePropertiesModalPresenter.Display>
    implements TablePropertiesModalUiHandlers {

  private final Translations translations;

  private final PlaceManager placeManager;

  private String datasource;

  private TableDto table;

  private final ValidationHandler validationHandler;

  @Inject
  public TablePropertiesModalPresenter(EventBus eventBus, Display display, Translations translations,
      PlaceManager placeManager) {
    super(eventBus, display);
    this.translations = translations;
    this.placeManager = placeManager;
    validationHandler = new PropertiesValidationHandler();
    getView().setUiHandlers(this);
  }

  /**
   * Will create a new table in the given datasource.
   *
   * @param table
   */
  public void initialize(DatasourceDto datasource) {
    this.datasource = datasource.getName();
  }

  /**
   * Will update given table.
   *
   * @param datasource
   * @param table
   */
  public void initialize(TableDto table) {
    datasource = table.getDatasourceName();
    this.table = table;
    getView().renderProperties(table);
  }

  @Override
  public void onSave(String name, String entityType) {
    if(!validationHandler.validate()) return;

    TableDto newTable = getTableDto(name, entityType);

    if(table == null) {
      onCreate(newTable);
    } else {
      onUpdate(newTable);
    }
  }

  private void onUpdate(TableDto updatedTable) {
    UriBuilder uriBuilder = UriBuilders.DATASOURCE_TABLE.create();

    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build(datasource, table.getName())) //
        .put() //
        .withResourceBody(TableDto.stringify(updatedTable)) //
        .withCallback(new TableUpdateCallback(updatedTable), Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR,
            Response.SC_OK).send();
  }

  private void onCreate(final TableDto newTable) {
    // make sure it does not exist
    ResourceRequestBuilderFactory.newBuilder().forResource(
        UriBuilders.DATASOURCE_TABLE.create().build(datasource, newTable.getName())) //
        .get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if(response.getStatusCode() == Response.SC_NOT_FOUND) {
              doCreate(newTable);
            } else if(response.getStatusCode() == Response.SC_OK) {
              getView().showError(translations.tableNameAlreadyExists(), Display.FormField.NAME);
            }
          }
        }, Response.SC_OK, Response.SC_NOT_FOUND)//
        .send();
  }

  public void doCreate(TableDto newTable) {
    UriBuilder uriBuilder = UriBuilders.DATASOURCE_TABLES.create();

    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build(datasource)) //
        .post() //
        .withResourceBody(TableDto.stringify(newTable)) //
        .withCallback(new TableCreateCallback(newTable), Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR,
            Response.SC_CREATED).send();
  }

  private TableDto getTableDto(String name, String entityType) {
    TableDto t = TableDto.create();
    t.setName(name);
    t.setEntityType(entityType);

    return t;
  }

  public interface Display extends PopupView, HasUiHandlers<TablePropertiesModalUiHandlers> {

    enum FormField {
      NAME, ENTITY_TYPE
    }

    void renderProperties(TableDto table);

    void showError(String message, @Nullable FormField id);

    HasText getName();

    HasText getEntityType();
  }

  private class TableCreateCallback extends TableUpdateCallback {

    private TableCreateCallback(TableDto tableDto) {
      super(tableDto);
    }

    @Override
    protected void onSuccess() {
      placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(datasource, updatedTable.getName()));
    }
  }

  private class TableUpdateCallback implements ResponseCodeCallback {

    protected final TableDto updatedTable;

    private TableUpdateCallback(TableDto tableDto) {
      updatedTable = tableDto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_CREATED) {
        getView().hide();
        onSuccess();
      } else {
        getView().showError(response.getText(), null);
      }
    }

    protected void onSuccess() {
      placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(datasource, updatedTable.getName()));
    }
  }

  private class PropertiesValidationHandler extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired", Display.FormField.NAME.name()));
      validators.add(new RequiredTextValidator(getView().getEntityType(), "EntityTypeIsRequired",
          Display.FormField.ENTITY_TYPE.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(message, Display.FormField.valueOf(id));
    }
  }

}
