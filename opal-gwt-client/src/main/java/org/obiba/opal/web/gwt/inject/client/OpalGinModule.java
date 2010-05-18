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

import org.obiba.opal.web.client.gwt.client.presenter.NavigatorPresenter;
import org.obiba.opal.web.client.gwt.client.presenter.VariablePresenter;
import org.obiba.opal.web.client.gwt.client.view.NavigatorView;
import org.obiba.opal.web.client.gwt.client.view.VariableView;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 *
 */
public class OpalGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    // Bind concrete implementations to interfaces
    bind(EventBus.class).to(DefaultEventBus.class);
    bind(NavigatorPresenter.Display.class).to(NavigatorView.class);
    bind(VariablePresenter.Display.class).to(VariableView.class);

    // Concrete classes (such as NavigatorPresenter) don't need to be "bound". Simply define a getter in the
    // OpalGinjector interface and they'll "just work".
  }

}
