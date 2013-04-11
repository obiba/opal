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
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WorkbenchLayout;
import org.obiba.opal.web.gwt.rest.client.authorization.FocusWidgetAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class FileExplorerView extends ViewImpl implements Display {

  private static final FileExplorerUiBinder uiBinder = GWT.create(FileExplorerUiBinder.class);

  @UiTemplate("FileExplorerView.ui.xml")
  interface FileExplorerUiBinder extends UiBinder<WorkbenchLayout, FileExplorerView> {}

  private final Widget widget;

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
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Button getFileDeleteButton() {
    return fileDeleteButton;
  }

  @Override
  public Button getFileDownloadButton() {
    return fileDownloadButton;
  }

  @Override
  public Button getCreateFolderButton() {
    return createFolderButton;
  }

  @Override
  public HasClickHandlers getFileUploadButton() {
    return fileUploadButton;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    HasWidgets panel = slot == SplitPaneWorkbenchPresenter.Slot.LEFT ? fileSystemTreePanel : folderDetailsPanel;
    panel.clear();
    if(content != null) {
      panel.add(content);
    }
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
