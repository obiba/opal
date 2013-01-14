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
    getView().getType().setText(dto.getType().getName());
    getView().getDay().setText(dto.getDay().getName());
    getView().getHours().setText(String.valueOf(dto.getHours()));
    getView().getMinutes().setText(String.valueOf(dto.getMinutes()));
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

    if(getView().getType().getText().equals(ScheduleType.MINUTES_5.getName())) {
      dto.setType(ScheduleType.MINUTES_5);
      return dto;
    } else if(getView().getType().getText().equals(ScheduleType.MINUTES_15.getName())) {
      dto.setType(ScheduleType.MINUTES_15);
      return dto;
    } else if(getView().getType().getText().equals(ScheduleType.MINUTES_30.getName())) {
      dto.setType(ScheduleType.MINUTES_30);
      return dto;
    } else if(getView().getType().getText().equals(ScheduleType.HOURLY.getName())) {
      dto.setType(ScheduleType.HOURLY);
      dto.setMinutes(Integer.parseInt(getView().getMinutes().getText()));
      return dto;
    } else if(getView().getType().getText().equals(ScheduleType.DAILY.getName())) {
      dto.setType(ScheduleType.DAILY);
      dto.setHours(Integer.parseInt(getView().getHours().getText()));
      dto.setMinutes(Integer.parseInt(getView().getMinutes().getText()));
      return dto;
    } else if(getView().getType().getText().equals(ScheduleType.WEEKLY.getName())) {
      dto.setType(ScheduleType.WEEKLY);
      dto.setHours(Integer.parseInt(getView().getHours().getText()));
      dto.setMinutes(Integer.parseInt(getView().getMinutes().getText()));

      if(getView().getDay().getText().equals(Day.SUNDAY.getName())) {
        dto.setDay(Day.SUNDAY);
      } else if(getView().getDay().getText().equals(Day.MONDAY.getName())) {
        dto.setDay(Day.MONDAY);
      } else if(getView().getDay().getText().equals(Day.TUESDAY.getName())) {
        dto.setDay(Day.TUESDAY);
      } else if(getView().getDay().getText().equals(Day.WEDNESDAY.getName())) {
        dto.setDay(Day.WEDNESDAY);
      } else if(getView().getDay().getText().equals(Day.THURSDAY.getName())) {
        dto.setDay(Day.THURSDAY);
      } else if(getView().getDay().getText().equals(Day.FRIDAY.getName())) {
        dto.setDay(Day.FRIDAY);
      } else if(getView().getDay().getText().equals(Day.SATURDAY.getName())) {
        dto.setDay(Day.SATURDAY);
      }

      return dto;
    }

    dto.setType(ScheduleType.NOT_SCHEDULED);
    return dto;
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

    HasText getType();

    HasText getDay();

    HasText getHours();

    HasText getMinutes();
  }

}
