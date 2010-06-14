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

import net.customware.gwt.presenter.client.DefaultEventBus;
import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.fs.view.FileExplorerView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileSystemTreeView;
import org.obiba.opal.web.gwt.app.client.fs.view.FolderDetailsView;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.JobDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NavigatorTreePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.view.ApplicationView;
import org.obiba.opal.web.gwt.app.client.view.DataExportView;
import org.obiba.opal.web.gwt.app.client.view.DataImportView;
import org.obiba.opal.web.gwt.app.client.view.DatasourceView;
import org.obiba.opal.web.gwt.app.client.view.ErrorDialogView;
import org.obiba.opal.web.gwt.app.client.view.JobDetailsView;
import org.obiba.opal.web.gwt.app.client.view.JobListView;
import org.obiba.opal.web.gwt.app.client.view.LoginView;
import org.obiba.opal.web.gwt.app.client.view.NavigatorTreeView;
import org.obiba.opal.web.gwt.app.client.view.NavigatorView;
import org.obiba.opal.web.gwt.app.client.view.TableView;
import org.obiba.opal.web.gwt.app.client.view.UnhandledResponseNotificationView;
import org.obiba.opal.web.gwt.app.client.view.VariableView;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 *
 */
public class OpalGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    // Bind concrete implementations to interfaces
    bind(EventBus.class).to(DefaultEventBus.class).in(Singleton.class);
    bind(RequestCredentials.class).in(Singleton.class);

    bind(ApplicationPresenter.Display.class).to(ApplicationView.class).in(Singleton.class);
    bind(NavigatorPresenter.Display.class).to(NavigatorView.class).in(Singleton.class);
    bind(JobListPresenter.Display.class).to(JobListView.class).in(Singleton.class);
    bind(JobDetailsPresenter.Display.class).to(JobDetailsView.class).in(Singleton.class);
    bind(VariablePresenter.Display.class).to(VariableView.class).in(Singleton.class);
    bind(LoginPresenter.Display.class).to(LoginView.class).in(Singleton.class);
    bind(UnhandledResponseNotificationPresenter.Display.class).to(UnhandledResponseNotificationView.class).in(Singleton.class);
    bind(FolderDetailsPresenter.Display.class).to(FolderDetailsView.class);
    bind(FileSystemTreePresenter.Display.class).to(FileSystemTreeView.class);
    bind(FileExplorerPresenter.Display.class).to(FileExplorerView.class).in(Singleton.class);

    // Don't bind as singleton because the ApplicationPresenter expects a new instance on every display
    bind(NavigatorTreePresenter.Display.class).to(NavigatorTreeView.class);
    bind(DatasourcePresenter.Display.class).to(DatasourceView.class);
    bind(TablePresenter.Display.class).to(TableView.class);
    bind(DataImportPresenter.Display.class).to(DataImportView.class);// .in(Singleton.class);
    bind(DataExportPresenter.Display.class).to(DataExportView.class);
    bind(ErrorDialogPresenter.Display.class).to(ErrorDialogView.class);

    // Concrete classes (such as NavigatorPresenter) don't need to be "bound". Simply define a getter in the
    // OpalGinjector interface and they'll "just work".
  }

}
