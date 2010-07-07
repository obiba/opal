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

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
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

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  HTMLPanel namePanel;

  @UiField
  Label nameLabel;

  @UiField
  TextBox name;

  @UiField
  ScrollPanel fileSystemTreePanel;

  @UiField
  ScrollPanel folderDetailsPanel;

  @UiField
  HTMLPanel createFolderPanel;

  @UiField
  TextBox createFolderName;

  @UiField
  Button createFolderButton;

  @UiField
  Button selectButton;

  @UiField
  Button cancelButton;

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

    nameLabel.setText(translations.nameLabel() + ":");
    createFolderButton.setText("Create Folder");
    selectButton.setText("Select");
    cancelButton.setText("Cancel");

    addCancelHandler();
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

  public void setTreeDisplay(FileSystemTreePresenter.Display treeDisplay) {
    getFileSystemTreePanel().clear();
    getFileSystemTreePanel().add(treeDisplay.asWidget());
  }

  public void setDetailsDisplay(FolderDetailsPresenter.Display detailsDisplay) {
    getFolderDetailsPanel().clear();
    getFolderDetailsPanel().add(detailsDisplay.asWidget());
  }

  public void setNewFilePanelVisible(boolean visible) {
    namePanel.setVisible(visible);
  }

  public void setNewFolderPanelVisible(boolean visible) {
    createFolderPanel.setVisible(visible);
  }

  public void clearNewFolderName() {
    createFolderName.setText("");
  }

  public HasWidgets getFileSystemTreePanel() {
    return fileSystemTreePanel;
  }

  public HasWidgets getFolderDetailsPanel() {
    return folderDetailsPanel;
  }

  public HandlerRegistration addSelectButtonHandler(ClickHandler handler) {
    return selectButton.addClickHandler(handler);
  }

  public HandlerRegistration addCreateFolderButtonHandler(ClickHandler handler) {
    return createFolderButton.addClickHandler(handler);
  }

  public HasText getCreateFolderName() {
    return createFolderName;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  public Widget asWidget() {
    return this;
  }

  //
  // Methods
  //

  private void addCancelHandler() {
    cancelButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        hideDialog();
      }
    });
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("FileSelectorView.ui.xml")
  interface FileSelectorViewUiBinder extends UiBinder<DockLayoutPanel, FileSelectorView> {
  }
}