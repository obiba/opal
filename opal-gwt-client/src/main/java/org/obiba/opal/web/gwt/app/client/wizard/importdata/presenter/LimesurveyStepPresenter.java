/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;

public class LimesurveyStepPresenter extends PresenterWidget<LimesurveyStepPresenter.Display> implements
    DataImportPresenter.DataImportFormatStepPresenter {

  @Inject
  public LimesurveyStepPresenter(EventBus eventBus, LimesurveyStepPresenter.Display view) {
    super(eventBus, view);
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  @Override
  public void onReveal() {
    super.onReveal();
    ResourceRequestBuilderFactory
        .<JsArray<JdbcDataSourceDto>>newBuilder().forResource("/jdbc/databases")
        .withCallback(new ResourceCallback<JsArray<JdbcDataSourceDto>>() {

          @Override
          public void onResource(Response response, JsArray<JdbcDataSourceDto> resource) {
            getView().setDatabases(resource);
          }
        }).get().send();
  }

  @Override
  public ImportData getImportData() {
    ImportData importData = new ImportData();
    importData.setImportFormat(ImportFormat.LIMESURVEY);
    importData.setDatabase(getView().getSelectedDatabase());
    importData.setTablePrefix(getView().getTablePrefix());
    return importData;
  }

  @Override
  public boolean validate() {
    return true;
  }

  public interface Display extends View, WizardStepDisplay {

    void setDatabases(JsArray<JdbcDataSourceDto> resource);

    String getSelectedDatabase();

    String getTablePrefix();
  }
}
