/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.identifiers.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.GenerateIdentifiersModalPresenter.Display;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.GenerateIdentifiersModalUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class GenerateIdentifiersModalView extends ModalPopupViewWithUiHandlers<GenerateIdentifiersModalUiHandlers>
    implements Display {

  interface Binder extends UiBinder<Widget, GenerateIdentifiersModalView> {}

  //
  // Constants
  //

  private static final int MIN_IDENTIFIER_SIZE = 5;

  private static final int MAX_IDENTIFIER_SIZE = 20;

  private final Translations translations;

  //
  // Data members
  //

  private int affectedEntities = 0;

  @UiField
  Modal dialog;

  @UiField
  Paragraph confirmationMessage;

  @UiField
  NumericTextBox size;

  @UiField
  HelpBlock sizeHelp;

  @UiField
  InlineLabel sampleIdentifier;

  @UiField
  TextBox prefix;

  @UiField
  CheckBox allowZeros;

  @UiField
  Button generateButton;

  @UiField
  Button cancelButton;

  //
  // Constructors
  //
  @Inject
  public GenerateIdentifiersModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initializeTexts();
    initializeHandlers();
  }

  //
  // ConfirmationPresenter.Display Methods
  //
  @Override
  public void onShow() {
    size.setFocus(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setAffectedEntities(int count) {
    affectedEntities = count;
    updateDescriptionText();
  }

  @Override
  public void setBusy(boolean busy) {
    generateButton.setEnabled(!busy);
    cancelButton.setEnabled(!busy);
    dialog.setBusy(busy);
  }

  @Override
  public void setDefault(int sizeNb, String prefixStr) {
    size.setValue(sizeNb + "");
    prefix.setText(prefixStr);
    sampleIdentifier.setText(generateSampleIdentifier());
  }

  @UiHandler("generateButton")
  public void onGenerateButtonClicked(ClickEvent event) {
    getUiHandlers().generateIdentifiers(getSize(), getAllowZeros(), getPrefix());
  }

  @UiHandler("cancelButton")
  public void onCancelButtonClicked(ClickEvent event) {
    hideDialog();
  }

  private Number getSize() {
    return size.getNumberValue();
  }

  private String getPrefix() {
    return prefix.getText();
  }

  private boolean getAllowZeros() {
    return allowZeros.getValue();
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
    dialog.setTitle(translations.generateIdentifiers());
    updateDescriptionText();
    List<String> args = new ArrayList<String>();
    args.clear();
    args.add(String.valueOf(MIN_IDENTIFIER_SIZE));
    args.add(String.valueOf(MAX_IDENTIFIER_SIZE));
    sizeHelp.setText(TranslationsUtils.replaceArguments(translations.generateIdentifiersSizeHelp(), args));
    generateButton.setText(translations.generateIdentifiersButton());
    size.setValue(Integer.toString(MIN_IDENTIFIER_SIZE), false);
  }

  private void updateDescriptionText() {
    List<String> args = new ArrayList<String>();
    args.add(String.valueOf(affectedEntities));
    String rawMessage = affectedEntities > 1
        ? translations.specifyGenerateIdentifiers()
        : translations.specifyGenerateIdentifier();
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