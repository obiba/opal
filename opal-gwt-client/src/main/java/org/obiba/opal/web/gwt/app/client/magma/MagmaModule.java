/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import org.obiba.opal.web.gwt.app.client.analysis.AnalysesPresenter;
import org.obiba.opal.web.gwt.app.client.analysis.AnalysesView;
import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;
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
import org.obiba.opal.web.gwt.app.client.magma.presenter.AddVariablesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValuesTablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariableVcsCommitHistoryPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VcsCommitHistoryModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.ViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.ViewWhereModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.view.TablePropertiesModalView;
import org.obiba.opal.web.gwt.app.client.magma.table.view.ViewModalView;
import org.obiba.opal.web.gwt.app.client.magma.table.view.ViewWhereModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.CategoriesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.ContingencyTablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableTaxonomyModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.CategoriesEditorModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.ContingencyTableView;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.VariableAttributeModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.VariablePropertiesModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.VariableTaxonomyModalView;
import org.obiba.opal.web.gwt.app.client.magma.view.AddVariablesModalView;
import org.obiba.opal.web.gwt.app.client.magma.view.DatasourceView;
import org.obiba.opal.web.gwt.app.client.magma.view.MagmaView;
import org.obiba.opal.web.gwt.app.client.magma.view.TableView;
import org.obiba.opal.web.gwt.app.client.magma.view.ValuesTableView;
import org.obiba.opal.web.gwt.app.client.magma.view.VariableVcsCommitHistoryView;
import org.obiba.opal.web.gwt.app.client.magma.view.VariableView;
import org.obiba.opal.web.gwt.app.client.magma.view.VcsCommitHistoryModalView;

import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
@SuppressWarnings("OverlyCoupledClass")
public class MagmaModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    configureMagma();
    configureDatasource();
  }

  private void configureMagma() {
    bindPresenterWidget(MagmaPresenter.class, MagmaPresenter.Display.class, MagmaView.class);
    bindPresenterWidget(DatasourcePresenter.class, DatasourcePresenter.Display.class, DatasourceView.class);
    bindPresenterWidget(TablePresenter.class, TablePresenter.Display.class, TableView.class);
    bindPresenterWidget(VariablePresenter.class, VariablePresenter.Display.class, VariableView.class);
    bindPresenterWidget(ContingencyTablePresenter.class, ContingencyTablePresenter.Display.class,
        ContingencyTableView.class);
    bindPresenterWidget(ValuesTablePresenter.class, ValuesTablePresenter.Display.class, ValuesTableView.class);
    bindPresenterWidget(VariableVcsCommitHistoryPresenter.class, VariableVcsCommitHistoryPresenter.Display.class,
        VariableVcsCommitHistoryView.class);
    bindPresenterWidget(VcsCommitHistoryModalPresenter.class, VcsCommitHistoryModalPresenter.Display.class,
        VcsCommitHistoryModalView.class);

    bindPresenterWidget(TablePropertiesModalPresenter.class, TablePropertiesModalPresenter.Display.class,
        TablePropertiesModalView.class);
    bindPresenterWidget(ViewModalPresenter.class, ViewModalPresenter.Display.class,
        ViewModalView.class);
    bindPresenterWidget(ViewWhereModalPresenter.class, ViewWhereModalPresenter.Display.class, ViewWhereModalView.class);
    bindPresenterWidget(CategoriesEditorModalPresenter.class, CategoriesEditorModalPresenter.Display.class,
        CategoriesEditorModalView.class);
    bindPresenterWidget(VariablePropertiesModalPresenter.class, VariablePropertiesModalPresenter.Display.class,
        VariablePropertiesModalView.class);
    bindPresenterWidget(VariableAttributeModalPresenter.class, VariableAttributeModalPresenter.Display.class,
        VariableAttributeModalView.class);
    bindPresenterWidget(VariableTaxonomyModalPresenter.class, VariableTaxonomyModalPresenter.Display.class,
        VariableTaxonomyModalView.class);
    bindPresenterWidget(AddVariablesModalPresenter.class, AddVariablesModalPresenter.Display.class,
        AddVariablesModalView.class);

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

  private <V extends View> void bindDatasourceFormPresenter(Class<? extends PresenterWidget<V>> presenter,
      Class<V> display, Class<? extends V> view, Class<? extends DatasourceFormPresenterSubscriber> subscriber) {
    bind(subscriber).asEagerSingleton();
    bindPresenterWidget(presenter, display, view);
  }

}
