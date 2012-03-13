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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.JdbcDatasourceFormPresenter;
import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class JdbcDatasourceFormView extends ViewImpl implements JdbcDatasourceFormPresenter.Display {

  @UiTemplate("JdbcDatasourceFormView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, JdbcDatasourceFormView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  ListBox database;

  @UiField
  TextBox defaultCreatedTimestampColumnName;

  @UiField
  TextBox defaultUpdatedTimestampColumnName;

  @UiField
  RadioButton useMetadataTablesOption;

  @UiField
  RadioButton doNotUseMetadataTablesOption;

  public JdbcDatasourceFormView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public String getSelectedDatabase() {
    return database.getItemText(database.getSelectedIndex());
  }

  @Override
  public void setDatabases(JsArray<JdbcDataSourceDto> resource) {
    database.clear();
    for(JdbcDataSourceDto dto : JsArrays.toIterable(resource)) {
      database.addItem(dto.getName());
    }
  }

  public HasValue<Boolean> getUseMetadataTablesOption() {
    return useMetadataTablesOption;
  }

  public HasValue<Boolean> getDoNotUseMetadataTablesOption() {
    return doNotUseMetadataTablesOption;
  }

  public HasText getDefaultCreatedTimestampColumnName() {
    return defaultCreatedTimestampColumnName;
  }

  public HasText getDefaultUpdatedTimestampColumnName() {
    return defaultUpdatedTimestampColumnName;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

}
