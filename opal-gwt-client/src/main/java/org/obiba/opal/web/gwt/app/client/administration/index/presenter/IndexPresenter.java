/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.presenter;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.Day;
import org.obiba.opal.web.model.client.opal.ScheduleDto;
import org.obiba.opal.web.model.client.opal.ScheduleType;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class IndexPresenter extends PresenterWidget<IndexPresenter.Display> {

  private Mode dialogMode;

  private List<TableIndexStatusDto> tableIndexStatusDtos;

  public enum Mode {
    UPDATE
  }

  @Inject
  public IndexPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.UPDATE);

    registerHandler(getView().getSaveButton().addClickHandler(new CreateOrUpdateMethodClickHandler()));

    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));
  }

  private void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

//  /**
//   * Setup the dialog for creating a method
//   */
//  public void createNewDatabase() {
//    setDialogMode(Mode.CREATE);
//  }

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

  private void updateSchedule() {
    ScheduleDto dto = getScheduleDto();
    for(TableIndexStatusDto tableIndexStatusDto : tableIndexStatusDtos) {
      putSchedule(tableIndexStatusDto.getDatasource(), tableIndexStatusDto.getTable(), dto);
    }
  }

  private void putSchedule(String datasource, String table, ScheduleDto dto) {
    CreateOrUpdateMethodCallBack callbackHandler = new CreateOrUpdateMethodCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource(Resources.updateSchedule(datasource, table)).put()//
        .withResourceBody(ScheduleDto.stringify(dto))//
        .withCallback(Response.SC_OK, callbackHandler).send();
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

  @SuppressWarnings("PMD.NcssMethodCount")
  private ScheduleType getScheduleTypeFromName(String type) {
    if(type.equals(ScheduleType.MINUTES_5.getName())) {
      return ScheduleType.MINUTES_5;
    } else if(type.equals(ScheduleType.MINUTES_15.getName())) {
      return ScheduleType.MINUTES_15;
    } else if(type.equals(ScheduleType.MINUTES_30.getName())) {
      return ScheduleType.MINUTES_30;
    } else if(type.equals(ScheduleType.HOURLY.getName())) {
      return ScheduleType.HOURLY;
    } else if(type.equals(ScheduleType.DAILY.getName())) {
      return ScheduleType.DAILY;
    } else if(type.equals(ScheduleType.WEEKLY.getName())) {
      return ScheduleType.WEEKLY;
    } else {
      return ScheduleType.NOT_SCHEDULED;
    }
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  private Day getDayFromName(String day) {
    if(day.equals(Day.SUNDAY.getName())) {
      return Day.SUNDAY;
    } else if(day.equals(Day.MONDAY.getName())) {
      return Day.MONDAY;
    } else if(day.equals(Day.TUESDAY.getName())) {
      return Day.TUESDAY;
    } else if(day.equals(Day.WEDNESDAY.getName())) {
      return Day.WEDNESDAY;
    } else if(day.equals(Day.THURSDAY.getName())) {
      return Day.THURSDAY;
    } else if(day.equals(Day.FRIDAY.getName())) {
      return Day.FRIDAY;
    } else if(day.equals(Day.SATURDAY.getName())) {
      return Day.SATURDAY;
    }
    return Day.MONDAY;
  }

  //
  // Inner classes and interfaces
  //
  public class CreateOrUpdateMethodClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.UPDATE) {
        updateSchedule();
      }
    }

  }

  private class CreateOrUpdateMethodCallBack implements ResponseCodeCallback {

    ScheduleDto dto;

    public CreateOrUpdateMethodCallBack(ScheduleDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        getEventBus().fireEvent(NotificationEvent.Builder.newNotification().info("IndexScheduleCompleted").build());
      } else {
        ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
        getEventBus().fireEvent(
            NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                .build());
      }
    }
  }

  public interface Display extends PopupView {

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    String getSelectedType();

    String getSelectedDay();

    int getSelectedHours();

    int getSelectedMinutes();

    void setSchedule(ScheduleDto dto);
  }

}
