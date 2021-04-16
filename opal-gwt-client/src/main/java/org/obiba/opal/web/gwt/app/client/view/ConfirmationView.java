/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.ConfirmationPresenter.Display;
import org.obiba.opal.web.gwt.app.client.presenter.ConfirmationUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;

/**
 *
 */
public class ConfirmationView extends ViewWithUiHandlers<ConfirmationUiHandlers> implements Display {

  interface Binder extends UiBinder<Widget, ConfirmationView> {}

  //
  // Constants
  //

  //
  // Instance Variables
  //

  @UiField
  Modal dialogBox;

  @UiField
  Paragraph message;

  @UiField
  Button yesButton;

  @UiField
  Button noButton;

  //
  // Constructors
  //
  @Inject
  public ConfirmationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // ConfirmationPresenter.Display Methods
  //

  @Override
  public void showDialog() {
    noButton.setEnabled(true);
    yesButton.setEnabled(true);
    dialogBox.setBusy(false);
    dialogBox.show();
  }

  @Override
  public void hideDialog() {
    dialogBox.hide();
  }

  @Override
  public void disableDialog() {
    noButton.setEnabled(false);
    yesButton.setEnabled(false);
    dialogBox.setBusy(true);
  }

  @Override
  public void setConfirmationTitle(String title) {
    dialogBox.setTitle(title);
  }

  @Override
  public void setConfirmationMessage(String message) {
    this.message.getElement().setInnerHTML(message);
  }

  @UiHandler("yesButton")
  public void onYes(ClickEvent event) {
    getUiHandlers().onYes();
  }

  @UiHandler("noButton")
  public void onNo(ClickEvent event) {
    getUiHandlers().onNo();
  }

}