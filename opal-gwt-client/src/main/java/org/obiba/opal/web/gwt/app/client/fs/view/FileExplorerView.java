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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class FileExplorerView extends Composite implements Display {

  @UiTemplate("FileExplorerView.ui.xml")
  interface FileExplorerUiBinder extends UiBinder<DockLayoutPanel, FileExplorerView> {
  }

  @UiField
  ScrollPanel fileSystemTreePanel;

  @UiField
  ScrollPanel folderDetailsPanel;

  @UiField
  Button fileUploadButton;

  private static FileExplorerUiBinder uiBinder = GWT.create(FileExplorerUiBinder.class);

  public FileExplorerView() {
    initWidget(uiBinder.createAndBindUi(this));
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

}
