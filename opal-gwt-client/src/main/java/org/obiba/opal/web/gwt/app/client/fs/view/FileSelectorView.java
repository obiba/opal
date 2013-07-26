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

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class FileSelectorView extends ModalPopupViewWithUiHandlers<FileSelectorUiHandlers> implements Display {

  @UiTemplate("FileSelectorView.ui.xml")
  interface FileSelectorViewUiBinder extends UiBinder<Widget, FileSelectorView> {}

  private static final int MAX_COLUMN_SIZE = 12;

  private static final String DIALOG_HEIGHT = "38.5em";

  private static final String DIALOG_SHORT_HEIGHT = "36em";

  private static final String DIALOG_WIDTH = "60em";

  private static final FileSelectorViewUiBinder uiBinder = GWT.create(FileSelectorViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal dialog;

  @UiField
  Panel content;

  @UiField
  HTMLPanel namePanel;

  @UiField
  TextBox newFileName;

  @UiField
  ScrollPanel fileSystemTreePanel;

  @UiField
  ScrollPanel folderDetailsPanel;

  @UiField
  Panel createFolderPanel;

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
    uiBinder.createAndBindUi(this);

    content.setHeight(DIALOG_HEIGHT);
    content.setWidth(DIALOG_WIDTH);

    dialog.setTitle(translations.fileSelectorTitle());
    dialog.setHeight(DIALOG_HEIGHT);
    dialog.setWidth(DIALOG_WIDTH);
  }

  private void updateHeight(String height) {
    dialog.setHeight(height);
    content.setHeight(height);
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    HasWidgets panel = slot == FileSelectorPresenter.LEFT ? fileSystemTreePanel : folderDetailsPanel;
    panel.clear();
    if(content != null) {
      panel.add(content.asWidget());
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

  @UiHandler("selectButton")
  public void onSelect(ClickEvent event) {
    getUiHandlers().selectFolder();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @UiHandler("createFolderButton")
  public void onCreate(ClickEvent event) {
    getUiHandlers().createFolder();
  }

  @UiHandler("uploadButton")
  public void onUpload(ClickEvent event) {
    getUiHandlers().uploadFile();
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

}