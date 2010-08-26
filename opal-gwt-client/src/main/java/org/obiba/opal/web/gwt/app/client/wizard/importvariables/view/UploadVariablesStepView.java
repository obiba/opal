/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.view;

import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.UploadVariablesStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;

public class UploadVariablesStepView extends Composite implements UploadVariablesStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  Button nextButton;

  @UiField
  FormPanel fileUploadForm;

  @UiField
  FileUpload fileToUpload;

  @UiField
  Button downloadExcelTemplateButton;

  //
  // Constructors
  //

  public UploadVariablesStepView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // UploadVariablesStepPresenter.Display Methods
  //

  public HandlerRegistration addNextClickHandler(ClickHandler handler) {
    return nextButton.addClickHandler(handler);
  }

  public HandlerRegistration addDownloadExcelTemplateClickHandler(ClickHandler handler) {
    return downloadExcelTemplateButton.addClickHandler(handler);
  }

  public HandlerRegistration addUploadCompleteHandler(FormPanel.SubmitCompleteHandler handler) {
    return fileUploadForm.addSubmitCompleteHandler(handler);
  }

  public void clear() {
    fileToUpload.removeFromParent();
    fileToUpload = new FileUpload();
    fileToUpload.setName("fileToUpload");

    fileUploadForm.add(fileToUpload);
  }

  public String getVariablesFilename() {
    return fileToUpload.getFilename();
  }

  public void uploadVariablesFile() {
    if(getVariablesFilename() != null && !getVariablesFilename().isEmpty()) {
      // Note: Not sure why the double slash (//) is required in the URI. Looks like a bug in
      // FilesResource.
      String uri = "/ws/files//tmp/" + getVariablesFilename();
      fileUploadForm.setAction(uri);
      fileUploadForm.submit();
    }
  }

  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("UploadVariablesStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, UploadVariablesStepView> {
  }
}
