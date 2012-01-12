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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
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
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class FileSelectorView extends PopupViewImpl implements Display {

  @UiTemplate("FileSelectorView.ui.xml")
  interface FileSelectorViewUiBinder extends UiBinder<DialogBox, FileSelectorView> {
  }

  private static final String DIALOG_HEIGHT = "38.5em";

  private static final String DIALOG_SHORT_HEIGHT = "36em";

  private static final String DIALOG_WIDTH = "60em";

  private static FileSelectorViewUiBinder uiBinder = GWT.create(FileSelectorViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final DialogBox dialog;

  @UiField
  DockLayoutPanel content;

  @UiField
  HTMLPanel namePanel;

  @UiField
  TextBox newFileName;

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
  Button uploadButton;

  @UiField
  Button selectButton;

  @UiField
  Button cancelButton;

  @Inject
  public FileSelectorView(EventBus eventBus) {
    super(eventBus);
    dialog = uiBinder.createAndBindUi(this);

    content.setHeight(DIALOG_HEIGHT);
    content.setWidth(DIALOG_WIDTH);

    dialog.setText(translations.fileSelectorTitle());
    dialog.setHeight(DIALOG_HEIGHT);
    dialog.setWidth(DIALOG_WIDTH);
  }

  private void updateHeight(String height) {
    dialog.setHeight(height);
    content.setHeight(height);
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    HasWidgets panel;
    if(slot == FileSelectorPresenter.LEFT) {
      panel = this.fileSystemTreePanel;
    } else {
      panel = this.folderDetailsPanel;
    }
    panel.clear();
    if(content != null) {
      panel.add(content);
    }
  }

  @Override
  public void hideDialog() {
    hide();
  }

  @Override
  public void setNewFilePanelVisible(boolean visible) {
    namePanel.setVisible(visible);
    updateHeight(visible ? DIALOG_HEIGHT : DIALOG_SHORT_HEIGHT);
  }

  @Override
  public void setNewFolderPanelVisible(boolean visible) {
    createFolderPanel.setVisible(visible);
  }

  @Override
  public void setDisplaysUploadFile(boolean visible) {
    uploadButton.setVisible(visible);
  }

  @Override
  public HandlerRegistration addSelectButtonHandler(ClickHandler handler) {
    return selectButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCancelButtonHandler(ClickHandler handler) {
    return cancelButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCreateFolderButtonHandler(ClickHandler handler) {
    return createFolderButton.addClickHandler(handler);
  }

  @Override
  public String getNewFileName() {
    return newFileName.getText();
  }

  @Override
  public void clearNewFileName() {
    newFileName.setText("");
  }

  @Override
  public HasText getCreateFolderName() {
    return createFolderName;
  }

  @Override
  public void clearNewFolderName() {
    createFolderName.setText("");
  }

  @Override
  public Widget asWidget() {
    return dialog;
  }

  @Override
  public HandlerRegistration addUploadButtonHandler(ClickHandler handler) {
    return uploadButton.addClickHandler(handler);
  }
}