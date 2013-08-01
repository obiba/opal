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

import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.AddDerivedVariableModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.AttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.CategoryModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.view.AddDerivedVariableModalView;
import org.obiba.opal.web.gwt.app.client.magma.configureview.view.AttributeModalView;
import org.obiba.opal.web.gwt.app.client.magma.configureview.view.CategoryModalView;
import org.obiba.opal.web.gwt.app.client.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.view.LabelListView;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.AttributesPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.CategoriesPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.DataTabPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.SelectScriptVariablesTabPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.VariablesListTabPresenter;
import org.obiba.opal.web.gwt.app.client.magma.configureview.view.AttributesView;
import org.obiba.opal.web.gwt.app.client.magma.configureview.view.CategoriesView;
import org.obiba.opal.web.gwt.app.client.magma.configureview.view.ConfigureViewStepView;
import org.obiba.opal.web.gwt.app.client.magma.configureview.view.DataTabView;
import org.obiba.opal.web.gwt.app.client.magma.configureview.view.SelectScriptVariablesTabView;
import org.obiba.opal.web.gwt.app.client.magma.configureview.view.VariablesListTabView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Bind concrete implementations to interfaces within the Configure View wizard.
 */
public class ConfigureViewWizardModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindSingletonPresenterWidget(ConfigureViewStepPresenter.class, ConfigureViewStepPresenter.Display.class,
        ConfigureViewStepView.class);

    bindPresenterWidget(AddDerivedVariableModalPresenter.class, AddDerivedVariableModalPresenter.Display.class,
        AddDerivedVariableModalView.class);
    bindPresenterWidget(VariablesListTabPresenter.class, VariablesListTabPresenter.Display.class,
        VariablesListTabView.class);
    bindPresenterWidget(CategoriesPresenter.class, CategoriesPresenter.Display.class, CategoriesView.class);
    bindPresenterWidget(AttributesPresenter.class, AttributesPresenter.Display.class, AttributesView.class);

    bindPresenterWidget(SelectScriptVariablesTabPresenter.class, SelectScriptVariablesTabPresenter.Display.class,
        SelectScriptVariablesTabView.class);

    bind(AttributeModalPresenter.Display.class).to(AttributeModalView.class);
    bind(CategoryModalPresenter.Display.class).to(CategoryModalView.class);
    bind(DataTabPresenter.Display.class).to(DataTabView.class);
    bind(LabelListPresenter.Display.class).to(LabelListView.class);
  }
}
