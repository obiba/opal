/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view;

import org.obiba.opal.web.gwt.app.client.widgets.view.AbstractCsvOptionsView;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CsvDatasourceFormPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class CsvDatasourceFormView extends AbstractCsvOptionsView implements CsvDatasourceFormPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  CsvOptionsView csvOptions;

  //
  // Constructors
  //

  public CsvDatasourceFormView() {
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
  // CsvDatasourceFormPresenter.Display Methods
  //

  public void clearForm() {
    getCsvOptions().clear();
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("CsvDatasourceFormView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CsvDatasourceFormView> {
  }
}
