/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.view;

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabasePresenter.Display;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabasePresenter.Mode;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.model.client.opal.JdbcDriverDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class DatabaseView extends PopupViewImpl implements Display {

  @UiTemplate("DatabaseView.ui.xml")
  interface ViewUiBinder extends UiBinder<DialogBox, DatabaseView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel contentLayout;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox name;

  @UiField
  TextBox url;

  @UiField
  ListBox driver;

  @UiField
  TextBox username;

  @UiField
  TextBox password;

  @UiField
  TextArea properties;

  String driverClass;

  HasText hasTextListBox;

  JsArray<JdbcDriverDto> availableDrivers;

  @Inject
  public DatabaseView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
    hasTextListBox = new HasText() {

      @Override
      public String getText() {
        return driver.getValue(driver.getSelectedIndex());
      }

      @Override
      public void setText(String text) {
        driverClass = text;
      }
    };
  }

  private void initWidgets() {
    dialog.hide();
    properties.getElement().setAttribute("placeholder", translations.keyValueLabel());
    resizeHandle.makeResizable(contentLayout);
    driver.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        int index = driver.getSelectedIndex();
        JdbcDriverDto jdbcDriver = getDriver(driver.getValue(index));
        if(jdbcDriver != null) {
          url.setText(jdbcDriver.getJdbcUrlTemplate());
        }
      }
    });
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
  }

  @Override
  public void show() {
    name.setFocus(true);
    super.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDialogMode(Mode dialogMode) {
    name.setEnabled(Mode.CREATE == dialogMode);
    if(Mode.CREATE == dialogMode) {
      dialog.setText(translations.addDatabase());
    } else {
      dialog.setText(translations.editDatabase());
    }
  }

  @Override
  public HasClickHandlers getSaveButton() {
    return saveButton;
  }

  @Override
  public HasClickHandlers getCancelButton() {
    return cancelButton;
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public HasText getUrl() {
    return url;
  }

  @Override
  public HasText getDriver() {
    return hasTextListBox;
  }

  @Override
  public HasText getUsername() {
    return username;
  }

  @Override
  public HasText getPassword() {
    return password;
  }

  @Override
  public HasText getProperties() {
    return properties;
  }

  @Override
  public void setAvailableDrivers(JsArray<JdbcDriverDto> resource) {
    availableDrivers = resource;
    for(JdbcDriverDto driver : JsArrays.toIterable(resource)) {
      this.driver.addItem(driver.getDriverName(), driver.getDriverClass());
    }
    updateDriverSelection();
  }

  private JdbcDriverDto getDriver(String driverClass) {
    for(JdbcDriverDto driver : JsArrays.toIterable(availableDrivers)) {
      if(driver.getDriverClass().equals(driverClass)) {
        return driver;
      }
    }
    return null;
  }

  private void updateDriverSelection() {
    for(int i = 0; i < driver.getItemCount(); i++) {
      if(driver.getValue(i).equals(driverClass)) {
        driver.setSelectedIndex(i);
        break;
      }
    }
    if(Strings.isNullOrEmpty(getUrl().getText())) {
      JdbcDriverDto dto = getDriver(hasTextListBox.getText());
      if(dto != null) {
        getUrl().setText(dto.getJdbcUrlTemplate());
      }
    }
  }

}
