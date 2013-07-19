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

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerUiHandlers;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.NavLink;
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
  Panel fileSystemTreePanel;

  @UiField
  Panel folderDetailsPanel;

  @UiField
  NavLink uploadFile;

  @UiField
  Button remove;

  @UiField
  Button download;

  @UiField
  NavLink addFolder;

  @UiField
  Panel breadcrumbs;

  @Inject
  public FileExplorerView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    HasWidgets panel = slot == SplitPaneWorkbenchPresenter.Slot.LEFT ? fileSystemTreePanel : folderDetailsPanel;
    panel.clear();
    if(content != null) {
      panel.add(content.asWidget());
    }
  }

  @Override
  public void setEnabledFileDeleteButton(boolean enabled) {
    remove.setEnabled(enabled);
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
    return new WidgetAuthorizer(download);
  }

  @Override
  public HasAuthorization getFileDeleteAuthorizer() {
    return new WidgetAuthorizer(remove);
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
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
