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

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileDownloadPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NavigatorTreePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules( { OpalGinModule.class, DashboardModule.class, NavigatorModule.class, FileSystemModule.class, JobModule.class })
public interface OpalGinjector extends Ginjector {

  EventBus getEventBus();

  RequestCredentials getRequestCredentials();

  ApplicationPresenter getApplicationPresenter();

  DashboardPresenter getDashboardPresenter();

  NavigatorPresenter getNavigatorPresenter();

  VariablePresenter getVariablePresenter();

  LoginPresenter getLoginPresenter();

  UnhandledResponseNotificationPresenter getUnhandledResponseNotificationPresenter();

  ErrorDialogPresenter getErrorDialogPresenter();

  NavigatorTreePresenter getNavigatorTreePresenter();

  DatasourcePresenter getDatasourcePresenter();

  TablePresenter getTablePresenter();

  FileSystemTreePresenter getFileSystemTreePresenter();

  FolderDetailsPresenter getFolderDetailsPresenter();

  FileUploadDialogPresenter getFileUploadDialogPresenter();

  FileDownloadPresenter getFileDownloadPresenter();

  FileSelectorPresenter getFileSelectorPresenter();

  TableSelectorPresenter getTableSelectorPresenter();
}
