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

import org.obiba.opal.web.gwt.app.client.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NavigatorTreePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.view.DatasourceView;
import org.obiba.opal.web.gwt.app.client.view.NavigatorTreeView;
import org.obiba.opal.web.gwt.app.client.view.NavigatorView;
import org.obiba.opal.web.gwt.app.client.view.TableView;
import org.obiba.opal.web.gwt.app.client.view.VariableView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourceStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view.CreateDatasourceStepView;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.view.CreateViewStepView;

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

    // Don't bind as singleton because the ApplicationPresenter expects a new instance on every display
    bind(NavigatorTreePresenter.Display.class).to(NavigatorTreeView.class);
    bind(DatasourcePresenter.Display.class).to(DatasourceView.class);
    bind(TablePresenter.Display.class).to(TableView.class);
    bind(VariablePresenter.Display.class).to(VariableView.class);
    bind(CreateViewStepPresenter.Display.class).to(CreateViewStepView.class);
    bind(CreateDatasourceStepPresenter.Display.class).to(CreateDatasourceStepView.class);
  }

}
