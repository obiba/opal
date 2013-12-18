/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.unit.presenter.AddKeyPairModalPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitListPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.GenerateIdentifiersModalPresenter;
import org.obiba.opal.web.gwt.app.client.unit.view.AddCryptoKeyModalView;
import org.obiba.opal.web.gwt.app.client.unit.view.FunctionalUnitDetailsView;
import org.obiba.opal.web.gwt.app.client.unit.view.FunctionalUnitListView;
import org.obiba.opal.web.gwt.app.client.unit.view.FunctionalUnitUpdateModalView;
import org.obiba.opal.web.gwt.app.client.unit.view.FunctionalUnitView;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.view.GenerateIdentifiersModalView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class UnitsModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(FunctionalUnitDetailsPresenter.class, FunctionalUnitDetailsPresenter.Display.class,
        FunctionalUnitDetailsView.class, FunctionalUnitDetailsPresenter.Proxy.class);

    bindPresenter(FunctionalUnitPresenter.class, FunctionalUnitPresenter.Display.class, FunctionalUnitView.class,
        FunctionalUnitPresenter.Proxy.class);
    bindPresenterWidget(FunctionalUnitListPresenter.class, FunctionalUnitListPresenter.Display.class,
        FunctionalUnitListView.class);
    bindPresenterWidget(FunctionalUnitUpdateModalPresenter.class, FunctionalUnitUpdateModalPresenter.Display.class,
        FunctionalUnitUpdateModalView.class);
    bindPresenterWidget(AddKeyPairModalPresenter.class, AddKeyPairModalPresenter.Display.class,
        AddCryptoKeyModalView.class);
  }

}
