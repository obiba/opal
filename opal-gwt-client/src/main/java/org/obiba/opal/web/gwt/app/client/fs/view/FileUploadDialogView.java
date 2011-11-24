/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.view;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadDialogPresenter.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class FileUploadDialogView extends Composite implements Display {

  @UiTemplate("FileUploadDialogView.ui.xml")
  interface FileUploadDialogUiBinder extends UiBinder<DialogBox, FileUploadDialogView> {
  }

  private static FileUploadDialogUiBinder uiBinder = GWT.create(FileUploadDialogUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  Button uploadButton;

  @UiField
  Button cancelButton;

  @UiField
  FileUpload fileToUpload;

  @UiField
  Label remoteFolderName;

  @UiField
  FormPanel form;

  @UiField
  Panel inputFieldPanel;

  @UiField
  Image uploadingText;

  public FileUploadDialogView() {
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
    setUploading(false);
    // Clears the fileUpload field as there's no way to do this on the widget itself.
    form.reset();
    dialog.center();
    dialog.show();
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
  public Button getUploadButton() {
    return uploadButton;
  }

  @Override
  public String getFilename() {
    return fileToUpload.getFilename();
  }

  @Override
  public HandlerRegistration addSubmitCompleteHandler(SubmitCompleteHandler handler) {
    return this.form.addSubmitCompleteHandler(handler);
  }

  @Override
  public void submit(String url) {
    setUploading(true);
    this.form.setAction(url);
    this.form.submit();
  }

  @Override
  public HasText getRemoteFolderName() {
    return remoteFolderName;
  }

  private void setUploading(boolean isUploading) {
    inputFieldPanel.setVisible(isUploading == false);
    cancelButton.setEnabled(isUploading == false);
    uploadButton.setEnabled(isUploading == false);

    uploadingText.setVisible(isUploading);
  }

}
