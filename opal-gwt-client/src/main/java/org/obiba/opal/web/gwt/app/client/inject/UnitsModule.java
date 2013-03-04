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

import org.obiba.opal.web.gwt.app.client.unit.presenter.AddKeyPairDialogPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitListPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateDialogPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.GenerateIdentifiersDialogPresenter;
import org.obiba.opal.web.gwt.app.client.unit.view.AddCryptoKeyDialogView;
import org.obiba.opal.web.gwt.app.client.unit.view.FunctionalUnitDetailsView;
import org.obiba.opal.web.gwt.app.client.unit.view.FunctionalUnitListView;
import org.obiba.opal.web.gwt.app.client.unit.view.FunctionalUnitUpdateDialogView;
import org.obiba.opal.web.gwt.app.client.unit.view.FunctionalUnitView;
import org.obiba.opal.web.gwt.app.client.unit.view.GenerateIdentifiersDialogView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class UnitsModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(FunctionalUnitPresenter.class, FunctionalUnitPresenter.Display.class, FunctionalUnitView.class,
        FunctionalUnitPresenter.Proxy.class);
    bindPresenterWidget(FunctionalUnitListPresenter.class, FunctionalUnitListPresenter.Display.class,
        FunctionalUnitListView.class);
    bindPresenterWidget(FunctionalUnitDetailsPresenter.class, FunctionalUnitDetailsPresenter.Display.class,
        FunctionalUnitDetailsView.class);
    bindPresenterWidget(FunctionalUnitUpdateDialogPresenter.class, FunctionalUnitUpdateDialogPresenter.Display.class,
        FunctionalUnitUpdateDialogView.class);
    bindPresenterWidget(AddKeyPairDialogPresenter.class, AddKeyPairDialogPresenter.Display.class,
        AddCryptoKeyDialogView.class);
    bindPresenterWidget(GenerateIdentifiersDialogPresenter.class, GenerateIdentifiersDialogPresenter.Display.class,
        GenerateIdentifiersDialogView.class);
  }

}
