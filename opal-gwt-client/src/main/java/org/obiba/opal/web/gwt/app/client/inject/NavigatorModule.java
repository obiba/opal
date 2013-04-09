/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.navigator.presenter.CodingViewDialogPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.EntityDialogPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorTreePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.view.CodingViewDialogView;
import org.obiba.opal.web.gwt.app.client.navigator.view.DatasourceView;
import org.obiba.opal.web.gwt.app.client.navigator.view.EntityDialogView;
import org.obiba.opal.web.gwt.app.client.navigator.view.NavigatorTreeView;
import org.obiba.opal.web.gwt.app.client.navigator.view.NavigatorView;
import org.obiba.opal.web.gwt.app.client.navigator.view.TableView;
import org.obiba.opal.web.gwt.app.client.navigator.view.ValuesTableView;
import org.obiba.opal.web.gwt.app.client.navigator.view.VariableView;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.CsvDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.DatasourceFormPresenterSubscriber;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.ExcelDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.FsDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.HibernateDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.JdbcDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.NullDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.view.CsvDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.view.ExcelDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.view.FsDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.view.HibernateDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.view.JdbcDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.widgets.datasource.view.NullDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view.CreateDatasourceView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveBooleanVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveCategoricalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveConclusionPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveCustomVariablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveFromVariablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveNumericalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveOpenTextualVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveTemporalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveBooleanVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveCategoricalVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveConclusionView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveCustomVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveFromVariableView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveNumericalVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveOpenTextualVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveTemporalVariableStepView;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.DeriveVariableView;

import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
@SuppressWarnings("OverlyCoupledClass")
public class NavigatorModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindPresenter(NavigatorPresenter.class, NavigatorPresenter.Display.class, NavigatorView.class,
        NavigatorPresenter.Proxy.class);
    bindPresenter(NavigatorTreePresenter.class, NavigatorTreePresenter.Display.class, NavigatorTreeView.class,
        NavigatorTreePresenter.Proxy.class);
    bindPresenter(DatasourcePresenter.class, DatasourcePresenter.Display.class, DatasourceView.class,
        DatasourcePresenter.Proxy.class);
    bindPresenter(TablePresenter.class, TablePresenter.Display.class, TableView.class, TablePresenter.Proxy.class);
    bindPresenter(VariablePresenter.class, VariablePresenter.Display.class, VariableView.class,
        VariablePresenter.Proxy.class);
    bindPresenterWidget(ValuesTablePresenter.class, ValuesTablePresenter.Display.class, ValuesTableView.class);
    bindPresenterWidget(EntityDialogPresenter.class, EntityDialogPresenter.Display.class, EntityDialogView.class);

    bindWizardPresenterWidget(CreateDatasourcePresenter.class, CreateDatasourcePresenter.Display.class,
        CreateDatasourceView.class, CreateDatasourcePresenter.Wizard.class);

    bindWizardPresenterWidget(DeriveVariablePresenter.class, DeriveVariablePresenter.Display.class,
        DeriveVariableView.class, DeriveVariablePresenter.CategorizeWizard.class);
    // tricky case: one wizard presenter for 3 different types
    bind(DeriveVariablePresenter.CustomWizard.class).asEagerSingleton();
    bind(DeriveVariablePresenter.FromWizard.class).asEagerSingleton();

    bindDatasourceFormPresenter(ExcelDatasourceFormPresenter.class, ExcelDatasourceFormPresenter.Display.class,
        ExcelDatasourceFormView.class, ExcelDatasourceFormPresenter.Subscriber.class);
    bindDatasourceFormPresenter(HibernateDatasourceFormPresenter.class, HibernateDatasourceFormPresenter.Display.class,
        HibernateDatasourceFormView.class, HibernateDatasourceFormPresenter.Subscriber.class);
    bindDatasourceFormPresenter(FsDatasourceFormPresenter.class, FsDatasourceFormPresenter.Display.class,
        FsDatasourceFormView.class, FsDatasourceFormPresenter.Subscriber.class);
    bindDatasourceFormPresenter(JdbcDatasourceFormPresenter.class, JdbcDatasourceFormPresenter.Display.class,
        JdbcDatasourceFormView.class, JdbcDatasourceFormPresenter.Subscriber.class);
    bindDatasourceFormPresenter(CsvDatasourceFormPresenter.class, CsvDatasourceFormPresenter.Display.class,
        CsvDatasourceFormView.class, CsvDatasourceFormPresenter.Subscriber.class);
    bindDatasourceFormPresenter(NullDatasourceFormPresenter.class, NullDatasourceFormPresenter.Display.class,
        NullDatasourceFormView.class, NullDatasourceFormPresenter.Subscriber.class);

    configureDeriveVariablePresenters();
  }

  private <V extends View> void bindDatasourceFormPresenter(Class<? extends PresenterWidget<V>> presenter,
      Class<V> display, Class<? extends V> view, Class<? extends DatasourceFormPresenterSubscriber> subscriber) {
    bind(subscriber).asEagerSingleton();
    bindPresenterWidget(presenter, display, view);
  }

  private void configureDeriveVariablePresenters() {
    bindPresenterWidget(DeriveCategoricalVariableStepPresenter.class,
        DeriveCategoricalVariableStepPresenter.Display.class, DeriveCategoricalVariableStepView.class);
    bindPresenterWidget(DeriveNumericalVariableStepPresenter.class, DeriveNumericalVariableStepPresenter.Display.class,
        DeriveNumericalVariableStepView.class);
    bindPresenterWidget(DeriveBooleanVariableStepPresenter.class, DeriveBooleanVariableStepPresenter.Display.class,
        DeriveBooleanVariableStepView.class);
    bindPresenterWidget(DeriveTemporalVariableStepPresenter.class, DeriveTemporalVariableStepPresenter.Display.class,
        DeriveTemporalVariableStepView.class);
    bindPresenterWidget(DeriveOpenTextualVariableStepPresenter.class,
        DeriveOpenTextualVariableStepPresenter.Display.class, DeriveOpenTextualVariableStepView.class);
    bindPresenterWidget(DeriveCustomVariablePresenter.class, DeriveCustomVariablePresenter.Display.class,
        DeriveCustomVariableStepView.class);
    bindPresenterWidget(DeriveFromVariablePresenter.class, DeriveFromVariablePresenter.Display.class,
        DeriveFromVariableView.class);
    bindPresenterWidget(DeriveConclusionPresenter.class, DeriveConclusionPresenter.Display.class,
        DeriveConclusionView.class);
    bind(CodingViewDialogPresenter.Display.class).to(CodingViewDialogView.class);
  }

}
