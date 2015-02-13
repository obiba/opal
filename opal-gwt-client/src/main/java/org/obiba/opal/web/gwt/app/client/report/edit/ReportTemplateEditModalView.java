/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.edit;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.report.edit.ReportTemplateEditModalPresenter.Display;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.github.gwtbootstrap.client.ui.event.HiddenHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ReportTemplateEditModalView extends ModalPopupViewWithUiHandlers<ReportTemplateEditModalUiHandlers>
implements Display {

  interface Binder extends UiBinder<Widget, ReportTemplateEditModalView> {}

  @UiField
  Modal dialog;

  @UiField
  Button updateReportTemplateButton;

  @UiField
  Button cancelButton;

  @UiField
  ControlGroup labelName;

  @UiField
  ControlGroup labelTemplateFile;

  @UiField
  ControlGroup labelSchedule;

  @UiField
  TextBox reportTemplateName;

  @UiField
  TextBox schedule;

  @UiField
  SimplePanel designFilePanel;

  @UiField
  SimplePanel notificationEmailsPanel;

  @UiField
  SimplePanel reportParametersPanel;

  @UiField
  RadioButton runManuallyRadio;

  @UiField
  RadioButton scheduleRadio;

  @UiField
  ControlGroup emailsGroup;

  private FileSelectionPresenter.Display fileSelection;

  private final Translations translations;

  @Inject
  public ReportTemplateEditModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    dialog.addHiddenHandler(new DialogHiddenHandler());
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    Display.Slots s = (Display.Slots) slot;
    switch(s) {
      case EMAIL:
        notificationEmailsPanel.add(content);
        break;
      case REPORT_PARAMS:
        reportParametersPanel.add(content);
        break;
    }
  }

  @Override
  public void removeFromSlot(Object slot, IsWidget content) {
    Display.Slots s = (Display.Slots) slot;
    switch(s) {
      case EMAIL:
        notificationEmailsPanel.remove(content);
        break;
      case REPORT_PARAMS:
        reportParametersPanel.remove(content);
        break;
    }
  }

  @Override
  public void onShow() {
    reportTemplateName.setFocus(true);
  }

  @Override
  public void showErrors(List<String> messages) {
    dialog.closeAlerts();
    for(String msg : messages) {
      dialog.addAlert(translations.userMessageMap().get(msg), AlertType.ERROR);
    }
  }

  @Override
  public void hideDialog() {
    hide();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    getUiHandlers().onDialogHide();
  }

  @UiHandler("updateReportTemplateButton")
  public void getUpdateReportTemplateButton(ClickEvent event) {
    getUiHandlers().updateReportTemplate();
  }

  @UiHandler("schedule")
  public void onSchedule(ClickEvent event) {
    getUiHandlers().enableSchedule();
  }

  @UiHandler("runManuallyRadio")
  public void onRunManually(ClickEvent event) {
    getUiHandlers().disableSchedule();
  }

  @Override
  public HasText getName() {
    return reportTemplateName;
  }

  @Override
  public String getDesignFile() {
    return fileSelection.getFile();
  }

  @Override
  public HasText getSchedule() {
    return schedule;
  }

  @Override
  public void clear() {
    setDesignFile("");
    setName("");
    setSchedule("");
  }

  @Override
  public void setReportTemplate(ReportTemplateDto reportTemplate) {
    setDesignFile(reportTemplate.getDesign());
    setName(reportTemplate.getName());
    setSchedule(reportTemplate.getCron());
  }

  @Override
  public void setDesignFileWidgetDisplay(FileSelectionPresenter.Display display) {
    designFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setEnabled(true);
    fileSelection.setFieldWidth("20em");
  }

  private void setName(String name) {
    reportTemplateName.setText(name != null ? name : "");
  }

  private void setDesignFile(String designFile) {
    fileSelection.setFile(designFile != null ? designFile : "");
  }

  private void setSchedule(String schedule) {
    this.schedule.setText(schedule);
    if("".equals(schedule)) {
      scheduleRadio.setValue(false);
      runManuallyRadio.setValue(true);
    } else {
      scheduleRadio.setValue(true);
      runManuallyRadio.setValue(false);
    }
  }

  @Override
  public void setEnabledReportTemplateName(boolean enabled) {
    reportTemplateName.setEnabled(enabled);
    dialog
        .setTitle(enabled ? translations.addReportTemplateDialogTitle() : translations.editReportTemplateDialogTitle());
  }

  @Override
  public HasValue<Boolean> isScheduled() {
    return scheduleRadio;
  }

  @Override
  public void setErrors(List<String> message, List<FormField> ids) {

    for(FormField id : ids) {
      switch(id) {
        case NAME:
          labelName.setType(ControlGroupType.ERROR);
          break;
        case TEMPLATE_FILE:
          labelTemplateFile.setType(ControlGroupType.ERROR);
          break;
        case CRON_EXPRESSION:
          labelSchedule.setType(ControlGroupType.ERROR);
          break;
        case EMAILS:
          emailsGroup.setType(ControlGroupType.ERROR);
          break;
        default:
          break;
      }
    }
  }

  @Override
  public void clearErrors() {
    labelName.setType(ControlGroupType.NONE);
    labelTemplateFile.setType(ControlGroupType.NONE);
    labelSchedule.setType(ControlGroupType.NONE);
  }

  private class DialogHiddenHandler implements HiddenHandler {
    @Override
    public void onHidden(HiddenEvent hiddenEvent) {
      getUiHandlers().onDialogHidden();
      clearErrors();
      dialog.clearAlert();
    }
  }

}
