/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.datasource.view;

import org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter.CsvDatasourceFormPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.AbstractCsvOptionsView;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class CsvDatasourceFormView extends AbstractCsvOptionsView implements CsvDatasourceFormPresenter.Display {
  @UiTemplate("CsvDatasourceFormView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CsvDatasourceFormView> {}

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  CsvOptionsView csvOptions;

  public CsvDatasourceFormView() {
    this.widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected CsvOptionsView getCsvOptions() {
    return csvOptions;
  }

  public void clearForm() {
    getCsvOptions().clear();
  }

}
