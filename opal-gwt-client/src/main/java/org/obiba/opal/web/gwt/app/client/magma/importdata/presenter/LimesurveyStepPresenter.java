/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.SqlSettingsDto;

import com.google.gwt.core.client.JsArray;
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
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder().forResource("/system/databases/sql").withCallback(
        new ResourceCallback<JsArray<DatabaseDto>>() {

          @Override
          public void onResource(Response response, JsArray<DatabaseDto> resource) {
            JsArray<DatabaseDto> databases = JsArrays.create();
            for(int i = 0; i < resource.length(); i++) {
              SqlSettingsDto sqlDatabaseDto = resource.get(i).getSqlSettings();
              if(sqlDatabaseDto.getSqlSchema().getName().equals(SqlSettingsDto.SqlSchema.LIMESURVEY.getName())) {
                databases.push(resource.get(i));
              }
            }
            getView().setDatabases(databases);
          }
        })//
        .withCallback(Response.SC_FORBIDDEN, ResponseCodeCallback.NO_OP).get().send();
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
    // LimeSurvey format is removed is no LimeSurvey database exists
    return true;
  }

  public interface Display extends View, WizardStepDisplay {

    void setDatabases(JsArray<DatabaseDto> resource);

    String getSelectedDatabase();

    String getTablePrefix();
  }
}
