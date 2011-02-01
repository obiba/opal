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

import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.DataShieldAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.view.AdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.view.DataShieldAdministrationView;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 *
 */
public class AdministrationModule extends AbstractGinModule {

  @Override
  protected void configure() {
    // Bind concrete implementations to interfaces
    bind(AdministrationPresenter.Display.class).to(AdministrationView.class).in(Singleton.class);
    bind(DataShieldAdministrationPresenter.Display.class).to(DataShieldAdministrationView.class);
  }

}
