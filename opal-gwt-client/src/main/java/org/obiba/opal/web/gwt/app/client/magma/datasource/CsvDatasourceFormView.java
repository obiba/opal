/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.datasource;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePanel;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class CsvDatasourceFormView extends AbstractCsvOptionsView implements CsvDatasourceFormPresenter.Display {

  @Override
  public HasType<ControlGroupType> getGroupType(String id) {
    return null;
  }

  @UiTemplate("CsvDatasourceFormView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CsvDatasourceFormView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  CsvOptionsView csvOptions;

  @UiField
  ControlGroup selectCsvFileGroup;

  @UiField
  OpalSimplePanel selectCsvFilePanel;

  public CsvDatasourceFormView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected CsvOptionsView getCsvOptions() {
    return csvOptions;
  }

  @Override
  public void clearForm() {
    getCsvOptions().clear();
  }

  @Override
  public void setCsvFileSelectorVisible(boolean value) {
    csvOptions.setCsvFileSelectorVisible(value);
  }

  public void setCsvFileSelectorWidgetDisplay(FileSelectionPresenter.Display display) {
    selectCsvFilePanel.setWidget(display.asWidget());
    display.setFieldWidth("20em");
  }

}
