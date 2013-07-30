/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateDialogPresenter.Display;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateDialogUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.github.gwtbootstrap.client.ui.event.HiddenHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ReportTemplateUpdateDialogView extends ModalPopupViewWithUiHandlers<ReportTemplateUpdateDialogUiHandlers>
    implements Display {


  interface ReportTemplateUpdateDialogUiBinder extends UiBinder<Widget, ReportTemplateUpdateDialogView> {}

  private static final ReportTemplateUpdateDialogUiBinder uiBinder = GWT
      .create(ReportTemplateUpdateDialogUiBinder.class);

  @UiField
  Modal dialog;

  @UiField
  Button updateReportTemplateButton;

  @UiField
  Button cancelButton;

  @UiField
  ControlGroup labelName;

  @UiField
  ControlGroup labelTempleFile;

  @UiField
  ControlGroup labelSchedule;

  @UiField
  TextBox reportTemplateName;

  @UiField
  ListBox format;

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
  Anchor cronLink;


  @UiField
  Panel alertPlace;


  private FileSelectionPresenter.Display fileSelection;

  private static final Translations translations = GWT.create(Translations.class);

  @Inject
  public ReportTemplateUpdateDialogView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    cronLink.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        Window.open("http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html", "_blank", null);
      }
    });

    dialog.setTitle(translations.reportTemplateDialogTitle());
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    Display.Slots s = (Display.Slots) slot;
    switch(s) {
      case ERROR:
        alertPlace.add(content);
        break;
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
      case ERROR:
        alertPlace.remove(content);
        break;
      case EMAIL:
        notificationEmailsPanel.remove(content);
        break;
      case REPORT_PARAMS:
        reportParametersPanel.remove(content);
        break;
    }
  }

  @Override
  public Widget asWidget() {
    return dialog;
  }

  @Override
  public void show() {
    reportTemplateName.setFocus(true);
    super.show();
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
  public String getFormat() {
    return format.getItemText(format.getSelectedIndex());
  }

  @Override
  public HasText getShedule() {
    return schedule;
  }

  @Override
  public void setDesignFileWidgetDisplay(FileSelectionPresenter.Display display) {
    designFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setEnabled(true);
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public void setName(String name) {
    reportTemplateName.setText(name != null ? name : "");
  }

  @Override
  public void setDesignFile(String designFile) {
    fileSelection.setFile(designFile != null ? designFile : "");
  }

  @Override
  public void setFormat(String format) {
    int itemCount = this.format.getItemCount();
    String item;
    for(int i = 0; i < itemCount; i++) {
      item = this.format.getItemText(i);
      if(item.equals(format)) {
        this.format.setSelectedIndex(i);
        break;
      }
    }
  }

  @Override
  public void setSchedule(String schedule) {
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

  }

  @Override
  public HasValue<Boolean> isScheduled() {
    return scheduleRadio;
  }

  @Override
  public void setErrors(List<String> message, List<FormField> ids) {

    for (FormField id : ids) {
    switch(id) {
      case NAME:
        labelName.setType(ControlGroupType.ERROR);
        break;

      case TEMPLE_FILE:
        labelTempleFile.setType(ControlGroupType.ERROR);
        break;

      case CRON_EXPRESSION:
        labelSchedule.setType(ControlGroupType.ERROR);
        break;

      default:
        break;
    }
    }
  }

  @Override
  public void clearErrors() {
    labelName.setType(ControlGroupType.NONE);
    labelTempleFile.setType(ControlGroupType.NONE);
    labelSchedule.setType(ControlGroupType.NONE);
  }

}
