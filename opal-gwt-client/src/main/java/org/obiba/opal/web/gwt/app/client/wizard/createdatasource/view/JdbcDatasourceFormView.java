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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.JdbcDatasourceFormPresenter;
import org.obiba.opal.web.model.client.opal.JdbcDriverDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
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
  ListBox driver;

  @UiField
  TextBox url;

  @UiField
  TextBox username;

  @UiField
  PasswordTextBox password;

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

  public HasText getDriver() {
    return new HasText() {

      public String getText() {
        return (driver.getSelectedIndex() != -1) ? driver.getValue(driver.getSelectedIndex()) : null;
      }

      public void setText(String text) {
        if(text != null && driver.getItemCount() > 0) {
          for(int i = 0; i < driver.getItemCount(); i++) {
            if(driver.getValue(i).equals(text)) {
              driver.setSelectedIndex(i);
              break;
            }
          }
          driver.setSelectedIndex(0);
        }
      }
    };
  }

  public HasText getUrl() {
    return url;
  }

  public HasText getUsername() {
    return username;
  }

  public HasText getPassword() {
    return password;
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

  public void setJdbcDrivers(List<JdbcDriverDto> drivers) {
    driver.clear();
    for(JdbcDriverDto driverDto : drivers) {
      driver.addItem(driverDto.getDriverName(), driverDto.getDriverClass());
    }
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

}
