/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.report.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ItemSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.report.ROptionsHelper;
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
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.ParameterDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_OK;

public class ReportTemplateEditModalPresenter extends ModalPresenterWidget<ReportTemplateEditModalPresenter.Display>
    implements ReportTemplateEditModalUiHandlers {

  private final FileSelectionPresenter fileSelectionPresenter;

  private final ItemSelectorPresenter emailSelectorPresenter;

  private final ItemSelectorPresenter parametersSelectorPresenter;

  private final Collection<FieldValidator> validators = new LinkedHashSet<>();

  private String project;

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  @Inject
  public ReportTemplateEditModalPresenter(Display display, EventBus eventBus,
      Provider<FileSelectionPresenter> fileSelectionPresenterProvider,
      Provider<ItemSelectorPresenter> itemSelectorPresenterProvider) {
    super(eventBus, display);
    fileSelectionPresenter = fileSelectionPresenterProvider.get();
    emailSelectorPresenter = itemSelectorPresenterProvider.get();
    parametersSelectorPresenter = itemSelectorPresenterProvider.get();
    Map<String, List<String>> suggestions = Maps.newLinkedHashMap();
    suggestions.put("opal.username", new ArrayList<String>());
    suggestions.put("opal.password", new ArrayList<String>());
    suggestions.put("opal.token", new ArrayList<String>());
    suggestions.put("opal.url", new ArrayList<String>());
    parametersSelectorPresenter.getView().setItemInputDisplay(new KeyValueItemInputView(suggestions) {
      @Override
      public String renderItem(String item) {
        String option = super.renderItem(item);
        String[] parts = option.split("=");
        return
            parts[0] + "=" + ROptionsHelper.renderROptionValue(parts[0], parts.length == 2 ? parts[1] : "");
      }
    });
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
    switch(dialogMode) {
      case CREATE:
        createReportTemplate();
        break;
      case UPDATE:
        updateReportTemplateInternal();
        break;
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
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.PROJECT_REPORT_TEMPLATES.create().build(reportTemplate.getProject())) //
          .withResourceBody(ReportTemplateDto.stringify(reportTemplate)) //
          .withCallback(new CreateOrUpdateReportTemplateCallBack(reportTemplate), SC_OK, SC_CREATED) //
          .post().send();
    }
  }

  private void updateReportTemplateInternal() {
    if(validReportTemplate()) {
      ReportTemplateDto reportTemplate = getReportTemplateDto();
      ResponseCodeCallback callbackHandler = new CreateOrUpdateReportTemplateCallBack(reportTemplate);
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.PROJECT_REPORT_TEMPLATE.create().build(reportTemplate.getProject(), reportTemplate.getName())) //
          .withResourceBody(ReportTemplateDto.stringify(reportTemplate)) //
          .withCallback(callbackHandler, SC_OK, SC_CREATED) //
          .put().send();
    }
  }

  private boolean validReportTemplate() {
    getView().clearErrors();

    List<Display.FormField> validatorIds = new ArrayList<>();
    List<String> messages = new ArrayList<>();
    String message;
    for(FieldValidator validator : validators) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
        validatorIds.add(Display.FormField.valueOf(validator.getId()));
      }
    }

    if(!messages.isEmpty()) {
      getView().showErrors(messages);
      getView().setErrors(messages, validatorIds);
      return false;
    }
    return true;
  }

  private ReportTemplateDto getReportTemplateDto() {
    ReportTemplateDto dto = ReportTemplateDto.create();
    dto.setName(getView().getName().getText());
    dto.setProject(project);
    String schedule = getView().getSchedule().getText();
    if(schedule != null && !schedule.trim().isEmpty()) {
      dto.setCron(getView().getSchedule().getText());
    }
    dto.setDesign(getView().getDesignFile());
    for(String email : emailSelectorPresenter.getView().getItems()) {
      dto.addEmailNotification(email);
    }
    String format = "html";
    if (getView().getFormat().getText() != null && !getView().getFormat().getText().trim().isEmpty()) {
      format = getView().getFormat().getText();
    }
    dto.setFormat(format);
    ParameterDto parameterDto;
    for(String parameterStr : parametersSelectorPresenter.getView().getItems()) {
      parameterDto = ParameterDto.create();
      parameterDto.setValue(getParameterValue(parameterStr));
      parameterDto.setKey(getParameterKey(parameterStr));
      dto.addParameters(parameterDto);
    }
    return dto;
  }

  private String getParameterKey(String parameterStr) {
    return parameterStr.split("=")[0].trim();
  }

  private String getParameterValue(String parameterStr) {
    String[] parts = parameterStr.split("=");
    return parts.length == 2 ? parts[1].trim() : "";
  }

  private class CreateOrUpdateReportTemplateCallBack implements ResponseCodeCallback {

    private final ReportTemplateDto reportTemplate;

    private CreateOrUpdateReportTemplateCallBack(ReportTemplateDto reportTemplate) {
      this.reportTemplate = reportTemplate;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(response.getStatusCode() == SC_OK) {
        getEventBus().fireEvent(new ReportTemplateUpdatedEvent(reportTemplate));
      } else if(response.getStatusCode() == SC_CREATED) {
        getEventBus().fireEvent(new ReportTemplateCreatedEvent(reportTemplate));
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
      project = reportTemplate.getProject();
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

  public interface Display extends PopupView, HasUiHandlers<ReportTemplateEditModalUiHandlers> {

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

    HasText getFormat();

    HasText getSchedule();

    HasValue<Boolean> isScheduled();

    void setDesignFileWidgetDisplay(FileSelectionPresenter.Display display);

    void setEnabledReportTemplateName(boolean enabled);

    void setErrors(List<String> messages, List<FormField> ids);

    void clearErrors();
  }
}
