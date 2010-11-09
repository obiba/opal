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

import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.AttributeDialogPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.CategoryDialogPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.DataTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.EntitiesTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.LocalizablesPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.SaveErrorsStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.SelectScriptVariablesTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.VariablesListTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.AttributeDialogView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.CategoryDialogView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.ConfigureViewStepView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.DataTabView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.EntitiesTabView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.LocalizablesView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.SaveErrorsStepView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.SelectScriptVariablesTabView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.view.VariablesListTabView;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 * Bind concrete implementations to interfaces within the Configure View wizard.
 */
public class ConfigureViewWizardModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ConfigureViewStepPresenter.Display.class).to(ConfigureViewStepView.class).in(Singleton.class);
    bind(SaveErrorsStepPresenter.Display.class).to(SaveErrorsStepView.class).in(Singleton.class);
    bind(AttributeDialogPresenter.Display.class).to(AttributeDialogView.class);
    bind(CategoryDialogPresenter.Display.class).to(CategoryDialogView.class);
    bind(DataTabPresenter.Display.class).to(DataTabView.class);
    bind(EntitiesTabPresenter.Display.class).to(EntitiesTabView.class);
    bind(SelectScriptVariablesTabPresenter.Display.class).to(SelectScriptVariablesTabView.class);
    bind(VariablesListTabPresenter.Display.class).to(VariablesListTabView.class);
    bind(LocalizablesPresenter.Display.class).to(LocalizablesView.class);
  }
}
