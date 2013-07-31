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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateCreatedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.view.KeyValueItemInputView;
import org.obiba.opal.web.gwt.app.client.view.TextBoxItemInputView;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class ReportTemplateUpdateDialogPresenter extends ModalPresenterWidget<ReportTemplateUpdateDialogPresenter.Display>
    implements ReportTemplateUpdateDialogUiHandlers {

  private final FileSelectionPresenter fileSelectionPresenter;

  private final ItemSelectorPresenter emailSelectorPresenter;

  private final ItemSelectorPresenter parametersSelectorPresenter;

  private final NotificationPresenter errorNotificationPresenter;

  private final Collection<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  public interface Display extends PopupView, HasUiHandlers<ReportTemplateUpdateDialogUiHandlers> {

    public enum FormField {
      NAME,
      TEMPLE_FILE,
      EMAILS,
      CRON_EXPRESSION
    }

    enum Slots {
      ERROR, EMAIL, REPORT_PARAMS
    }

    void hideDialog();

    void setName(String name);

    void setDesignFile(String designFile);

    void setFormat(String format);

    void setSchedule(String schedule);

    HasText getName();

    String getDesignFile();

    String getFormat();

    HasText getShedule();

    HasValue<Boolean> isScheduled();

    void setDesignFileWidgetDisplay(FileSelectionPresenter.Display display);

    void setEnabledReportTemplateName(boolean enabled);

    void setErrors(List<String> messages, List<FormField> ids);

    void clearErrors();
  }

  @Inject
  public ReportTemplateUpdateDialogPresenter(Display display, EventBus eventBus,
      Provider<FileSelectionPresenter> fileSelectionPresenterProvider,
      Provider<ItemSelectorPresenter> itemSelectorPresenterProvider, Provider<NotificationPresenter> errorNotificationPresenterProvider) {
    super(eventBus, display);
    fileSelectionPresenter = fileSelectionPresenterProvider.get();
    emailSelectorPresenter = itemSelectorPresenterProvider.get();
    parametersSelectorPresenter = itemSelectorPresenterProvider.get();
    parametersSelectorPresenter.getView().setItemInputDisplay(new KeyValueItemInputView());
    emailSelectorPresenter.getView().setItemInputDisplay(new TextBoxItemInputView());
    errorNotificationPresenter = errorNotificationPresenterProvider.get();
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addValidators();
  }

  private void addValidators() {
    validators.add(new RequiredTextValidator(getView().getName(), "ReportTemplateNameIsRequired")
        .setId(Display.FormField.TEMPLE_FILE.ordinal()));

    validators.add(new FieldValidator() {

      @Nullable
      @Override
      public String validate() {
        if("".equals(getView().getDesignFile())) {
          return "BirtReportDesignFileIsRequired";
        }
        return null;
      }

      @Override
      public int getId() {
        return Display.FormField.NAME.ordinal();
      }
    });

    validators.add(new ConditionalValidator(getView().isScheduled(),
        new RequiredTextValidator(getView().getShedule(), "CronExpressionIsRequired"))
        .setId(Display.FormField.CRON_EXPRESSION.ordinal()));

    validators.add(new FieldValidator() {

      @Nullable
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

      @Override
      public int getId() {
        return Display.FormField.EMAILS.ordinal();
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
    setInSlot(Display.Slots.ERROR, errorNotificationPresenter);
    setInSlot(Display.Slots.EMAIL, emailSelectorPresenter);
    setInSlot(Display.Slots.REPORT_PARAMS, parametersSelectorPresenter);

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    fileSelectionPresenter.bind();
    getView().setDesignFileWidgetDisplay(fileSelectionPresenter.getView());

  }

  public void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setEnabledReportTemplateName(this.dialogMode == Mode.CREATE);
  }

  @Override
  public void onDialogHidden() {
    errorNotificationPresenter.close();
    getView().clearErrors();
  }

  @Override
  public void updateReportTemplate() {
    if(dialogMode == Mode.CREATE) {
      createReportTemplate();
    } else if(dialogMode == Mode.UPDATE) {
      updateReportTemplateInternal();
    }
  }

  @Override
  public void onDialogHide() {
    getView().hideDialog();
  }

  @Override
  public void enableSchedule() {
    getView().isScheduled().setValue(true);
  }

  @Override
  public void disableSchedule() {
    getView().getShedule().setText("");
  }

  private void createReportTemplate() {
    if(validReportTemplate()) {
      ResponseCodeCallback createReportTemplateCallback = new CreateReportTemplateCallBack();
      ResourceCallback alreadyExistReportTemplateCallback = new AlreadyExistReportTemplateCallBack();
      UriBuilder ub = UriBuilder.create().segment("report-template", getView().getName().getText());

      ResourceRequestBuilderFactory.<ReportTemplateDto>newBuilder().forResource(ub.build()).get()
          .withCallback(alreadyExistReportTemplateCallback)
          .withCallback(Response.SC_NOT_FOUND, createReportTemplateCallback).send();
    }
  }

  private void updateReportTemplateInternal() {
    if(validReportTemplate()) {
      doUpdateReportTemplate();
    }
  }

  private boolean validReportTemplate() {
    errorNotificationPresenter.close();
    getView().clearErrors();

    List<Display.FormField> validatorIds = new ArrayList<Display.FormField>();
    List<String> messages = new ArrayList<String>();
    String message;
    for(FieldValidator validator : validators) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
        validatorIds.add(Display.FormField.values()[validator.getId()]);
      }
    }

    if(messages.size() > 0) {
      errorNotificationPresenter
          .setNotification(NotificationPresenter.NotificationType.ERROR, messages, null, null, true,
              new ErrorNotificationErrorCloseHandler());

      getView().setErrors(messages, validatorIds);
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
    ResponseCodeCallback callbackHandler = new CreateOrUpdateReportTemplateCallBack(reportTemplate);
    UriBuilder ub = UriBuilder.create().segment("report-template", getView().getName().getText());
    ResourceRequestBuilderFactory.newBuilder().forResource(ub.build()).put()
        .withResourceBody(ReportTemplateDto.stringify(reportTemplate)).withCallback(Response.SC_OK, callbackHandler)
        .withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler)
        .send();
  }

  private void doCreateReportTemplate() {
    ReportTemplateDto reportTemplate = getReportTemplateDto();
    ResponseCodeCallback callbackHandler = new CreateOrUpdateReportTemplateCallBack(reportTemplate);
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

  private class CreateOrUpdateReportTemplateCallBack implements ResponseCodeCallback {

    ReportTemplateDto reportTemplate;

    private CreateOrUpdateReportTemplateCallBack(ReportTemplateDto reportTemplate) {
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
            ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
            msg = errorDto.getStatus();
          } catch(Exception ignored) {

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

  private class ErrorNotificationErrorCloseHandler implements ClosedHandler {

    @Override
    public void onClosed(ClosedEvent closedEvent) {
      getView().clearErrors();
    }
  }
}
