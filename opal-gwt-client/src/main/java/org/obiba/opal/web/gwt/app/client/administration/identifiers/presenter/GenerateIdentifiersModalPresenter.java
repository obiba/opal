/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.event.IdentifiersTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

/**
 *
 */
public class GenerateIdentifiersModalPresenter extends ModalPresenterWidget<GenerateIdentifiersModalPresenter.Display>
    implements GenerateIdentifiersModalUiHandlers {

  //
  // Instance Variables
  //

  private TableDto table;

  private VariableDto variable;

  //
  // Constructors
  //

  @Inject
  public GenerateIdentifiersModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void initialize(VariableDto variable, final TableDto table) {
    this.variable = variable;
    this.table = table;

    String prefix = variable.getName().toUpperCase() + "-";
    getView().setDefault(10, prefix);

    UriBuilder ub = UriBuilder.create().segment("identifiers", "mapping", "{}", "_count")
        .query("type", table.getEntityType());
    ResourceRequestBuilderFactory.newBuilder().forResource(ub.build(variable.getName())).get()//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            try {
              getView().setAffectedEntities(table.getValueSetCount() - Integer.parseInt(response.getText()));
            } catch(Exception e) {
              GWT.log("Unable to get identifiers mapping count: " + e.getMessage());
            }
          }
        }, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_NOT_FOUND, Response.SC_BAD_REQUEST).send();
  }

  @Override
  public void generateIdentifiers(Number size, boolean allowZeros, String prefix) {
    getView().setBusy(true);

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK) {
          int count = 0;
          try {
            count = Integer.parseInt(response.getText());
          } catch(NumberFormatException ignored) {
          }
          if(count > 0) {
            fireEvent(NotificationEvent.newBuilder().info("IdentifiersGenerationCompleted")
                .args(variable.getName(), response.getText()).build());
          } else {
            fireEvent(NotificationEvent.newBuilder().info("NoIdentifiersGenerated").args(variable.getName()).build());
          }
        } else {
          fireEvent(
              NotificationEvent.newBuilder().error("IdentifiersGenerationFailed").args(variable.getName()).build());
        }
        getView().hideDialog();
        fireEvent(new IdentifiersTableSelectionEvent.Builder().dto(table).build());
      }

    };

    UriBuilder ub = UriBuilder.create().segment("identifiers", "mapping", "{}", "_generate")
        .query("type", table.getEntityType(), "size", String.valueOf(size), "zeros", String.valueOf(allowZeros),
            "prefix", prefix);
    ResourceRequestBuilderFactory.newBuilder().forResource(ub.build(variable.getName())).post()//
        .withCallback(callbackHandler,
            Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR,//
            Response.SC_NOT_FOUND) //
        .send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends PopupView, HasUiHandlers<GenerateIdentifiersModalUiHandlers> {

    void hideDialog();

    void setAffectedEntities(int count);

    void setBusy(boolean busy);

    void setDefault(int size, String prefix);
  }
}
