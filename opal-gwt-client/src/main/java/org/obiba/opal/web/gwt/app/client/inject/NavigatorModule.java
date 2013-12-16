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

import org.obiba.opal.web.gwt.app.client.magma.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.magma.createdatasource.view.CreateDatasourceView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.CsvDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.DatasourceFormPresenterSubscriber;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.ExcelDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.FsDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.HibernateDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.JdbcDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.NullDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.CsvDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.ExcelDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.FsDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.HibernateDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.JdbcDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.NullDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveBooleanVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveCategoricalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveConclusionPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveCustomVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveFromVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveNumericalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveOpenTextualVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveTemporalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveBooleanVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveCategoricalVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveConclusionView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveCustomVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveFromVariableView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveNumericalVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveOpenTextualVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveTemporalVariableStepView;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.DeriveVariableView;
import org.obiba.opal.web.gwt.app.client.magma.presenter.CodingViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.EntityModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValuesTablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariableVcsCommitHistoryPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VcsCommitHistoryModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.ViewPropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.view.TablePropertiesModalView;
import org.obiba.opal.web.gwt.app.client.magma.table.view.ViewPropertiesModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.CategoriesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.CategoriesEditorModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.VariableAttributeModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.VariablePropertiesModalView;
import org.obiba.opal.web.gwt.app.client.magma.view.CodingViewModalView;
import org.obiba.opal.web.gwt.app.client.magma.view.DatasourceView;
import org.obiba.opal.web.gwt.app.client.magma.view.EntityModalView;
import org.obiba.opal.web.gwt.app.client.magma.view.MagmaView;
import org.obiba.opal.web.gwt.app.client.magma.view.TableView;
import org.obiba.opal.web.gwt.app.client.magma.view.ValuesTableView;
import org.obiba.opal.web.gwt.app.client.magma.view.VariableVcsCommitHistoryView;
import org.obiba.opal.web.gwt.app.client.magma.view.VariableView;
import org.obiba.opal.web.gwt.app.client.magma.view.VcsCommitHistoryModalView;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectDataExchangePresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectsPresenter;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectAdministrationView;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectDataExchangeView;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectPropertiesModalView;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectView;
import org.obiba.opal.web.gwt.app.client.project.view.ProjectsView;

import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
@SuppressWarnings("OverlyCoupledClass")
public class NavigatorModule extends AbstractOpalModule {

  public NavigatorModule() {
  }

  @Override
  protected void configure() {
    configureProject();
    configureMagma();
    configureDereiveVariableWizard();
    configureDatasource();
    configureDeriveVariablePresenters();
  }

  private void configureMagma() {
    bindPresenterWidget(MagmaPresenter.class, MagmaPresenter.Display.class, MagmaView.class);
    bindPresenterWidget(DatasourcePresenter.class, DatasourcePresenter.Display.class, DatasourceView.class);
    bindPresenterWidget(TablePresenter.class, TablePresenter.Display.class, TableView.class);
    bindPresenterWidget(VariablePresenter.class, VariablePresenter.Display.class, VariableView.class);
    bindPresenterWidget(ValuesTablePresenter.class, ValuesTablePresenter.Display.class, ValuesTableView.class);
    bindPresenterWidget(EntityModalPresenter.class, EntityModalPresenter.Display.class, EntityModalView.class);
    bindPresenterWidget(VariableVcsCommitHistoryPresenter.class, VariableVcsCommitHistoryPresenter.Display.class,
        VariableVcsCommitHistoryView.class);
    bindPresenterWidget(VcsCommitHistoryModalPresenter.class, VcsCommitHistoryModalPresenter.Display.class,
        VcsCommitHistoryModalView.class);

    bindPresenterWidget(TablePropertiesModalPresenter.class, TablePropertiesModalPresenter.Display.class,
        TablePropertiesModalView.class);
    bindPresenterWidget(ViewPropertiesModalPresenter.class, ViewPropertiesModalPresenter.Display.class,
        ViewPropertiesModalView.class);
    bindPresenterWidget(CategoriesEditorModalPresenter.class, CategoriesEditorModalPresenter.Display.class,
        CategoriesEditorModalView.class);
    bindPresenterWidget(VariablePropertiesModalPresenter.class, VariablePropertiesModalPresenter.Display.class,
        VariablePropertiesModalView.class);
    bindPresenterWidget(VariableAttributeModalPresenter.class, VariableAttributeModalPresenter.Display.class,
        VariableAttributeModalView.class);

    bindWizardPresenterWidget(CreateDatasourcePresenter.class, CreateDatasourcePresenter.Display.class,
        CreateDatasourceView.class, CreateDatasourcePresenter.Wizard.class);
  }

  private void configureDatasource() {
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
  }

  private void configureDereiveVariableWizard() {
    bindWizardPresenterWidget(DeriveVariablePresenter.class, DeriveVariablePresenter.Display.class,
        DeriveVariableView.class, DeriveVariablePresenter.CategorizeWizard.class);
    // tricky case: one wizard presenter for 3 different types
    bind(DeriveVariablePresenter.CustomWizard.class).asEagerSingleton();
    bind(DeriveVariablePresenter.FromWizard.class).asEagerSingleton();
  }

  private void configureProject() {
    bindPresenter(ProjectsPresenter.class, ProjectsPresenter.Display.class, ProjectsView.class,
        ProjectsPresenter.Proxy.class);
    bindPresenter(ProjectPresenter.class, ProjectPresenter.Display.class, ProjectView.class,
        ProjectPresenter.Proxy.class);
    bindPresenterWidget(ProjectAdministrationPresenter.class, ProjectAdministrationPresenter.Display.class,
        ProjectAdministrationView.class);
    bindPresenterWidget(ProjectPropertiesModalPresenter.class, ProjectPropertiesModalPresenter.Display.class,
        ProjectPropertiesModalView.class);
    bindPresenterWidget(ProjectDataExchangePresenter.class, ProjectDataExchangePresenter.Display.class,
        ProjectDataExchangeView.class);
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
    bind(CodingViewModalPresenter.Display.class).to(CodingViewModalView.class);
  }

}
