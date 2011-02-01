/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.view;

import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.DataShieldAdministrationPresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class AdministrationView extends Composite implements AdministrationPresenter.Display {

  @UiTemplate("AdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, AdministrationView> {
  }

  //
  // Constants
  //

  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  SimplePanel datashieldAdministration;

  //
  // Constructors
  //

  public AdministrationView() {
    super();
    initWidget(uiBinder.createAndBindUi(this));

  }

  //
  // AdministrationPresenter.Display Methods
  //

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {

  }

  @Override
  public void stopProcessing() {

  }

  @Override
  public void setDataShieldAdministrationDisplay(Display display) {
    datashieldAdministration.setWidget(display.asWidget());
  }

  //
  // Methods
  //

}
