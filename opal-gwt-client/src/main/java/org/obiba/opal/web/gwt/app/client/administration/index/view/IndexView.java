/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.view;

import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.model.client.opal.Day;
import org.obiba.opal.web.model.client.opal.ScheduleType;

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
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

import static org.obiba.opal.web.model.client.opal.ScheduleType.DAILY;
import static org.obiba.opal.web.model.client.opal.ScheduleType.HOURLY;
import static org.obiba.opal.web.model.client.opal.ScheduleType.MINUTES_15;
import static org.obiba.opal.web.model.client.opal.ScheduleType.MINUTES_30;
import static org.obiba.opal.web.model.client.opal.ScheduleType.MINUTES_5;
import static org.obiba.opal.web.model.client.opal.ScheduleType.NOT_SCHEDULED;
import static org.obiba.opal.web.model.client.opal.ScheduleType.WEEKLY;

/**
 *
 */
public class IndexView extends PopupViewImpl implements IndexPresenter.Display {

  @UiTemplate("IndexView.ui.xml")
  interface ViewUiBinder extends UiBinder<DialogBox, IndexView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

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
  com.github.gwtbootstrap.client.ui.ListBox type;

  @UiField
  com.github.gwtbootstrap.client.ui.ListBox day;

  @UiField
  com.github.gwtbootstrap.client.ui.ListBox hour;

  @UiField
  com.github.gwtbootstrap.client.ui.ListBox minutes;

  HasText hasTextListBox;

  JsArray<ScheduleType> availableTypes;

  @Override
  public HasText getType() {
    return hasTextListBox;
  }

  @Inject
  public IndexView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
    hasTextListBox = new HasText() {

      @Override
      public String getText() {
        return type.getValue(type.getSelectedIndex());
      }

      @Override
      public void setText(String text) {
        GWT.log("set text" + text);
        //driverClass = text;
      }
    };
  }

  private void initWidgets() {
    dialog.hide();
    //properties.getElement().setAttribute("placeholder", translations.keyValueLabel());
    resizeHandle.makeResizable(contentLayout);

    type.setEnabled(true);

    type.addItem(translations.manuallyLabel(), NOT_SCHEDULED.getName());
    type.addItem(translations.minutes5Label(), MINUTES_5.getName());
    type.addItem(translations.minutes15Label(), MINUTES_15.getName());
    type.addItem(translations.minutes30Label(), MINUTES_30.getName());
    type.addItem(translations.hourlyLabel(), HOURLY.getName());
    type.addItem(translations.dailyLabel(), DAILY.getName());
    type.addItem(translations.weeklyLabel(), WEEKLY.getName());

    type.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        int index = type.getSelectedIndex();

        if(type.getValue(index).equals(ScheduleType.NOT_SCHEDULED.getName()) || type.getValue(index)
            .equals(ScheduleType.MINUTES_5.getName()) ||
            type.getValue(index).equals(ScheduleType.MINUTES_15.getName()) || type.getValue(index)
            .equals(ScheduleType.MINUTES_30.getName())) {
          day.setVisible(false);
          hour.setVisible(false);
          minutes.setVisible(false);
        } else if(type.getValue(index).equals(ScheduleType.HOURLY.getName())) {
          day.setVisible(false);
          hour.setVisible(false);
          minutes.setVisible(true);
        } else if(type.getValue(index).equals(ScheduleType.DAILY.getName())) {
          day.setVisible(false);
          hour.setVisible(true);
          minutes.setVisible(true);
        } else if(type.getValue(index).equals(ScheduleType.WEEKLY.getName())) {
          day.setVisible(true);
          hour.setVisible(true);
          minutes.setVisible(true);
        }

//        JdbcDriverDto jdbcDriver = getDriver(type.getValue(index));
//        if(jdbcDriver != null) {
//          url.setText(jdbcDriver.getJdbcUrlTemplate());
//        }
      }
    });

    //if (!type.getValue(type.getSelectedIndex()).equals(ScheduleType.NOT_SCHEDULED.getName())){
    day.addItem(translations.sundayLabel(), Day.SUNDAY.getName());
    day.addItem(translations.mondayLabel(), Day.MONDAY.getName());
    day.addItem(translations.tuesdayLabel(), Day.TUESDAY.getName());
    day.addItem(translations.wednesdayLabel(), Day.WEDNESDAY.getName());
    day.addItem(translations.thursdayLabel(), Day.THURSDAY.getName());
    day.addItem(translations.fridayLabel(), Day.FRIDAY.getName());
    day.addItem(translations.saturdayLabel(), Day.SATURDAY.getName());

    for(int i = 0; i < 24; i++) {
      String h = "";
      if(i < 10) {
        h += "0";
      }
      hour.addItem(h + i);
    }

    for(int i = 0; i < 60; i++) {
      String m = "";
      if(i < 10) {
        m += "0";
      }
      minutes.addItem(m + i);
    }
    //}
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
    //name.setFocus(true);
    super.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDialogMode(IndexPresenter.Mode dialogMode) {
    //name.setEnabled(IndexPresenter.Mode.UPDATE.equals(dialogMode));
    dialog.setText(translations.editScheduleLabel());
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
  public void setAvailableType(JsArray<ScheduleType> resource) {
    availableTypes = resource;
    for(ScheduleType type : JsArrays.toIterable(resource)) {
      this.type.addItem(type.getName(), type.getName());
    }
    updateTypeSelection();
  }

  private ScheduleType getType(String typeName) {
    for(ScheduleType type : JsArrays.toIterable(availableTypes)) {
      if(type.getName().equals(typeName)) {
        return type;
      }
    }
    return null;
  }

  private void updateTypeSelection() {
    for(int i = 0; i < type.getItemCount(); i++) {
      if(type.getValue(i).equals(hasTextListBox)) {
        type.setSelectedIndex(i);
        break;
      }
    }
  }
}
