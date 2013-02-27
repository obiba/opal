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
import org.obiba.opal.web.gwt.app.client.administration.database.view.DatabaseAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.database.view.DatabaseView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldMethodPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageCreatePresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackagePresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldConfigView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldMethodView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldPackageAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldPackageCreateView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldPackageView;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexConfigurationView;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexView;
import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.presenter.RAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.view.RAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.view.AdministrationView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
@SuppressWarnings("OverlyCoupledClass")
public class AdministrationModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(AdministrationPresenter.class, AdministrationPresenter.Display.class, AdministrationView.class,
        AdministrationPresenter.Proxy.class);
    bindPresenter(DataShieldConfigPresenter.class, DataShieldConfigPresenter.Display.class, DataShieldConfigView.class,
        DataShieldConfigPresenter.Proxy.class);
    bindPresenter(RAdministrationPresenter.class, RAdministrationPresenter.Display.class, RAdministrationView.class,
        RAdministrationPresenter.Proxy.class);

    bindPresenter(DatabaseAdministrationPresenter.class, DatabaseAdministrationPresenter.Display.class,
        DatabaseAdministrationView.class, DatabaseAdministrationPresenter.Proxy.class);
    bindPresenterWidget(DatabasePresenter.class, DatabasePresenter.Display.class, DatabaseView.class);

    bindPresenter(IndexAdministrationPresenter.class, IndexAdministrationPresenter.Display.class,
        IndexAdministrationView.class, IndexAdministrationPresenter.Proxy.class);
    bindPresenterWidget(IndexPresenter.class, IndexPresenter.Display.class, IndexView.class);
    bindPresenterWidget(IndexConfigurationPresenter.class, IndexConfigurationPresenter.Display.class,
        IndexConfigurationView.class);

    bindPresenterWidget(DataShieldPackageAdministrationPresenter.class,
        DataShieldPackageAdministrationPresenter.Display.class, DataShieldPackageAdministrationView.class);
    bindPresenterWidget(DataShieldAdministrationPresenter.class, DataShieldAdministrationPresenter.Display.class,
        DataShieldAdministrationView.class);
    bindPresenterWidget(DataShieldPackageCreatePresenter.class, DataShieldPackageCreatePresenter.Display.class,
        DataShieldPackageCreateView.class);
    bindPresenterWidget(DataShieldPackagePresenter.class, DataShieldPackagePresenter.Display.class,
        DataShieldPackageView.class);
    bindPresenterWidget(DataShieldMethodPresenter.class, DataShieldMethodPresenter.Display.class,
        DataShieldMethodView.class);

  }

}
