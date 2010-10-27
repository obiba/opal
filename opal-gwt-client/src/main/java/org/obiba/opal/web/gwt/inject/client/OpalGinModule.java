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

import org.obiba.opal.web.gwt.app.client.fs.presenter.CreateFolderDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.view.CreateFolderDialogView;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateListPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplatePresenter;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateDetailsView;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateListView;
import org.obiba.opal.web.gwt.app.client.report.view.ReportTemplateView;
import org.obiba.opal.web.gwt.app.client.view.ApplicationView;
import org.obiba.opal.web.gwt.app.client.view.DataExportView;
import org.obiba.opal.web.gwt.app.client.view.LoginView;
import org.obiba.opal.web.gwt.app.client.view.NotificationView;
import org.obiba.opal.web.gwt.app.client.view.UnhandledResponseNotificationView;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ConfirmationPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.DatasourceSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.ConfirmationView;
import org.obiba.opal.web.gwt.app.client.widgets.view.DatasourceSelectorView;
import org.obiba.opal.web.gwt.app.client.widgets.view.FileSelectionView;
import org.obiba.opal.web.gwt.app.client.widgets.view.FileSelectorView;
import org.obiba.opal.web.gwt.app.client.widgets.view.TableListView;
import org.obiba.opal.web.gwt.app.client.widgets.view.TableSelectorView;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.view.EvaluateScriptView;
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
    bind(LoginPresenter.Display.class).to(LoginView.class).in(Singleton.class);
    bind(UnhandledResponseNotificationPresenter.Display.class).to(UnhandledResponseNotificationView.class).in(Singleton.class);

    // Don't bind as singleton because the ApplicationPresenter expects a new instance on every display
    bind(DataExportPresenter.Display.class).to(DataExportView.class);
    bind(NotificationPresenter.Display.class).to(NotificationView.class);

    bind(FileSelectorPresenter.Display.class).to(FileSelectorView.class).in(Singleton.class);
    bind(TableSelectorPresenter.Display.class).to(TableSelectorView.class).in(Singleton.class);
    bind(TableListPresenter.Display.class).to(TableListView.class);
    bind(FileSelectionPresenter.Display.class).to(FileSelectionView.class);
    bind(ConfirmationPresenter.Display.class).to(ConfirmationView.class).in(Singleton.class);
    bind(DatasourceSelectorPresenter.Display.class).to(DatasourceSelectorView.class);
    bind(CreateFolderDialogPresenter.Display.class).to(CreateFolderDialogView.class).in(Singleton.class);
    bind(EvaluateScriptPresenter.Display.class).to(EvaluateScriptView.class).in(Singleton.class);
    
    // TODO Move this to a ReportTemplate module
    bind(ReportTemplateListPresenter.Display.class).to(ReportTemplateListView.class).in(Singleton.class);
    bind(ReportTemplateDetailsPresenter.Display.class).to(ReportTemplateDetailsView.class).in(Singleton.class);
    bind(ReportTemplatePresenter.Display.class).to(ReportTemplateView.class).in(Singleton.class);

    // Concrete classes (such as NavigatorPresenter) don't need to be "bound". Simply define a getter in the
    // OpalGinjector interface and they'll "just work".
  }

}
