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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerUiHandlers;
import org.obiba.opal.web.gwt.app.client.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class FileExplorerView extends ViewWithUiHandlers<FileExplorerUiHandlers> implements Display {

  interface Binder extends UiBinder<Widget, FileExplorerView> {}

  @UiField
  Panel filePathPanel;

  @UiField
  Panel filePlacesPanel;

  @UiField
  Panel folderDetailsPanel;

  @UiField
  Button uploadFile;

  @UiField
  Button remove;

  @UiField
  Button download;

  @UiField
  Button unzip;

  @UiField
  Button copy;

  @UiField
  Button cut;

  @UiField
  Button paste;

  @UiField
  Button addFolder;

  @UiField
  Button rename;

  @Inject
  public FileExplorerView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    HasWidgets panel;
    switch((SplitPaneWorkbenchPresenter.Slot) slot) {
      case TOP:
        panel = filePathPanel;
        break;
      case LEFT:
        panel = filePlacesPanel;
        break;
      default:
        panel = folderDetailsPanel;
    }
    panel.clear();
    if(content != null) {
      panel.add(content.asWidget());
    }
  }

  @Override
  public HasAuthorization getCreateFolderAuthorizer() {
    return new WidgetAuthorizer(addFolder);
  }

  @Override
  public HasAuthorization getFileUploadAuthorizer() {
    return new WidgetAuthorizer(uploadFile);
  }

  @Override
  public HasAuthorization getFileDownloadAuthorizer() {
    return new WidgetAuthorizer(false, download);
  }

  @Override
  public HasAuthorization getFileUnzipAuthorizer() {
    return new WidgetAuthorizer(false, unzip);
  }

  @Override
  public HasAuthorization getFileRenameAuthorizer() {
    return new WidgetAuthorizer(false, rename);
  }

  @Override
  public HasAuthorization getFileDeleteAuthorizer() {
    return new WidgetAuthorizer(false, remove);
  }

  @Override
  public HasAuthorization getFileCopyAuthorizer() {
    return new WidgetAuthorizer(false, copy);
  }

  @Override
  public HasAuthorization getFileCutAuthorizer() {
    return new WidgetAuthorizer(false, cut);
  }

  @Override
  public HasAuthorization getFilePasteAuthorizer() {
    return new WidgetAuthorizer(false, paste);
  }

  @Override
  public void showFilesInClipboard(List<FileDto> filesClipboard) {
    // TODO translate
    paste.setTitle(filesClipboard == null || filesClipboard.isEmpty() ? "Paste" : "Paste " + filesClipboard.size() + " files");
  }

  @UiHandler("addFolder")
  void onAddFolder(ClickEvent event) {
    getUiHandlers().onAddFolder();
  }

  @UiHandler("uploadFile")
  void onUploadFile(ClickEvent event) {
    getUiHandlers().onUploadFile();
  }

  @UiHandler("download")
  void onDownload(ClickEvent event) {
    getUiHandlers().onDownload();
  }

  @UiHandler("unzip")
  void onUnzip(ClickEvent event) {
    getUiHandlers().onUnzip();
  }

  @UiHandler("rename")
  void onRename(ClickEvent event) {
    getUiHandlers().onRename();
  }

  @UiHandler("remove")
  void onDelete(ClickEvent event) {
    getUiHandlers().onDelete();
  }

  @UiHandler("copy")
  void onCopy(ClickEvent event) {
    getUiHandlers().onCopy();

  }

  @UiHandler("cut")
  void onCut(ClickEvent event) {
    getUiHandlers().onCut();
  }

  @UiHandler("paste")
  void onPaste(ClickEvent event) {
    getUiHandlers().onPaste();
  }
}
