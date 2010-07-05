/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class FileSelectorView extends DialogBox implements Display {
  //
  // Constants
  //

  private static final String DIALOG_HEIGHT = "30em";

  private static final String DIALOG_WIDTH = "50em";

  //
  // Static Variables
  //

  private static FileSelectorViewUiBinder uiBinder = GWT.create(FileSelectorViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  ScrollPanel fileSystemTreePanel;

  @UiField
  ScrollPanel folderDetailsPanel;

  //
  // Constructors
  //

  public FileSelectorView() {
    setText("File Selector");
    setHeight(DIALOG_HEIGHT);
    setWidth(DIALOG_WIDTH);

    DockLayoutPanel content = uiBinder.createAndBindUi(this);
    content.setHeight(DIALOG_HEIGHT);
    content.setWidth(DIALOG_WIDTH);
    add(content);
  }

  //
  // FileSelectorPresenter.Display Methods
  //

  @Override
  public void showDialog() {
    center();
    show();
  }

  public void hideDialog() {
    hide();
  }

  public HasWidgets getFileSystemTreePanel() {
    return fileSystemTreePanel;
  }

  public HasWidgets getFolderDetailsPanel() {
    return folderDetailsPanel;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  public Widget asWidget() {
    return this;
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("FileSelectorView.ui.xml")
  interface FileSelectorViewUiBinder extends UiBinder<DockLayoutPanel, FileSelectorView> {
  }
}