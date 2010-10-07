/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.view;

import org.obiba.opal.web.gwt.app.client.widgets.view.AbstractCsvOptionsView;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.CsvFormatStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public class CsvFormatStepView extends AbstractCsvOptionsView implements CsvFormatStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  Button nextButton;

  @UiField
  CsvOptionsView csvOptions;

  //
  // Constructors
  //

  public CsvFormatStepView() {
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
  // Methods
  //

  @Override
  public HandlerRegistration addNextClickHandler(ClickHandler handler) {
    return nextButton.addClickHandler(handler);
  }

  @Override
  public void setNextEnabled(boolean enabled) {
    nextButton.setEnabled(enabled);
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("CsvFormatStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CsvFormatStepView> {
  }
}
