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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.unit.presenter.GenerateIdentifiersDialogPresenter.Display;
import org.obiba.opal.web.gwt.app.client.workbench.view.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
  interface GenerateIdentifiersDialogUiBinder extends UiBinder<DialogBox, GenerateIdentifiersDialogView> {}

  //
  // Constants
  //

  private static final int MIN_IDENTIFIER_SIZE = 5;

  private static final int MAX_IDENTIFIER_SIZE = 20;

  private static final GenerateIdentifiersDialogUiBinder uiBinder = GWT.create(GenerateIdentifiersDialogUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  //
  // Data members
  //

  private int affectedEntities = 0;

  @UiField
  DialogBox dialog;

  @UiField
  Label confirmationMessage;

  @UiField
  NumericTextBox size;

  @UiField
  Label sizeHelp;

  @UiField
  Label sampleIdentifier;

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
    initializeHandlers();
    resizeHandle.makeResizable(content);
    clear();
    dialog.hide();
  }

  //
  // ConfirmationPresenter.Display Methods
  //
  @Override
  public void show() {
    size.setFocus(true);
    super.show();
  }

  @Override
  public Widget asWidget() {
    return dialog;
  }

  @Override
  public void hideDialog() {
    clear();
    hide();
  }

  @Override
  public void setAffectedEntities(int count) {
    affectedEntities = count;
    updateDescriptionText();
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
    updateFields();
  }

  //
  // Private Methods
  //

  private void initializeHandlers() {
    size.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        updateFields();
      }
    });

    prefix.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        sampleIdentifier.setText(generateSampleIdentifier());
      }
    });

    allowZeros.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        sampleIdentifier.setText(generateSampleIdentifier());
      }
    });
  }

  private void updateFields() {
    boolean valid = isIdentifierSizeValid();
    generateButton.setEnabled(valid);
    prefix.setEnabled(valid);
    allowZeros.setEnabled(valid);
    sampleIdentifier.setText(valid ? generateSampleIdentifier() : "");
  }

  private boolean isIdentifierSizeValid() {
    int sizeValue = size.getText().isEmpty() ? 0 : Integer.valueOf(size.getText());
    return sizeValue >= MIN_IDENTIFIER_SIZE && sizeValue <= MAX_IDENTIFIER_SIZE;
  }

  private void initializeTexts() {
    dialog.setText(translations.generateUnitIdentifiers());
    updateDescriptionText();
    List<String> args = new ArrayList<String>();
    args.clear();
    args.add(String.valueOf(MIN_IDENTIFIER_SIZE));
    args.add(String.valueOf(MAX_IDENTIFIER_SIZE));
    sizeHelp.setText(TranslationsUtils.replaceArguments(translations.generateIdentifiersSizeHelp(), args));

    dialog.setModal(false);
    generateButton.setText(translations.generateIdentifiersButton());
  }

  private void updateDescriptionText() {
    List<String> args = new ArrayList<String>();
    args.add(String.valueOf(affectedEntities));
    String rawMessage = affectedEntities > 1
        ? translations.specifyGenerateFunctionalUnitIdentifiers()
        : translations.specifyGenerateFunctionalUnitIdentifier();
    confirmationMessage.setText(TranslationsUtils.replaceArguments(rawMessage, args));
  }

  private String generateSampleIdentifier() {
    int count = Integer.valueOf(size.getValue());
    String sample = replicateString('9', count);

    if(allowZeros.getValue()) {
      sample = "0" + replicateString('9', count - 1);
    }

    if(!prefix.getText().isEmpty()) {
      sample = prefix.getText() + sample;
    }

    return sample;
  }

  private static String replicateString(Character c, int count) {
    char[] chars = new char[count];
    Arrays.fill(chars, c);
    return new String(chars);
  }

}