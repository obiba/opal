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

import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateDialogPresenter.Display;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ItemSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

public class ReportTemplateUpdateDialogView extends PopupViewImpl implements Display {

  @UiTemplate("ReportTemplateUpdateDialogView.ui.xml")
  interface ReportTemplateUpdateDialogUiBinder extends UiBinder<DialogBox, ReportTemplateUpdateDialogView> {
  }

  private static ReportTemplateUpdateDialogUiBinder uiBinder = GWT.create(ReportTemplateUpdateDialogUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel contentLayout;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  Button updateReportTemplateButton;

  @UiField
  Button cancelButton;

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

  private ItemSelectorPresenter.Display emailsSelector;

  private ItemSelectorPresenter.Display parametersSelector;

  private FileSelectionPresenter.Display fileSelection;

  @Inject
  public ReportTemplateUpdateDialogView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    resizeHandle.makeResizable(contentLayout);
    dialog.hide();
    cronLink.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        Window.open("http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html", "_blank", null);
      }
    });
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
    super.hide();
  }

  @Override
  public Button getCancelButton() {
    return cancelButton;
  }

  @Override
  public Button getUpdateReportTemplateButton() {
    return updateReportTemplateButton;
  }

  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
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
  public List<String> getNotificationEmails() {
    return emailsSelector.getItems();
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
  public void setNotificationEmailsWidgetDisplay(ItemSelectorPresenter.Display display) {
    notificationEmailsPanel.setWidget(display.asWidget());
    emailsSelector = display;
  }

  @Override
  public HandlerRegistration addEnableScheduleClickHandler(ClickHandler handler) {
    return schedule.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addDisableScheduleClickHandler(ClickHandler handler) {
    return runManuallyRadio.addClickHandler(handler);
  }

  @Override
  public void setReportParametersWidgetDisplay(ItemSelectorPresenter.Display display) {
    reportParametersPanel.setWidget(display.asWidget());
    parametersSelector = display;
  }

  @Override
  public List<String> getReportParameters() {
    return parametersSelector.getItems();
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
    if(!schedule.equals("")) {
      scheduleRadio.setValue(true);
      runManuallyRadio.setValue(false);
    } else {
      scheduleRadio.setValue(false);
      runManuallyRadio.setValue(true);
    }
  }

  @Override
  public void setNotificationEmails(List<String> emails) {
    emailsSelector.clear();
    for(String email : emails) {
      emailsSelector.addItem(email);
    }
  }

  @Override
  public void setReportParameters(List<String> params) {
    parametersSelector.clear();
    for(String param : params) {
      parametersSelector.addItem(param);
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

}
