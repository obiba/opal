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

import org.obiba.opal.web.gwt.app.client.fs.presenter.CreateFolderModalPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.CreateFolderUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class CreateFolderModalView extends ModalPopupViewWithUiHandlers<CreateFolderUiHandlers> implements Display {

  interface Binder extends UiBinder<Widget, CreateFolderModalView> {}

  @UiField
  Modal dialog;

  @UiField
  Button createFolderButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox folderToCreate;

  @Inject
  public CreateFolderModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.createFolderModalTitle());
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

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("createFolderButton")
  public void onCreateFolderButton(ClickEvent event) {
    getUiHandlers().createFolder(folderToCreate.getText());
  }

}
