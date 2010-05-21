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
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.view.ApplicationView;
import org.obiba.opal.web.gwt.app.client.view.LoginView;
import org.obiba.opal.web.gwt.app.client.view.NavigatorView;
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
    bind(EventBus.class).to(DefaultEventBus.class);
    bind(RequestCredentials.class).in(Singleton.class);
    bind(ApplicationPresenter.Display.class).to(ApplicationView.class);
    bind(NavigatorPresenter.Display.class).to(NavigatorView.class);
    bind(VariablePresenter.Display.class).to(VariableView.class);
    bind(LoginPresenter.Display.class).to(LoginView.class);
    bind(UnhandledResponseNotificationPresenter.Display.class).to(UnhandledResponseNotificationView.class);

    // Concrete classes (such as NavigatorPresenter) don't need to be "bound". Simply define a getter in the
    // OpalGinjector interface and they'll "just work".
  }

}
