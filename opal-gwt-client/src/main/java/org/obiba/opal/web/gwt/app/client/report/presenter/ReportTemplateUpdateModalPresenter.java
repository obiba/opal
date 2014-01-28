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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateCreatedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.view.KeyValueItemInputView;
import org.obiba.opal.web.gwt.app.client.view.TextBoxItemInputView;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

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

public class ReportTemplateUpdateModalPresenter extends ModalPresenterWidget<ReportTemplateUpdateModalPresenter.Display>
    implements ReportTemplateUpdateModalUiHandlers {

  private final FileSelectionPresenter fileSelectionPresenter;

  private final ItemSelectorPresenter emailSelectorPresenter;

  private final ItemSelectorPresenter parametersSelectorPresenter;

  private final Collection<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  private String project;

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  public interface Display extends PopupView, HasUiHandlers<ReportTemplateUpdateModalUiHandlers> {

    void setReportTemplate(ReportTemplateDto reportTemplate);

    void clear();

    enum FormField {
      NAME,
      TEMPLATE_FILE,
      EMAILS,
      CRON_EXPRESSION
    }

    enum Slots {
      EMAIL, REPORT_PARAMS
    }

    void showErrors(List<String> messages);

    void hideDialog();

    HasText getName();

    String getDesignFile();

    HasText getSchedule();

    HasValue<Boolean> isScheduled();

    void setDesignFileWidgetDisplay(FileSelectionPresenter.Display display);

    void setEnabledReportTemplateName(boolean enabled);

    void setErrors(List<String> messages, List<FormField> ids);

    void clearErrors();
  }

  @Inject
  public ReportTemplateUpdateModalPresenter(Display display, EventBus eventBus,
      Provider<FileSelectionPresenter> fileSelectionPresenterProvider,
      Provider<ItemSelectorPresenter> itemSelectorPresenterProvider) {
    super(eventBus, display);
    fileSelectionPresenter = fileSelectionPresenterProvider.get();
    emailSelectorPresenter = itemSelectorPresenterProvider.get();
    parametersSelectorPresenter = itemSelectorPresenterProvider.get();
    parametersSelectorPresenter.getView().setItemInputDisplay(new KeyValueItemInputView());
    emailSelectorPresenter.getView().setItemInputDisplay(new TextBoxItemInputView());
    getView().setUiHandlers(this);
  }

  public void setProject(String project) {
    this.project = project;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addValidators();
  }

  private void addValidators() {
    validators.add(
        new RequiredTextValidator(getView().getName(), "ReportTemplateNameIsRequired", Display.FormField.NAME.name()));

    validators.add(new ConditionValidator(fileExtensionCondition(), "RReportDesignFileIsRequired",
        Display.FormField.TEMPLATE_FILE.name()));

    validators.add(new ConditionValidator(cronCondition(getView().isScheduled(), getView().getSchedule()),
        "CronExpressionIsRequired", Display.FormField.CRON_EXPRESSION.name()));

    validators
        .add(new ConditionValidator(emailCondition(), "NotificationEmailsAreInvalid", Display.FormField.EMAILS.name()));
  }

  private HasValue<Boolean> fileExtensionCondition() {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        return getView().getDesignFile().endsWith(".Rmd");
      }
    };
  }

  private HasValue<Boolean> emailCondition() {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        for(String email : emailSelectorPresenter.getView().getItems()) {
          if(!email
              .matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)")) {
            return false;
          }
        }
        return true;
      }
    };
  }

  private HasValue<Boolean> cronCondition(final HasValue<Boolean> isScheduled, final HasText cron) {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        return !(isScheduled.getValue() && cron.getText().isEmpty());
      }
    };
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

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
    fileSelectionPresenter.bind();
    getView().setDesignFileWidgetDisplay(fileSelectionPresenter.getView());

  }

  public void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setEnabledReportTemplateName(this.dialogMode == Mode.CREATE);
  }

  @Override
  public void onDialogHidden() {
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
    getView().getSchedule().setText("");
  }

  private void createReportTemplate() {
    if(validReportTemplate()) {
      ReportTemplateDto reportTemplate = getReportTemplateDto();
      String uri = UriBuilder.create().segment("report-templates").build();
      if(project != null) {
        reportTemplate.setProject(project);
        uri = UriBuilders.PROJECT_REPORT_TEMPLATES.create().build(project);
      }
      ResponseCodeCallback callbackHandler = new CreateOrUpdateReportTemplateCallBack(reportTemplate);
      ResourceRequestBuilderFactory.newBuilder().forResource(uri).post()
          .withResourceBody(ReportTemplateDto.stringify(reportTemplate)).withCallback(Response.SC_OK, callbackHandler)
          .withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler)
          .send();
    }
  }

  private void updateReportTemplateInternal() {
    if(validReportTemplate()) {
      doUpdateReportTemplate();
    }
  }

  private boolean validReportTemplate() {
    getView().clearErrors();

    List<Display.FormField> validatorIds = new ArrayList<Display.FormField>();
    List<String> messages = new ArrayList<String>();
    String message;
    for(FieldValidator validator : validators) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
        validatorIds.add(Display.FormField.valueOf(validator.getId()));
      }
    }

    if(messages.size() > 0) {
      getView().showErrors(messages);

      getView().setErrors(messages, validatorIds);
      return false;
    } else {
      return true;
    }

  }

  private ReportTemplateDto getReportTemplateDto() {
    ReportTemplateDto reportTemplate = ReportTemplateDto.create();
    reportTemplate.setName(getView().getName().getText());
    String schedule = getView().getSchedule().getText();
    if(schedule != null && schedule.trim().length() > 0) {
      reportTemplate.setCron(getView().getSchedule().getText());
    }
    reportTemplate.setDesign(getView().getDesignFile());
    for(String email : emailSelectorPresenter.getView().getItems()) {
      reportTemplate.addEmailNotification(email);
    }
    reportTemplate.setFormat("html");
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
    return parameterStr.split("=")[0].trim();
  }

  private String getParameterValue(String parameterStr) {
    return parameterStr.split("=")[1].trim();
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

  public void setReportTemplate(ReportTemplateDto reportTemplate) {
    getView().clear();
    emailSelectorPresenter.getView().clear();
    parametersSelectorPresenter.getView().clear();
    if(reportTemplate == null) {
      setDialogMode(Mode.CREATE);
    } else {
      setDialogMode(Mode.UPDATE);
      if (reportTemplate.hasProject()) project = reportTemplate.getProject();
      getView().setReportTemplate(reportTemplate);
      emailSelectorPresenter.getView().setItems(JsArrays.toIterable(reportTemplate.getEmailNotificationArray()));
      parametersSelectorPresenter.getView().setItems(Iterables
          .transform(JsArrays.toIterable(reportTemplate.getParametersArray()), new Function<ParameterDto, String>() {

            @Override
            public String apply(ParameterDto input) {
              return input.getKey() + "=" + input.getValue();
            }
          }));
    }

  }
}
