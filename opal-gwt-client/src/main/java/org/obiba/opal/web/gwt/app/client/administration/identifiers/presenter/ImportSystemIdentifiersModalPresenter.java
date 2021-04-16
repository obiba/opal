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
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;

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
public class ImportSystemIdentifiersModalPresenter
    extends ModalPresenterWidget<ImportSystemIdentifiersModalPresenter.Display>
    implements ImportSystemIdentifiersModalUiHandlers {

  private final ValidationHandler validationHandler;

  private TableDto table;

  @Inject
  public ImportSystemIdentifiersModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
    validationHandler = new PropertiesValidationHandler();
    getView().setUiHandlers(this);
  }

  public void initialize(TableDto table) {
    this.table = table;
  }

  @Override
  public void onSubmit(String identifiers) {
    if(!validationHandler.validate()) return;

    getView().setBusy(true);
    String uri = UriBuilder.create().segment("identifiers", "mappings", "entities", "_import")
        .query("type", table.getEntityType()).build();
    ResourceRequestBuilderFactory.newBuilder().forResource(uri) //
        .post() //
        .withBody("text/plain", identifiers) //
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
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
  }

  public interface Display extends PopupView, HasUiHandlers<ImportSystemIdentifiersModalUiHandlers> {

    enum FormField {
      IDENTIFIERS
    }

    void setBusy(boolean busy);

    void showError(String message, @Nullable FormField id);

    HasText getIdentifiers();
  }

  private class PropertiesValidationHandler extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
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
