/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.syncidentifiers.presenter;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableIdentifiersSync;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class IdentifiersSyncPresenter extends WizardPresenterWidget<IdentifiersSyncPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  public static class Wizard extends WizardProxy<IdentifiersSyncPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<IdentifiersSyncPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  public interface Display extends WizardView {

    String getSelectedDatasource();

    void setDatasources(JsArray<DatasourceDto> datasources);

    void setTableIdentifiersSync(JsArray<TableIdentifiersSync> tableIdentifiersSyncs);

    void setTableIdentifiersSyncRequestHandler(StepInHandler handler);

    List<String> getSelectedTables();

    void setProgress(boolean progress);
  }

  @Inject
  public IdentifiersSyncPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    super.onBind();

    getView().setTableIdentifiersSyncRequestHandler(new StepInHandler() {

      @Override
      public void onStepIn() {
        UriBuilder ub = getDatasourceEntitiesUri();

        ResourceRequestBuilderFactory.<JsArray<TableIdentifiersSync>>newBuilder().forResource(ub.build()).get()
            .withCallback(new ResourceCallback<JsArray<TableIdentifiersSync>>() {

              @Override
              public void onResource(Response response, JsArray<TableIdentifiersSync> resource) {
                getView().setTableIdentifiersSync(JsArrays.toSafeArray(resource));
              }

            }).send();

      }
    });
  }

  @Override
  protected void onReveal() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            JsArray<DatasourceDto> datasources = JsArrays.create();
            if(resource != null) {
              for(int i = 0; i < resource.length(); i++) {
                DatasourceDto ds = resource.get(i);
                if(ds.getTableArray() != null && ds.getTableArray().length() > 0) {
                  datasources.push(ds);
                }
              }
            }
            getView().setDatasources(datasources);
          }

        }).send();
  }

  @Override
  protected void onFinish() {
    final List<String> selectedTables = getView().getSelectedTables();
    if(selectedTables.size() == 0) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("TableSelectionIsRequired").build());
      return;
    }

    ResponseCodeCallback callback = new IdentifiersImportationCallback();

    UriBuilder ub = getDatasourceEntitiesUri();
    for(String table : selectedTables) {
      ub.query("table", table);
    }
    ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).post().withCallback(Response.SC_OK, callback)//
        .withCallback(Response.SC_BAD_REQUEST, callback)//
        .withCallback(Response.SC_NOT_FOUND, callback)//
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callback)//
        .send();

    getView().setProgress(true);
  }

  private UriBuilder getDatasourceEntitiesUri() {
    return UriBuilder.create().segment("functional-units", "entities", "sync")
        .query("datasource", getView().getSelectedDatasource());
  }

  private final class IdentifiersImportationCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hide();
      getView().setProgress(false);
      if(response.getStatusCode() == Response.SC_OK) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().info("IdentifiersImportationCompleted").build());
      } else {
        try {
          ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          getEventBus().fireEvent(
              NotificationEvent.newBuilder().error(errorDto.getStatus()).args(errorDto.getArgumentsArray()).build());
        } catch(Exception e) {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
        }
      }
    }
  }

}
