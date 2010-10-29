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

import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateUpdateDialogPresenter.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ReportTemplateUpdateDialogView extends Composite implements Display {

  @UiTemplate("ReportTemplateUpdateDialogView.ui.xml")
  interface ReportTemplateUpdateDialogUiBinder extends UiBinder<DialogBox, ReportTemplateUpdateDialogView> {
  }

  private static ReportTemplateUpdateDialogUiBinder uiBinder = GWT.create(ReportTemplateUpdateDialogUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  Button updateReportTemplateButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox reportTemplateName;

  @UiField
  TextBox designFile;

  @UiField
  TextBox format;

  @UiField
  TextBox schedule;

  // @UiField
  // Label dialogTitle;

  public ReportTemplateUpdateDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    dialog.setGlassEnabled(false);
    dialog.hide();
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void showDialog() {
    dialog.center();
    dialog.show();
    reportTemplateName.setFocus(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
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
  public void setDialogTitle(String title) {
    // dialogTitle.setText(title);
  }

  @Override
  public String getName() {
    return reportTemplateName.getText();
  }

  @Override
  public String getDesign() {
    return designFile.getText();
  }

  @Override
  public String getFormat() {
    return designFile.getText();
  }

  @Override
  public String getShedule() {
    return designFile.getText();
  }

}
