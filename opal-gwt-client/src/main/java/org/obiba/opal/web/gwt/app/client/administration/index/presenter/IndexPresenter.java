/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.index.event.TableIndicesRefreshEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableIndexStatusRefreshEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.Day;
import org.obiba.opal.web.model.client.opal.ScheduleDto;
import org.obiba.opal.web.model.client.opal.ScheduleType;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class IndexPresenter extends ModalPresenterWidget<IndexPresenter.Display> implements IndexUiHandlers {

  public interface Display extends PopupView, HasUiHandlers<IndexUiHandlers> {

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    String getSelectedType();

    String getSelectedDay();

    int getSelectedHours();

    int getSelectedMinutes();

    void setSchedule(ScheduleDto dto);
  }

  private Mode dialogMode;

  private List<TableIndexStatusDto> tableIndexStatusDtos;

  private boolean refreshIndices = true;

  private boolean refreshTable = false;

  public enum Mode {
    UPDATE
  }

  @Inject
  public IndexPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    if(dialogMode == Mode.UPDATE) {
      updateSchedule();
    }
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.UPDATE);
  }

  private void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  /**
   * Setup the dialog for updating an existing method
   *
   * @param dto method to update
   */
  public void updateSchedules(List<TableIndexStatusDto> dtos) {
    setDialogMode(Mode.UPDATE);
    tableIndexStatusDtos = dtos;

    if(dtos.size() == 1) {
      displaySchedule(dtos.get(0).getSchedule());
    }
  }

  private void displaySchedule(ScheduleDto dto) {
    getView().setSchedule(dto);
  }

  @SuppressWarnings({ "PMD.NcssMethodCount", "MethodOnlyUsedFromInnerClass" })
  private ScheduleType getScheduleTypeFromName(String type) {
    if(type.equals(ScheduleType.MINUTES_5.getName())) {
      return ScheduleType.MINUTES_5;
    }
    if(type.equals(ScheduleType.MINUTES_15.getName())) {
      return ScheduleType.MINUTES_15;
    }
    if(type.equals(ScheduleType.MINUTES_30.getName())) {
      return ScheduleType.MINUTES_30;
    }
    if(type.equals(ScheduleType.HOURLY.getName())) {
      return ScheduleType.HOURLY;
    }
    if(type.equals(ScheduleType.DAILY.getName())) {
      return ScheduleType.DAILY;
    }
    if(type.equals(ScheduleType.WEEKLY.getName())) {
      return ScheduleType.WEEKLY;
    }
    return ScheduleType.NOT_SCHEDULED;
  }

  @SuppressWarnings({ "PMD.NcssMethodCount", "MethodOnlyUsedFromInnerClass" })
  private Day getDayFromName(String day) {
    if(day.equals(Day.SUNDAY.getName())) {
      return Day.SUNDAY;
    }
    if(day.equals(Day.MONDAY.getName())) {
      return Day.MONDAY;
    }
    if(day.equals(Day.TUESDAY.getName())) {
      return Day.TUESDAY;
    }
    if(day.equals(Day.WEDNESDAY.getName())) {
      return Day.WEDNESDAY;
    }
    if(day.equals(Day.THURSDAY.getName())) {
      return Day.THURSDAY;
    }
    if(day.equals(Day.FRIDAY.getName())) {
      return Day.FRIDAY;
    }
    if(day.equals(Day.SATURDAY.getName())) {
      return Day.SATURDAY;
    }
    return Day.MONDAY;
  }

  private void updateSchedule() {
    ScheduleDto dto = getScheduleDto();
    for(TableIndexStatusDto tableIndexStatusDto : tableIndexStatusDtos) {
      putSchedule(tableIndexStatusDto.getDatasource(), tableIndexStatusDto.getTable(), dto);
    }
  }

  private ScheduleDto getScheduleDto() {
    ScheduleDto dto = ScheduleDto.create();
    ScheduleType type = getScheduleTypeFromName(getView().getSelectedType());
    dto.setType(type);

    if(type.equals(ScheduleType.HOURLY)) {
      dto.setMinutes(getView().getSelectedMinutes());
    } else if(type.equals(ScheduleType.DAILY)) {
      dto.setHours(getView().getSelectedHours());
      dto.setMinutes(getView().getSelectedMinutes());
    } else if(type.equals(ScheduleType.WEEKLY)) {
      dto.setHours(getView().getSelectedHours());
      dto.setMinutes(getView().getSelectedMinutes());
      dto.setDay(getDayFromName(getView().getSelectedDay()));
    }

    return dto;
  }

  private void putSchedule(String datasource, String table, ScheduleDto dto) {
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(Resources.updateSchedule(datasource, table)) //
        .withResourceBody(ScheduleDto.stringify(dto)) //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            if(refreshIndices) getEventBus().fireEvent(new TableIndicesRefreshEvent());
            if(refreshTable) getEventBus().fireEvent(new TableIndexStatusRefreshEvent());
          }
        }) //
        .put().send();
  }

  public void setUpdateMethodCallbackRefreshIndices(boolean b) {
    refreshIndices = b;
  }

  public void setUpdateMethodCallbackRefreshTable(boolean b) {
    refreshTable = b;
  }
}
