/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.view;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FileUpload;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import elemental.html.File;
import elemental.html.FileList;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadModalPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import java.util.List;

public class FileUploadModalView extends ModalPopupViewWithUiHandlers<FileUploadModalUiHandlers> implements Display {

  interface Binder extends UiBinder<Widget, FileUploadModalView> {
  }

  @UiField
  Modal dialog;

  @UiField
  Button uploadButton;

  @UiField
  Button cancelButton;

  @UiField
  FileUpload fileToUpload;

  @UiField
  InlineLabel remoteFolderName;

  @UiField
  Form form;

  @UiField
  Panel inputFieldPanel;

  @UiField
  Panel uploadProgressPanel;

  @Inject
  public FileUploadModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.uploadFileModalTitle());
    fileToUpload.getElement().setAttribute("multiple", "multiple");
  }

  @Override
  public void onShow() {
    setUploading(false);
    // Clears the fileUpload field as there's no way to do this on the widget itself.
    form.reset();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    hideDialog();
  }

  @UiHandler("uploadButton")
  public void onUploadButton(ClickEvent event) {
    List<String> filenames = Lists.newArrayList();
    FileList files = (FileList) fileToUpload.getElement().getPropertyObject("files");
    if (files.getLength() > 0) {
      for (int i = 0; i < files.getLength(); i++) {
        File file = files.item(i);
        filenames.add(file.getName());
      }
    }
    getUiHandlers().uploadFiles(filenames);
  }

  @UiHandler("form")
  public void onSubmitCompleted(Form.SubmitCompleteEvent event) {
    getUiHandlers().submit();
  }

  @Override
  public void submit(String url) {
    setUploading(true);
    form.setAction(url);
    form.submit();
  }

  @Override
  public void setRemoteFolderName(String folderName) {
    remoteFolderName.setText(folderName);
  }

  private void setUploading(boolean isUploading) {
    inputFieldPanel.setVisible(!isUploading);
    cancelButton.setEnabled(!isUploading);
    uploadButton.setEnabled(!isUploading);
    uploadProgressPanel.setVisible(isUploading);
    dialog.setBusy(isUploading);
  }

}
