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
import org.obiba.opal.web.gwt.app.client.workbench.view.Chooser;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.model.client.opal.Day;
import org.obiba.opal.web.model.client.opal.ScheduleDto;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

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
  Chooser type;

  @UiField
  Chooser day;

  @UiField
  Chooser hour;

  @UiField
  Chooser minutes;

  @UiField
  Label on;

  @UiField
  Label at;

  JsArray<ScheduleType> availableTypes;

  @Override
  public String getSelectedType() {
    return type.getSelectedValue();
  }

  @Override
  public String getSelectedDay() {
    return day.getSelectedValue();
  }

  @Override
  public int getSelectedHours() {
    return Integer.parseInt(hour.getSelectedValue().split(":")[0]);
  }

  @Override
  public int getSelectedMinutes() {
    if(type.getSelectedValue().equals(ScheduleType.HOURLY.getName())) {
      return Integer.parseInt(minutes.getSelectedValue());
    } else {
      return Integer.parseInt(hour.getSelectedValue().split(":")[1]);
    }
  }

  @Override
  public void setSchedule(ScheduleDto dto) {
    type.setSelectedValue(dto.getType().getName());
    setDefaults();
    if(dto.hasDay()) {
      day.setSelectedValue(dto.getDay().getName());
    }
    if(dto.hasHours() && dto.hasMinutes()) {
      hour.setSelectedValue(dto.getHours() + ":" + dto.getMinutes());
    }
    if(dto.hasMinutes()) {
      minutes.setSelectedValue(String.valueOf(dto.getMinutes()));
    }
  }

  @Inject
  public IndexView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
  }

  private void initWidgets() {
    dialog.hide();
    resizeHandle.makeResizable(contentLayout);

    initTypeWidget();
    initDayWidget();
    initHourWidget();
    initMinutesWidget();

    // default to 15 minutes
    type.setSelectedValue(ScheduleType.MINUTES_15.getName());
    on.setVisible(false);
    at.setVisible(false);
    day.setVisible(false);
    hour.setVisible(false);
    minutes.setVisible(false);
  }

  private void initTypeWidget() {
    type.setEnabled(true);
    type.addItem(translations.manuallyLabel(), ScheduleType.NOT_SCHEDULED.getName());
    type.addItem(translations.minutes5Label(), ScheduleType.MINUTES_5.getName());
    type.addItem(translations.minutes15Label(), ScheduleType.MINUTES_15.getName());
    type.addItem(translations.minutes30Label(), ScheduleType.MINUTES_30.getName());
    type.addItem(translations.hourlyLabel(), ScheduleType.HOURLY.getName());
    type.addItem(translations.dailyLabel(), ScheduleType.DAILY.getName());
    type.addItem(translations.weeklyLabel(), ScheduleType.WEEKLY.getName());
    type.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        setDefaults();
      }
    });
    //if (!type.getValue(type.getSelectedIndex()).equals(ScheduleType.NOT_SCHEDULED.getName())){
  }

  private void initDayWidget() {
    day.addItem(translations.mondayLabel(), Day.MONDAY.getName());
    day.addItem(translations.tuesdayLabel(), Day.TUESDAY.getName());
    day.addItem(translations.wednesdayLabel(), Day.WEDNESDAY.getName());
    day.addItem(translations.thursdayLabel(), Day.THURSDAY.getName());
    day.addItem(translations.fridayLabel(), Day.FRIDAY.getName());
    day.addItem(translations.saturdayLabel(), Day.SATURDAY.getName());
    day.addItem(translations.sundayLabel(), Day.SUNDAY.getName());
  }

  private void initHourWidget() {
    for(int i = 0; i < 24; i++) {
      String h = "";
      if(i < 10) {
        h += "0";
      }
      hour.addItem(h + i + ":00", i +":0");
      hour.addItem(h + i + ":15", i +":15");
      hour.addItem(h + i + ":30", i +":30");
      hour.addItem(h + i + ":45", i +":45");
    }
    hour.setWidth("80px");
  }

  private void initMinutesWidget() {
    minutes.addItem("00 " + translations.minutesLabel(), "0");
    minutes.addItem("15 " + translations.minutesLabel(), "15");
    minutes.addItem("30 " + translations.minutesLabel(), "30");
    minutes.addItem("45 " + translations.minutesLabel(), "45");
    minutes.setWidth("120px");
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private void setDefaults() {
    String typeValue = type.getSelectedValue();
    on.setVisible(typeValue.equals(ScheduleType.WEEKLY.getName()));
    day.setVisible(on.isVisible());
    at.setVisible(typeValue.equals(ScheduleType.HOURLY.getName()) || typeValue.equals(ScheduleType.DAILY.getName())
        || typeValue.equals(ScheduleType.WEEKLY.getName()));
    hour.setVisible(at.isVisible() && (typeValue.equals(ScheduleType.DAILY.getName())
        || typeValue.equals(ScheduleType.WEEKLY.getName())));
    minutes.setVisible(at.isVisible() && typeValue.equals(ScheduleType.HOURLY.getName()));
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

}
