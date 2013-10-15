/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import org.obiba.opal.web.gwt.app.client.magma.datasource.view.AbstractCsvOptionsView;
import org.obiba.opal.web.gwt.app.client.magma.datasource.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.CsvFormatStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CsvFormatStepView extends AbstractCsvOptionsView implements CsvFormatStepPresenter.Display {

  //
  // Instance Variables
  //

  @UiField
  CsvOptionsView csvOptions;

  //
  // Constructors
  //

  @Inject
  public CsvFormatStepView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // AbstractCsvOptionsView Methods
  //

  @Override
  protected CsvOptionsView getCsvOptions() {
    return csvOptions;
  }

  //
  // Inner Classes / Interfaces
  //

  interface Binder extends UiBinder<Widget, CsvFormatStepView> {}

}
