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
import org.obiba.opal.web.gwt.app.client.fs.presenter.CreateFolderDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileDownloadPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorTreePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ConfirmationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.ExcelDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.FsDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.HibernateDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.JdbcDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.view.EvaluateScriptView;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.CsvFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DestinationSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.IdentityArchiveStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.XmlFormatStepPresenter;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules( { OpalGinModule.class, DashboardModule.class, NavigatorModule.class, FileSystemModule.class, JobModule.class, ImportModule.class, ImportVariablesWizardModule.class, CreateViewWizardModule.class, ConfigureViewWizardModule.class, ReportsModule.class, UnitsModule.class })
public interface OpalGinjector extends Ginjector {

  EventBus getEventBus();

  RequestCredentials getRequestCredentials();

  ApplicationPresenter getApplicationPresenter();

  DashboardPresenter getDashboardPresenter();

  NavigatorPresenter getNavigatorPresenter();

  VariablePresenter getVariablePresenter();

  LoginPresenter getLoginPresenter();

  UnhandledResponseNotificationPresenter getUnhandledResponseNotificationPresenter();

  NotificationPresenter getErrorDialogPresenter();

  NavigatorTreePresenter getNavigatorTreePresenter();

  DatasourcePresenter getDatasourcePresenter();

  TablePresenter getTablePresenter();

  FileSystemTreePresenter getFileSystemTreePresenter();

  FolderDetailsPresenter getFolderDetailsPresenter();

  FileUploadDialogPresenter getFileUploadDialogPresenter();

  FileDownloadPresenter getFileDownloadPresenter();

  FileSelectorPresenter getFileSelectorPresenter();

  TableSelectorPresenter getTableSelectorPresenter();

  TableListPresenter getTableListPresenter();

  FileSelectionPresenter getFileSectionPresenter();

  ConfirmationPresenter getConfirmationPresenter();

  CreateFolderDialogPresenter getCreateFolderDialogPresenter();

  DataImportPresenter getDataImportPresenter();

  CsvFormatStepPresenter getCsvFormatStepPresenter();

  XmlFormatStepPresenter getXmlFormatStepPresenter();

  DestinationSelectionStepPresenter getDestinationSelectionStepPresenter();

  ImportData getImportData();

  IdentityArchiveStepPresenter getIdentityArchiveStepPresenter();

  ConclusionStepPresenter getConclusionStepPresenter();

  EvaluateScriptView getEvaluateScriptView();

  HibernateDatasourceFormPresenter getHibernateDatasourceFormPresenter();

  ExcelDatasourceFormPresenter getExcelDatasourceFormPresenter();

  FsDatasourceFormPresenter getFsDatasourceFormPresenter();

  JdbcDatasourceFormPresenter getJdbcDatasourceFormPresenter();

  CreateViewStepPresenter getCreateViewStepPresenter();

  ConfigureViewStepPresenter getConfigureViewStepPresenter();

  CreateDatasourcePresenter getCreateDatasourcePresenter();

  DataExportPresenter getDataExportPresenter();
}
