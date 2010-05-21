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

import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.LoginPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.VariablePresenter;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules(OpalGinModule.class)
public interface OpalGinjector extends Ginjector {

  EventBus getEventBus();

  ApplicationPresenter getApplicationPresenter();

  NavigatorPresenter getNavigatorPresenter();

  VariablePresenter getVariablePresenter();

  LoginPresenter getLoginPresenter();

  UnhandledResponseNotificationPresenter getUnhandledResponseNotificationPresenter();

}
