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

import org.obiba.opal.web.gwt.app.client.fs.presenter.CreateFolderDialogPresenter.Display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CreateFolderDialogView extends Composite implements Display {

  @UiTemplate("CreateFolderDialogView.ui.xml")
  interface CreateFolderDialogUiBinder extends UiBinder<DialogBox, CreateFolderDialogView> {
  }

  private static CreateFolderDialogUiBinder uiBinder = GWT.create(CreateFolderDialogUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  Button createFolderButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox folderToCreate;

  @UiField
  Label errorMsg;

  public CreateFolderDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    dialog.setGlassEnabled(false);
    dialog.hide();
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
  public void showDialog() {
    dialog.center();
    dialog.show();
    folderToCreate.setFocus(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public Button getCancelButton() {
    return cancelButton;
  }

  @Override
  public Button getCreateFolderButton() {
    return createFolderButton;
  }

  @Override
  public HasText getFolderToCreate() {
    return folderToCreate;
  }

  @Override
  public HasText getErrorMsg() {
    return errorMsg;
  }

  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
  }

}
