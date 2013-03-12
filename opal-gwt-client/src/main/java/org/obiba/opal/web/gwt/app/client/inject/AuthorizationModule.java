/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.view.AuthorizationView;
import org.obiba.opal.web.gwt.app.client.authz.view.SubjectAuthorizationView;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.view.LoginView;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationCache;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
public class AuthorizationModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bind(RequestCredentials.class).in(Singleton.class);
    bind(ResourceAuthorizationCache.class).in(Singleton.class);

    bindPresenter(LoginPresenter.class, LoginPresenter.Display.class, LoginView.class, LoginPresenter.Proxy.class);
    bindPresenterWidget(AuthorizationPresenter.class, AuthorizationPresenter.Display.class, AuthorizationView.class);
    bindPresenterWidget(SubjectAuthorizationPresenter.class, SubjectAuthorizationPresenter.Display.class,
        SubjectAuthorizationView.class);
  }

}
