/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;

/**
 * Base class for presenting administration widgets.
 */
public abstract class ItemAdministrationPresenter<D extends View, P extends Proxy<? extends Presenter<?, ?>>>
    extends Presenter<D, P> implements RequestAdministrationPermissionEvent.Handler {

  public ItemAdministrationPresenter(EventBus eventBus, D display, P p) {
    super(eventBus, display, p);
  }

  public abstract String getName();

  public abstract void authorize(HasAuthorization authorizer);

}
