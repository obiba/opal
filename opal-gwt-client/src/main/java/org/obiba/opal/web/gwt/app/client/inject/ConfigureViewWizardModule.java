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

import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.LabelListView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.AddDerivedVariableDialogPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.AttributeDialogPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.AttributesPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.CategoriesPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.CategoryDialogPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.DataTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.SelectScriptVariablesTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.VariablesListTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.AddDerivedVariableDialogView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.AttributeDialogView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.AttributesView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.CategoriesView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.CategoryDialogView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.ConfigureViewStepView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.DataTabView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.SelectScriptVariablesTabView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.VariablesListTabView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Bind concrete implementations to interfaces within the Configure View wizard.
 */
public class ConfigureViewWizardModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindSingletonPresenterWidget(ConfigureViewStepPresenter.class, ConfigureViewStepPresenter.Display.class,
        ConfigureViewStepView.class);

    bindPresenterWidget(AddDerivedVariableDialogPresenter.class, AddDerivedVariableDialogPresenter.Display.class,
        AddDerivedVariableDialogView.class);
    bindPresenterWidget(VariablesListTabPresenter.class, VariablesListTabPresenter.Display.class,
        VariablesListTabView.class);
    bindPresenterWidget(CategoriesPresenter.class, CategoriesPresenter.Display.class, CategoriesView.class);
    bindPresenterWidget(AttributesPresenter.class, AttributesPresenter.Display.class, AttributesView.class);

    bindPresenterWidget(SelectScriptVariablesTabPresenter.class, SelectScriptVariablesTabPresenter.Display.class,
        SelectScriptVariablesTabView.class);

    bind(AttributeDialogPresenter.Display.class).to(AttributeDialogView.class);
    bind(CategoryDialogPresenter.Display.class).to(CategoryDialogView.class);
    bind(DataTabPresenter.Display.class).to(DataTabView.class);
    bind(LabelListPresenter.Display.class).to(LabelListView.class);
  }
}
