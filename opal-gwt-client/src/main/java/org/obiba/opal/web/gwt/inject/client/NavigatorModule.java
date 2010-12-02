/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.inject.client;

import org.obiba.opal.web.gwt.app.client.navigator.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorTreePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.view.DatasourceView;
import org.obiba.opal.web.gwt.app.client.navigator.view.NavigatorTreeView;
import org.obiba.opal.web.gwt.app.client.navigator.view.NavigatorView;
import org.obiba.opal.web.gwt.app.client.navigator.view.TableView;
import org.obiba.opal.web.gwt.app.client.navigator.view.VariableView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourceConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CsvDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.ExcelDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.FsDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.HibernateDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.JdbcDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view.CreateDatasourceConclusionStepView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view.CreateDatasourceView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view.CsvDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view.ExcelDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view.FsDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view.HibernateDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view.JdbcDatasourceFormView;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 *
 */
public class NavigatorModule extends AbstractGinModule {

  @Override
  protected void configure() {
    // Bind concrete implementations to interfaces
    bind(NavigatorPresenter.Display.class).to(NavigatorView.class).in(Singleton.class);
    bind(CreateDatasourcePresenter.Display.class).to(CreateDatasourceView.class);

    // Don't bind as singleton because the ApplicationPresenter expects a new instance on every display
    bind(NavigatorTreePresenter.Display.class).to(NavigatorTreeView.class);
    bind(DatasourcePresenter.Display.class).to(DatasourceView.class);
    bind(TablePresenter.Display.class).to(TableView.class);
    bind(VariablePresenter.Display.class).to(VariableView.class);

    bind(CreateDatasourceConclusionStepPresenter.Display.class).to(CreateDatasourceConclusionStepView.class);
    bind(HibernateDatasourceFormPresenter.Display.class).to(HibernateDatasourceFormView.class);
    bind(ExcelDatasourceFormPresenter.Display.class).to(ExcelDatasourceFormView.class);
    bind(FsDatasourceFormPresenter.Display.class).to(FsDatasourceFormView.class);
    bind(JdbcDatasourceFormPresenter.Display.class).to(JdbcDatasourceFormView.class);
    bind(CsvDatasourceFormPresenter.Display.class).to(CsvDatasourceFormView.class);
  }

}
