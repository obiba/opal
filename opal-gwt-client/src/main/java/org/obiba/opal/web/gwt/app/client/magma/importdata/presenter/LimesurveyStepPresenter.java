/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.DatabaseDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

public class LimesurveyStepPresenter extends PresenterWidget<LimesurveyStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  @Inject
  public LimesurveyStepPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  @Override
  public void onReveal() {
    super.onReveal();
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder().forResource("/system/databases/sql")
        .withCallback(new ResourceCallback<JsArray<DatabaseDto>>() {

          @Override
          public void onResource(Response response, JsArray<DatabaseDto> resource) {
            getView().setDatabases(resource);
          }
        })//
        .withCallback(Response.SC_FORBIDDEN, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            // ignore
          }
        }).get().send();
  }

  @Override
  public ImportConfig getImportConfig() {
    ImportConfig importConfig = new ImportConfig();
    importConfig.setImportFormat(ImportFormat.LIMESURVEY);
    importConfig.setDatabase(getView().getSelectedDatabase());
    importConfig.setTablePrefix(getView().getTablePrefix());
    return importConfig;
  }

  @Override
  public boolean validate() {
    if(getView().getSelectedDatabase().isEmpty()) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error("LimeSurveyDatabaseIsRequired").build());
      return false;
    }
    return true;
  }

  public interface Display extends View, WizardStepDisplay {

    void setDatabases(JsArray<DatabaseDto> resource);

    String getSelectedDatabase();

    String getTablePrefix();
  }
}
