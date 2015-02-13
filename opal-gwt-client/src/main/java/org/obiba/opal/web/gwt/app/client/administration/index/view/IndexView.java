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
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.Day;
import org.obiba.opal.web.model.client.opal.ScheduleDto;
import org.obiba.opal.web.model.client.opal.ScheduleType;

import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;

/**
 *
 */
public class IndexView extends ModalPopupViewWithUiHandlers<IndexUiHandlers> implements IndexPresenter.Display {

  private static final int DIALOG_MIN_WIDTH = 400;

  private static final int DIALOG_MIN_HEIGHT = 400;

  interface Binder extends UiBinder<Widget, IndexView> {}

  @UiField
  Modal dialog;

  @UiField
  Chooser type;

  @UiField
  Chooser day;

  @UiField
  Chooser hour;

  @UiField
  Chooser minutes;

  @UiField
  ControlLabel on;

  @UiField
  ControlLabel at;

  private final Translations translations;

  @Inject
  public IndexView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initWidgets();
  }

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
    return type.getSelectedValue().equals(ScheduleType.HOURLY.getName())
        ? Integer.parseInt(minutes.getSelectedValue())
        : Integer.parseInt(hour.getSelectedValue().split(":")[1]);
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

  private void initWidgets() {
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

    dialog.setMinWidth(DIALOG_MIN_WIDTH);
    dialog.setMinHeight(DIALOG_MIN_HEIGHT);
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
    type.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {

      @Override
      public void onChange(ChosenChangeEvent chosenChangeEvent) {
        setDefaults();
      }
    });
  }

  private void initDayWidget() {
    day.addItem(translations.timeMap().get(Day.MONDAY.getName()), Day.MONDAY.getName());
    day.addItem(translations.timeMap().get(Day.TUESDAY.getName()), Day.TUESDAY.getName());
    day.addItem(translations.timeMap().get(Day.WEDNESDAY.getName()), Day.WEDNESDAY.getName());
    day.addItem(translations.timeMap().get(Day.THURSDAY.getName()), Day.THURSDAY.getName());
    day.addItem(translations.timeMap().get(Day.FRIDAY.getName()), Day.FRIDAY.getName());
    day.addItem(translations.timeMap().get(Day.SATURDAY.getName()), Day.SATURDAY.getName());
    day.addItem(translations.timeMap().get(Day.SUNDAY.getName()), Day.SUNDAY.getName());
  }

  private void initHourWidget() {
    for(int i = 0; i < 24; i++) {
      String h = "";
      if(i < 10) {
        h += "0";
      }
      hour.addItem(h + i + ":00", i + ":0");
      hour.addItem(h + i + ":15", i + ":15");
      hour.addItem(h + i + ":30", i + ":30");
      hour.addItem(h + i + ":45", i + ":45");
    }
    hour.setWidth("80px");
  }

  private void initMinutesWidget() {
    for(int i = 0; i < 60; i = i + 5) {
      String m = "";
      if(i < 10) {
        m += "0";
      }
      minutes.addItem(m + i + " " + translations.minutesLabel(), Integer.toString(i));
    }
    minutes.setWidth("120px");
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private void setDefaults() {
    String typeValue = type.getSelectedValue();
    on.setVisible(typeValue.equals(ScheduleType.WEEKLY.getName()));
    day.setVisible(on.isVisible());
    at.setVisible(typeValue.equals(ScheduleType.HOURLY.getName()) || typeValue.equals(ScheduleType.DAILY.getName()) ||
        typeValue.equals(ScheduleType.WEEKLY.getName()));
    hour.setVisible(at.isVisible() &&
        (typeValue.equals(ScheduleType.DAILY.getName()) || typeValue.equals(ScheduleType.WEEKLY.getName())));
    minutes.setVisible(at.isVisible() && typeValue.equals(ScheduleType.HOURLY.getName()));
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDialogMode(IndexPresenter.Mode dialogMode) {
    //name.setEnabled(IndexPresenter.Mode.UPDATE.equals(dialogMode));
    dialog.setTitle(translations.editScheduleLabel());
  }

  @UiHandler("saveButton")
  public void onSaveButton(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

}
