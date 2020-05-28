/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;

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
public class IdentifiersTableModalPresenter extends ModalPresenterWidget<IdentifiersTableModalPresenter.Display>
    implements IdentifiersTableModalUiHandlers {

  private final Translations translations;

  private final ValidationHandler validationHandler;

  @Inject
  public IdentifiersTableModalPresenter(EventBus eventBus, Display display, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    validationHandler = new PropertiesValidationHandler();
    getView().setUiHandlers(this);
  }

  @Override
  public void onSave(String entityType) {
    if(!validationHandler.validate()) return;

    final TableDto newTable = getTableDto(entityType);

    // make sure it does not exist
    String uri = UriBuilders.IDENTIFIERS_TABLES.create().build();
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder() //
        .forResource(uri) //
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            for(TableDto table : JsArrays.toList(resource)) {
              if(table.getEntityType().toLowerCase().equals(newTable.getEntityType().toLowerCase())) {
                // identifiers table for this entity type already exists
                getView().showError(translations.tableNameAlreadyExists(), Display.FormField.ENTITY_TYPE);
                return;
              }
            }
            doCreate(newTable);
          }
        }) //
        .get().send();
  }

  public void doCreate(TableDto newTable) {
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.IDENTIFIERS_TABLES.create().build()) //
        .post() //
        .withResourceBody(TableDto.stringify(newTable)) //
        .withCallback(new TableCreateCallback(newTable), Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR,
            Response.SC_CREATED).send();
  }

  private TableDto getTableDto(String entityType) {
    TableDto t = TableDto.create();
    t.setName(entityType);
    t.setEntityType(entityType);

    return t;
  }

  public interface Display extends PopupView, HasUiHandlers<IdentifiersTableModalUiHandlers> {

    enum FormField {
      ENTITY_TYPE
    }

    void showError(String message, @Nullable FormField id);

    HasText getEntityType();
  }

  private class TableCreateCallback implements ResponseCodeCallback {

    private TableDto table;

    private TableCreateCallback(TableDto table) {
      this.table = table;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_CREATED) {
        getView().hide();
        fireEvent(new IdentifiersTableSelectionEvent.Builder().dto(table).build());
      } else {
        getView().showError(response.getText(), null);
      }
    }

  }

  private class PropertiesValidationHandler extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
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
