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

import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.view.ApplicationView;
import org.obiba.opal.web.gwt.app.client.view.DataExportView;
import org.obiba.opal.web.gwt.app.client.view.DataImportView;
import org.obiba.opal.web.gwt.app.client.view.ErrorDialogView;
import org.obiba.opal.web.gwt.app.client.view.LoginView;
import org.obiba.opal.web.gwt.app.client.view.UnhandledResponseNotificationView;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.FileSelectionView;
import org.obiba.opal.web.gwt.app.client.widgets.view.FileSelectorView;
import org.obiba.opal.web.gwt.app.client.widgets.view.TableListView;
import org.obiba.opal.web.gwt.app.client.widgets.view.TableSelectorView;
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
    bind(DataImportPresenter.Display.class).to(DataImportView.class);// .in(Singleton.class);
    bind(DataExportPresenter.Display.class).to(DataExportView.class);
    bind(ErrorDialogPresenter.Display.class).to(ErrorDialogView.class);

    bind(FileSelectorPresenter.Display.class).to(FileSelectorView.class).in(Singleton.class);
    bind(TableSelectorPresenter.Display.class).to(TableSelectorView.class).in(Singleton.class);
    bind(TableListPresenter.Display.class).to(TableListView.class);
    bind(FileSelectionPresenter.Display.class).to(FileSelectionView.class);

    // Concrete classes (such as NavigatorPresenter) don't need to be "bound". Simply define a getter in the
    // OpalGinjector interface and they'll "just work".
  }

}
