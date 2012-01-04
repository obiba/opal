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
import org.obiba.opal.web.gwt.app.client.workbench.view.WorkbenchLayout;
import org.obiba.opal.web.gwt.rest.client.authorization.FocusWidgetAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class FileExplorerView extends Composite implements Display {

  private static FileExplorerUiBinder uiBinder = GWT.create(FileExplorerUiBinder.class);

  @UiTemplate("FileExplorerView.ui.xml")
  interface FileExplorerUiBinder extends UiBinder<WorkbenchLayout, FileExplorerView> {
  }

  @UiField
  ScrollPanel fileSystemTreePanel;

  @UiField
  ScrollPanel folderDetailsPanel;

  @UiField
  Button fileUploadButton;

  @UiField
  Button fileDeleteButton;

  @UiField
  Button fileDownloadButton;

  @UiField
  Button createFolderButton;

  public FileExplorerView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public Button getFileDeleteButton() {
    return fileDeleteButton;
  }

  public Button getFileDownloadButton() {
    return fileDownloadButton;
  }

  public Button getCreateFolderButton() {
    return createFolderButton;
  }

  @Override
  public ScrollPanel getFileSystemTree() {
    return fileSystemTreePanel;
  }

  @Override
  public ScrollPanel getFolderDetailsPanel() {
    return folderDetailsPanel;
  }

  @Override
  public HasClickHandlers getFileUploadButton() {
    return fileUploadButton;
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void addToSlot(Object slot, Widget content) {
  }

  @Override
  public void removeFromSlot(Object slot, Widget content) {
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
  }

  @Override
  public void setEnabledFileDeleteButton(boolean enabled) {
    fileDeleteButton.setEnabled(enabled);
  }

  @Override
  public HasAuthorization getCreateFolderAuthorizer() {
    return new FocusWidgetAuthorizer(createFolderButton);
  }

  @Override
  public HasAuthorization getFileUploadAuthorizer() {
    return new FocusWidgetAuthorizer(fileUploadButton);
  }

  @Override
  public HasAuthorization getFileDownloadAuthorizer() {
    return new FocusWidgetAuthorizer(fileDownloadButton);
  }

  @Override
  public HasAuthorization getFileDeleteAuthorizer() {
    return new FocusWidgetAuthorizer(fileDeleteButton);
  }

}
