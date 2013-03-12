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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

public class CreateFolderDialogView extends PopupViewImpl implements Display {

  @UiTemplate("CreateFolderDialogView.ui.xml")
  interface CreateFolderDialogUiBinder extends UiBinder<DialogBox, CreateFolderDialogView> {}

  private static CreateFolderDialogUiBinder uiBinder = GWT.create(CreateFolderDialogUiBinder.class);

  private final Widget widget;

  @UiField
  DialogBox dialog;

  @UiField
  Button createFolderButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox folderToCreate;

  @Inject
  public CreateFolderDialogView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    uiBinder.createAndBindUi(this);
    dialog.setGlassEnabled(false);
    dialog.hide();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
  }

  @Override
  public void show() {
    folderToCreate.setFocus(true);
    super.show();
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

}
