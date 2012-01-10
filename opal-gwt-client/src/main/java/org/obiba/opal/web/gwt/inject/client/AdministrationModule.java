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

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldMethodPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldConfigView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldMethodView;
import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.presenter.RAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.view.RAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.view.AdministrationView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
public class AdministrationModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(AdministrationPresenter.class, AdministrationPresenter.Display.class, AdministrationView.class, AdministrationPresenter.Proxy.class);
    bindPresenter(DataShieldConfigPresenter.class, DataShieldConfigPresenter.Display.class, DataShieldConfigView.class, DataShieldConfigPresenter.Proxy.class);
    bindPresenter(RAdministrationPresenter.class, RAdministrationPresenter.Display.class, RAdministrationView.class, RAdministrationPresenter.Proxy.class);
    bindPresenterWidget(DataShieldAdministrationPresenter.class, DataShieldAdministrationPresenter.Display.class, DataShieldAdministrationView.class);

    bind(DataShieldMethodPresenter.Display.class).to(DataShieldMethodView.class);
  }

}
