/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.presenter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateCreatedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ItemSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.KeyValueItemInputView;
import org.obiba.opal.web.gwt.app.client.widgets.view.TextBoxItemInputView;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ReportTemplateUpdateDialogPresenter extends WidgetPresenter<ReportTemplateUpdateDialogPresenter.Display> {

  private FileSelectionPresenter fileSelectionPresenter;

  private ItemSelectorPresenter emailSelectorPresenter;

  private ItemSelectorPresenter parametersSelectorPresenter;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    HasClickHandlers getUpdateReportTemplateButton();

    HasClickHandlers getCancelButton();

    HasCloseHandlers<DialogBox> getDialog();

    void setName(String name);

    void setDesignFile(String designFile);

    void setFormat(String format);

    void setSchedule(String schedule);

    void setNotificationEmails(List<String> emails);

    void setReportParameters(List<String> params);

    HasText getName();

    String getDesignFile();

    String getFormat();

    HasText getShedule();

    HasValue<Boolean> isScheduled();

    List<String> getNotificationEmails();

    List<String> getReportParameters();

    HandlerRegistration addDisableScheduleClickHandler(ClickHandler handler);

    HandlerRegistration addEnableScheduleClickHandler(ClickHandler handler);

    void setDesignFileWidgetDisplay(FileSelectionPresenter.Display display);

    void setNotificationEmailsWidgetDisplay(ItemSelectorPresenter.Display display);

    void setReportParametersWidgetDisplay(ItemSelectorPresenter.Display display);

    void setEnabledReportTemplateName(boolean enabled);

  }

  @Inject
  public ReportTemplateUpdateDialogPresenter(Display display, EventBus eventBus, Provider<FileSelectionPresenter> fileSelectionPresenterProvider, Provider<ItemSelectorPresenter> emailSelectorPresenterProvider, ItemSelectorPresenter parametersSelectorPresenter) {
    super(display, eventBus);
    this.fileSelectionPresenter = fileSelectionPresenterProvider.get();
    this.emailSelectorPresenter = emailSelectorPresenterProvider.get();
    this.parametersSelectorPresenter = parametersSelectorPresenter;
    parametersSelectorPresenter.getDisplay().setItemInputDisplay(new KeyValueItemInputView());
    emailSelectorPresenter.getDisplay().setItemInputDisplay(new TextBoxItemInputView());
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addEventHandlers();
    addValidators();
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getDisplay().getName(), "ReportTemplateNameIsRequired"));
    validators.add(new FieldValidator() {

      @Override
      public String validate() {
        if(getDisplay().getDesignFile().equals("")) {
          return "BirtReportDesignFileIsRequired";
        }
        return null;
      }
    });
    validators.add(new ConditionalValidator(getDisplay().isScheduled(), new RequiredTextValidator(getDisplay().getShedule(), "CronExpressionIsRequired")));
    validators.add(new FieldValidator() {

      @Override
      public String validate() {
        List<String> emails = getDisplay().getNotificationEmails();
        for(String email : emails) {
          if(!email.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)")) {
            return "NotificationEmailsAreInvalid";
          }
        }
        return null;
      }
    });
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    emailSelectorPresenter.unbind();
    parametersSelectorPresenter.unbind();
    fileSelectionPresenter.unbind();
    validators.clear();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  protected void initDisplayComponents() {
    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    fileSelectionPresenter.bind();
    emailSelectorPresenter.bind();
    parametersSelectorPresenter.bind();
    getDisplay().setNotificationEmailsWidgetDisplay(emailSelectorPresenter.getDisplay());
    getDisplay().setReportParametersWidgetDisplay(parametersSelectorPresenter.getDisplay());
    getDisplay().setDesignFileWidgetDisplay(fileSelectionPresenter.getDisplay());

  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getUpdateReportTemplateButton().addClickHandler(new CreateOrUpdateReportTemplateClickHandler()));

    super.registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    super.registerHandler(getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    }));

    super.registerHandler(getDisplay().addEnableScheduleClickHandler(new EnableScheduleClickHandler()));
    super.registerHandler(getDisplay().addDisableScheduleClickHandler(new DisableScheduleClickHandler()));

  }

  public void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
  }

  private void updateReportTemplate() {
    if(validReportTemplate()) {
      doUpdateReportTemplate();
    }
  }

  private void createReportTemplate() {
    if(validReportTemplate()) {
      CreateReportTemplateCallBack createReportTemplateCallback = new CreateReportTemplateCallBack();
      AlreadyExistReportTemplateCallBack alreadyExistReportTemplateCallback = new AlreadyExistReportTemplateCallBack();
      ResourceRequestBuilderFactory.<ReportTemplateDto> newBuilder().forResource("/report-template/" + getDisplay().getName().getText()).get().withCallback(alreadyExistReportTemplateCallback).withCallback(Response.SC_NOT_FOUND, createReportTemplateCallback).send();
    }
  }

  private boolean validReportTemplate() {
    List<String> messages = new ArrayList<String>();
    String message;
    for(FieldValidator validator : validators) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
      }
    }

    if(messages.size() > 0) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error(messages).build());
      return false;
    } else {
      return true;
    }
  }

  private ReportTemplateDto getReportTemplateDto() {
    ReportTemplateDto reportTemplate = ReportTemplateDto.create();
    reportTemplate.setName(getDisplay().getName().getText());
    String schedule = getDisplay().getShedule().getText();
    if(schedule != null && schedule.trim().length() > 0) {
      reportTemplate.setCron(getDisplay().getShedule().getText());
    }
    reportTemplate.setFormat(getDisplay().getFormat());
    reportTemplate.setDesign(getDisplay().getDesignFile());
    int i = 0;
    for(String email : getDisplay().getNotificationEmails()) {
      reportTemplate.addEmailNotification(email);
    }
    ParameterDto parameterDto;
    for(String parameterStr : getDisplay().getReportParameters()) {
      parameterDto = ParameterDto.create();
      parameterDto.setValue(getParameterValue(parameterStr));
      parameterDto.setKey(getParameterKey(parameterStr));
      reportTemplate.addParameters(parameterDto);
    }
    return reportTemplate;
  }

  private String getParameterKey(String parameterStr) {
    return parameterStr.split("=")[0];
  }

  private String getParameterValue(String parameterStr) {
    return parameterStr.split("=")[1];
  }

  private void doUpdateReportTemplate() {
    ReportTemplateDto reportTemplate = getReportTemplateDto();
    CreateOrUpdateReportTemplateCallBack callbackHandler = new CreateOrUpdateReportTemplateCallBack(reportTemplate);
    ResourceRequestBuilderFactory.newBuilder().forResource("/report-template/" + getDisplay().getName().getText()).put().withResourceBody(ReportTemplateDto.stringify(reportTemplate)).withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private void doCreateReportTemplate() {
    ReportTemplateDto reportTemplate = getReportTemplateDto();
    CreateOrUpdateReportTemplateCallBack callbackHandler = new CreateOrUpdateReportTemplateCallBack(reportTemplate);
    ResourceRequestBuilderFactory.newBuilder().forResource("/report-templates").post().withResourceBody(ReportTemplateDto.stringify(reportTemplate)).withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private class AlreadyExistReportTemplateCallBack implements ResourceCallback<ReportTemplateDto> {

    @Override
    public void onResource(Response response, ReportTemplateDto resource) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error("ReportTemplateAlreadyExistForTheSpecifiedName").build());
    }

  }

  private class CreateReportTemplateCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      doCreateReportTemplate();
    }
  }

  public class CreateOrUpdateReportTemplateClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.CREATE) {
        createReportTemplate();
      } else if(dialogMode == Mode.UPDATE) {
        updateReportTemplate();
      }
    }

  }

  private class EnableScheduleClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getDisplay().isScheduled().setValue(true);
    }
  }

  private class DisableScheduleClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getDisplay().getShedule().setText("");
    }
  }

  private class CreateOrUpdateReportTemplateCallBack implements ResponseCodeCallback {

    ReportTemplateDto reportTemplate;

    public CreateOrUpdateReportTemplateCallBack(ReportTemplateDto reportTemplate) {
      this.reportTemplate = reportTemplate;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getDisplay().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        eventBus.fireEvent(new ReportTemplateUpdatedEvent(reportTemplate));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new ReportTemplateCreatedEvent(reportTemplate));
      } else {
        String msg = "UnknownError";
        if(response.getText() != null && response.getText().length() != 0) {
          try {
            ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
            msg = errorDto.getStatus();
          } catch(Exception e) {

          }
        }
        eventBus.fireEvent(NotificationEvent.newBuilder().error(msg).build());
      }
    }
  }

}
