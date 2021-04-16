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

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class FileSelectorView extends ModalPopupViewWithUiHandlers<FileSelectorUiHandlers> implements Display {

  interface Binder extends UiBinder<Widget, FileSelectorView> {}

  @UiField
  Modal dialog;

  @UiField
  Panel content;

  @UiField
  Panel filePathPanel;

  @UiField
  Panel filePlacesPanel;

  @UiField
  Panel folderDetailsPanel;

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
  public FileSelectorView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));

    dialog.setTitle(translations.fileSelectorTitle());
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
  public void hideDialog() {
    hide();
  }

  @UiHandler("selectButton")
  public void onSelect(ClickEvent event) {
    getUiHandlers().onSelect();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().onCancel();
  }

  @UiHandler("createFolderButton")
  public void onCreate(ClickEvent event) {
    getUiHandlers().onCreateFolder();
  }

  @UiHandler("uploadButton")
  public void onUpload(ClickEvent event) {
    getUiHandlers().onUploadFile();
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
  public void clearErrors() {
    dialog.closeAlerts();
  }

  @Override
  public void showError(String errorMessage) {
    dialog.addAlert(errorMessage, AlertType.ERROR);
  }
}