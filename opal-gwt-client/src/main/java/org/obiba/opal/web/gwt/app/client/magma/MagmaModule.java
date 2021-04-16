/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;
import org.obiba.opal.web.gwt.app.client.magma.datasource.CsvDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.DatasourceFormPresenterSubscriber;
import org.obiba.opal.web.gwt.app.client.magma.datasource.ExcelDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.FsDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.HibernateDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.JdbcDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.NullDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.CsvDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.ExcelDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.FsDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.HibernateDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.JdbcDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.NullDatasourceFormView;
import org.obiba.opal.web.gwt.app.client.magma.sql.SQLPresenter;
import org.obiba.opal.web.gwt.app.client.magma.sql.SQLView;
import org.obiba.opal.web.gwt.app.client.magma.table.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.ViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.ViewWhereModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.TablePropertiesModalView;
import org.obiba.opal.web.gwt.app.client.magma.table.ViewModalView;
import org.obiba.opal.web.gwt.app.client.magma.table.ViewWhereModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.CategoriesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.ContingencyTablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.VariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.VariablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.VariableTaxonomyModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.CategoriesEditorModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.ContingencyTableView;
import org.obiba.opal.web.gwt.app.client.magma.variable.VariableAttributeModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.VariablePropertiesModalView;
import org.obiba.opal.web.gwt.app.client.magma.variable.VariableTaxonomyModalView;

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
    bindPresenterWidget(SQLPresenter.class, SQLPresenter.Display.class, SQLView.class);
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
