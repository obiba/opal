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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
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
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

public class ReportTemplateUpdateDialogPresenter extends PresenterWidget<ReportTemplateUpdateDialogPresenter.Display> {

  private FileSelectionPresenter fileSelectionPresenter;

  private ItemSelectorPresenter emailSelectorPresenter;

  private ItemSelectorPresenter parametersSelectorPresenter;

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  public interface Display extends PopupView {

    enum Slots {
      EMAIL, REPORT_PARAMS
    }

    void hideDialog();

    HasClickHandlers getUpdateReportTemplateButton();

    HasClickHandlers getCancelButton();

    void setName(String name);

    void setDesignFile(String designFile);

    void setFormat(String format);

    void setSchedule(String schedule);

    HasText getName();

    String getDesignFile();

    String getFormat();

    HasText getShedule();

    HasValue<Boolean> isScheduled();

    HandlerRegistration addDisableScheduleClickHandler(ClickHandler handler);

    HandlerRegistration addEnableScheduleClickHandler(ClickHandler handler);

    void setDesignFileWidgetDisplay(FileSelectionPresenter.Display display);

    void setEnabledReportTemplateName(boolean enabled);

  }

  @Inject
  public ReportTemplateUpdateDialogPresenter(Display display, EventBus eventBus,
      Provider<FileSelectionPresenter> fileSelectionPresenterProvider,
      Provider<ItemSelectorPresenter> itemSelectorPresenterProvider) {
    super(eventBus, display);
    this.fileSelectionPresenter = fileSelectionPresenterProvider.get();
    this.emailSelectorPresenter = itemSelectorPresenterProvider.get();
    this.parametersSelectorPresenter = itemSelectorPresenterProvider.get();
    parametersSelectorPresenter.getView().setItemInputDisplay(new KeyValueItemInputView());
    emailSelectorPresenter.getView().setItemInputDisplay(new TextBoxItemInputView());
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addEventHandlers();
    addValidators();
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getView().getName(), "ReportTemplateNameIsRequired"));
    validators.add(new FieldValidator() {

      @Override
      public String validate() {
        if(getView().getDesignFile().equals("")) {
          return "BirtReportDesignFileIsRequired";
        }
        return null;
      }
    });
    validators.add(new ConditionalValidator(getView().isScheduled(),
        new RequiredTextValidator(getView().getShedule(), "CronExpressionIsRequired")));
    validators.add(new FieldValidator() {

      @Override
      public String validate() {
        for(String email : emailSelectorPresenter.getView().getItems()) {
          if(!email
              .matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)")) {
            return "NotificationEmailsAreInvalid";
          }
        }
        return null;
      }
    });
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    fileSelectionPresenter.unbind();
    validators.clear();
  }

  protected void initDisplayComponents() {
    setInSlot(Display.Slots.EMAIL, emailSelectorPresenter);
    setInSlot(Display.Slots.REPORT_PARAMS, parametersSelectorPresenter);

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    fileSelectionPresenter.bind();
    getView().setDesignFileWidgetDisplay(fileSelectionPresenter.getDisplay());

  }

  private void addEventHandlers() {
    super.registerHandler(
        getView().getUpdateReportTemplateButton().addClickHandler(new CreateOrUpdateReportTemplateClickHandler()));

    super.registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

    super.registerHandler(getView().addEnableScheduleClickHandler(new EnableScheduleClickHandler()));
    super.registerHandler(getView().addDisableScheduleClickHandler(new DisableScheduleClickHandler()));

  }

  public void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setEnabledReportTemplateName(this.dialogMode == Mode.CREATE);
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
      UriBuilder ub = UriBuilder.create().segment("report-template", getView().getName().getText());

      ResourceRequestBuilderFactory.<ReportTemplateDto>newBuilder()
          .forResource(ub.build()).get()
          .withCallback(alreadyExistReportTemplateCallback)
          .withCallback(Response.SC_NOT_FOUND, createReportTemplateCallback).send();
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
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(messages).build());
      return false;
    } else {
      return true;
    }
  }

  private ReportTemplateDto getReportTemplateDto() {
    ReportTemplateDto reportTemplate = ReportTemplateDto.create();
    reportTemplate.setName(getView().getName().getText());
    String schedule = getView().getShedule().getText();
    if(schedule != null && schedule.trim().length() > 0) {
      reportTemplate.setCron(getView().getShedule().getText());
    }
    reportTemplate.setFormat(getView().getFormat());
    reportTemplate.setDesign(getView().getDesignFile());
    for(String email : emailSelectorPresenter.getView().getItems()) {
      reportTemplate.addEmailNotification(email);
    }
    ParameterDto parameterDto;
    for(String parameterStr : parametersSelectorPresenter.getView().getItems()) {
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
    UriBuilder ub = UriBuilder.create().segment("report-temnplate", getView().getName().getText());
    ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).put()
        .withResourceBody(ReportTemplateDto.stringify(reportTemplate)).withCallback(Response.SC_OK, callbackHandler)
        .withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler)
        .send();
  }

  private void doCreateReportTemplate() {
    ReportTemplateDto reportTemplate = getReportTemplateDto();
    CreateOrUpdateReportTemplateCallBack callbackHandler = new CreateOrUpdateReportTemplateCallBack(reportTemplate);
    ResourceRequestBuilderFactory.newBuilder().forResource("/report-templates").post()
        .withResourceBody(ReportTemplateDto.stringify(reportTemplate)).withCallback(Response.SC_OK, callbackHandler)
        .withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler)
        .send();
  }

  private class AlreadyExistReportTemplateCallBack implements ResourceCallback<ReportTemplateDto> {

    @Override
    public void onResource(Response response, ReportTemplateDto resource) {
      getEventBus()
          .fireEvent(NotificationEvent.newBuilder().error("ReportTemplateAlreadyExistForTheSpecifiedName").build());
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
      getView().isScheduled().setValue(true);
    }
  }

  private class DisableScheduleClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getView().getShedule().setText("");
    }
  }

  private class CreateOrUpdateReportTemplateCallBack implements ResponseCodeCallback {

    ReportTemplateDto reportTemplate;

    public CreateOrUpdateReportTemplateCallBack(ReportTemplateDto reportTemplate) {
      this.reportTemplate = reportTemplate;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        getEventBus().fireEvent(new ReportTemplateUpdatedEvent(reportTemplate));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(new ReportTemplateCreatedEvent(reportTemplate));
      } else {
        String msg = "UnknownError";
        if(response.getText() != null && response.getText().length() != 0) {
          try {
            ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
            msg = errorDto.getStatus();
          } catch(Exception e) {

          }
        }
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(msg).build());
      }
    }
  }

  /**
   * @param reportTemplateDetails
   */
  public void setReportTemplate(ReportTemplateDto reportTemplate) {
    getView().setDesignFile(reportTemplate.getDesign());
    getView().setFormat(reportTemplate.getFormat());
    getView().setName(reportTemplate.getName());
    emailSelectorPresenter.getView().setItems(JsArrays.toIterable(reportTemplate.getEmailNotificationArray()));
    parametersSelectorPresenter.getView().setItems(Iterables
        .transform(JsArrays.toIterable(reportTemplate.getParametersArray()), new Function<ParameterDto, String>() {

          @Override
          public String apply(ParameterDto input) {
            return input.getKey() + "=" + input.getValue();
          }
        }));
    getView().setSchedule(reportTemplate.getCron());
  }

}
