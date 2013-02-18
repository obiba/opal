/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.unit.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.unit.presenter.GenerateIdentifiersDialogPresenter.Display;
import org.obiba.opal.web.gwt.app.client.workbench.view.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class GenerateIdentifiersDialogView extends PopupViewImpl implements Display {

  @UiTemplate("GenerateIdentifiersDialogView.ui.xml")
  interface GenerateIdentifiersDialogUiBinder extends UiBinder<DialogBox, GenerateIdentifiersDialogView> {
  }

  private static final GenerateIdentifiersDialogUiBinder uiBinder = GWT
      .create(GenerateIdentifiersDialogUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);
  private static final int MIN_IDENTIFIER_SIZE = 5;

  @UiField
  DialogBox dialog;

  @UiField
  Label confirmationMessage;

  @UiField
  NumericTextBox size;

  @UiField
  TextBox prefix;

  @UiField
  CheckBox allowZeros;

  @UiField
  Button generateButton;

  @UiField
  Button cancelButton;

  @UiField
  DockLayoutPanel content;

  @UiField
  ResizeHandle resizeHandle;

  //
  // Constructors
  //
  @Inject
  public GenerateIdentifiersDialogView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    initializeTexts();
    resizeHandle.makeResizable(content);
    clear();
    dialog.hide();
  }

  //
  // ConfirmationPresenter.Display Methods
  //
  @Override
  public void show() {
    GWT.log("***** GenerateIdentifiersDialogView.show()");
    size.setFocus(true);
    super.show();
  }


  @Override
  public Widget asWidget() {
    return dialog;
  }

  public void hideDialog() {
    clear();
    hide();
  }

  @Override
  public HasClickHandlers getGenerateIdentifiersButton() {
    return generateButton;
  }

  @Override
  public HasClickHandlers getCancelButton() {
    return cancelButton;
  }

  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
  }

  @Override
  public Number getSize() {
    return size.getNumberValue();
  }

  @Override
  public String getPrefix() {
    return prefix.getText();
  }

  @Override
  public boolean getAllowZeros() {
    return allowZeros.getValue();
  }

  @Override
  public void clear() {
    allowZeros.setValue(false);
    prefix.setText("");
    size.setValue(Integer.toString(MIN_IDENTIFIER_SIZE), false);
  }

  //
  // Private Methods
  //

  private void initializeTexts() {
    dialog.setText(translations.generateUnitIdentifiers());
    confirmationMessage.setText(translations.confirmGenerateFunctionalUnitIdentifiers());
    dialog.setModal(false);
    generateButton.setText(translations.generateIdentifiersButton());
  }

}