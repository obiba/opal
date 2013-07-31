/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabasePresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldMethodPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageCreatePresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackagePresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.presenter.RAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.CreateFolderModalPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadModalPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.CodingViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.EntityModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoggedInGatekeeper;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateListPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplatePresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitListPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.ExcelDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.FsDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.HibernateDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.JdbcDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.magma.datasource.presenter.NullDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ConfirmationPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ScriptEvaluationPopupPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValueMapPopupPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.app.client.magma.view.ScriptEditorView;
import org.obiba.opal.web.gwt.app.client.magma.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.magma.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.magma.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.ArchiveStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.CsvFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DestinationSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.NoFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.SpssFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.UnitSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.XmlFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.VariablesImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationCache;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

@SuppressWarnings({ "UnusedDeclaration", "OverlyCoupledClass" })
@GinModules({ OpalGinModule.class, AuthorizationModule.class, AdministrationModule.class, DashboardModule.class,
    NavigatorModule.class, FileSystemModule.class, JobModule.class, ImportModule.class, ExportModule.class,
    ImportVariablesWizardModule.class, CreateViewWizardModule.class, ConfigureViewWizardModule.class,
    ReportsModule.class, UnitsModule.class, ImportIdentifiersWizardModule.class })
public interface OpalGinjector extends Ginjector {

  com.google.gwt.event.shared.EventBus getOldEventBus();

  EventBus getEventBus();

  PlaceManager getPlaceManager();

  ResourceAuthorizationCache getResourceAuthorizationCache();

  Provider<AuthorizationPresenter> getAuthorizationPresenter();

  Provider<SubjectAuthorizationPresenter> getSubjectAuthorizationPresenter();

  RequestCredentials getRequestCredentials();

  RequestUrlBuilder getRequestUrlBuilder();

  @DefaultGatekeeper
  LoggedInGatekeeper getLoginGateKeeper();

  Provider<LoginPresenter> getLoginPresenter();

  UnhandledResponseNotificationPresenter getUnhandledResponseNotificationPresenter();

  Provider<NotificationPresenter> getErrorModalPresenter();

  Provider<ApplicationPresenter> getApplicationPresenter();

  Provider<AdministrationPresenter> getAdministrationPresenter();

  Provider<DashboardPresenter> getDashboardPresenter();

  Provider<FunctionalUnitPresenter> getFunctionalUnitPresenter();

  Provider<ReportTemplatePresenter> getReportTemplatePresenter();

  Provider<ReportTemplateListPresenter> getReportTemplateListPresenter();

  Provider<ReportTemplateDetailsPresenter> getReportTemplateDetailsPresenter();

  Provider<JobListPresenter> getJobListPresenter();

  Provider<FileExplorerPresenter> getFileExplorerPresenter();

  Provider<VariablePresenter> getVariablePresenter();

  Provider<DatasourcePresenter> getDatasourcePresenter();

  Provider<TablePresenter> getTablePresenter();

  Provider<EntityModalPresenter> getEntityModalPresenter();

  Provider<FunctionalUnitListPresenter> getFunctionalUnitListPresenter();

  Provider<FunctionalUnitDetailsPresenter> getFunctionalUnitDetailsPresenter();

  Provider<FunctionalUnitUpdateModalPresenter> getFunctionalUnitUpdateModalPresenter();

  Provider<FileSystemTreePresenter> getFileSystemTreePresenter();

  Provider<FolderDetailsPresenter> getFolderDetailsPresenter();

  FileUploadModalPresenter getFileUploadModalPresenter();

  Provider<FileSelectorPresenter> getFileSelectorPresenter();

  FileSelectionPresenter getFileSectionPresenter();

  ConfirmationPresenter getConfirmationPresenter();

  Provider<ScriptEvaluationPopupPresenter> getScriptEvaluationPopupPresenter();

  ValueMapPopupPresenter getValueMapPopupPresenter();

  ValueSequencePopupPresenter getValueSequencePopupPresenter();

  Provider<CreateFolderModalPresenter> getCreateFolderModalPresenter();

  DataImportPresenter getDataImportPresenter();

  VariablesImportPresenter getVariablesImportPresenter();

  CsvFormatStepPresenter getCsvFormatStepPresenter();

  XmlFormatStepPresenter getXmlFormatStepPresenter();

  SpssFormatStepPresenter getSpssFormatStepPresenter();

  NoFormatStepPresenter getNoFormatStepPresenter();

  DestinationSelectionStepPresenter getDestinationSelectionStepPresenter();

  ArchiveStepPresenter getArchiveStepPresenter();

  UnitSelectionStepPresenter getUnitSelectionStepPresenter();

  ScriptEditorView getEvaluateScriptView();

  HibernateDatasourceFormPresenter getHibernateDatasourceFormPresenter();

  ExcelDatasourceFormPresenter getExcelDatasourceFormPresenter();

  FsDatasourceFormPresenter getxresourFsDatasourceFormPresenter();

  JdbcDatasourceFormPresenter getJdbcDatasourceFormPresenter();

  NullDatasourceFormPresenter getNullDatasourceFormPresenter();

  CreateViewStepPresenter getCreateViewStepPresenter();

  ConfigureViewStepPresenter getConfigureViewStepPresenter();

  VariablesToViewPresenter getVariablesToViewPresenter();

  CreateDatasourcePresenter getCreateDatasourcePresenter();

  DataExportPresenter getDataExportPresenter();

  DataCopyPresenter getDataCopyPresenter();

  DeriveVariablePresenter getDeriveVariablePresenter();

  CodingViewModalPresenter getCodingViewModalPresenter();

  Translations getTranslations();

  Provider<DataShieldConfigPresenter> getDataShieldConfigPresenter();

  Provider<DataShieldPackageAdministrationPresenter> getDataShieldPackageAdministrationPresenter();

  Provider<RAdministrationPresenter> getRAdministrationPresenter();

  Provider<DataShieldPackageCreatePresenter> getDataShieldPackageCreatePresenter();

  Provider<DataShieldPackagePresenter> getDataShieldPackagePresenter();

  Provider<DataShieldMethodPresenter> getDataShieldMethodPresenter();

  Provider<DatabaseAdministrationPresenter> getDatabaseAdministrationPresenter();

  Provider<DatabasePresenter> getDatabasePresenter();

  Provider<IndexAdministrationPresenter> getIndexAdministrationPresenter();

  Provider<IndexPresenter> getIndexPresenter();

  Provider<PageContainerPresenter> getPageContainerPresenter();

  Provider<UserAdministrationPresenter> getUserAdministrationPresenter();

  Provider<UserPresenter> getUserPresenter();
}
