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

import org.obiba.opal.web.gwt.app.client.navigator.presenter.CodingViewDialogPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorTreePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.view.CodingViewDialogView;
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
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveBooleanVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveCategoricalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveCustomVariablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveNumericalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveOpenTextualVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveTemporalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveBooleanVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveCategoricalVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveCustomVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveNumericalVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveOpenTextualVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveTemporalVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveVariableView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
public class NavigatorModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    // Bind concrete implementations to interfaces
    bindPresenter(NavigatorPresenter.class, NavigatorPresenter.Display.class, NavigatorView.class, NavigatorPresenter.Proxy.class);

    // Don't bind as singleton because the ApplicationPresenter expects a new instance on every display
    bind(NavigatorTreePresenter.Display.class).to(NavigatorTreeView.class);
    bind(TablePresenter.Display.class).to(TableView.class);
    bind(VariablePresenter.Display.class).to(VariableView.class);

    configureDatasourcePresenters();

    configureDeriveVariablePresenters();
  }

  private void configureDatasourcePresenters() {
    bind(CreateDatasourcePresenter.Display.class).to(CreateDatasourceView.class);
    bind(DatasourcePresenter.Display.class).to(DatasourceView.class);
    bind(CreateDatasourceConclusionStepPresenter.Display.class).to(CreateDatasourceConclusionStepView.class);
    bind(HibernateDatasourceFormPresenter.Display.class).to(HibernateDatasourceFormView.class);
    bind(ExcelDatasourceFormPresenter.Display.class).to(ExcelDatasourceFormView.class);
    bind(FsDatasourceFormPresenter.Display.class).to(FsDatasourceFormView.class);
    bind(JdbcDatasourceFormPresenter.Display.class).to(JdbcDatasourceFormView.class);
    bind(CsvDatasourceFormPresenter.Display.class).to(CsvDatasourceFormView.class);
  }

  private void configureDeriveVariablePresenters() {
    bind(DeriveVariablePresenter.Display.class).to(DeriveVariableView.class);
    bind(DeriveCategoricalVariableStepPresenter.Display.class).to(DeriveCategoricalVariableStepView.class);
    bind(DeriveNumericalVariableStepPresenter.Display.class).to(DeriveNumericalVariableStepView.class);
    bind(DeriveBooleanVariableStepPresenter.Display.class).to(DeriveBooleanVariableStepView.class);
    bind(DeriveTemporalVariableStepPresenter.Display.class).to(DeriveTemporalVariableStepView.class);
    bind(DeriveOpenTextualVariableStepPresenter.Display.class).to(DeriveOpenTextualVariableStepView.class);
    bind(DeriveCustomVariablePresenter.Display.class).to(DeriveCustomVariableStepView.class);
    bind(CodingViewDialogPresenter.Display.class).to(CodingViewDialogView.class);
  }

}
