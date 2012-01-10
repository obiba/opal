/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.unit.presenter.AddKeyPairDialogPresenter;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class AddCryptoKeyDialogView extends PopupViewImpl implements AddKeyPairDialogPresenter.Display {

  @UiTemplate("AddCryptoKeyDialogView.ui.xml")
  interface ViewUiBinder extends UiBinder<WizardDialogBox, AddCryptoKeyDialogView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final WizardDialogBox dialog;

  @UiField
  WizardStep keyTypeStep;

  @UiField
  WizardStep importCertificateStep;

  @UiField
  WizardStep privateKeyStep;

  @UiField
  WizardStep publicKeyStep;

  @UiField
  TextBox alias;

  @UiField
  RadioButton keyPairType;

  @UiField
  RadioButton certificateType;

  @UiField
  TextArea certPEM;

  @UiField
  RadioButton privateKeyCreated;

  @UiField
  TextBox algo;

  @UiField
  NumericTextBox size;

  @UiField
  RadioButton privateKeyImported;

  @UiField
  TextArea privateKeyPEM;

  @UiField
  RadioButton publicKeyCreated;

  @UiField
  HTMLPanel publicKeyForm;

  @UiField
  TextBox names;

  @UiField
  TextBox organizationalUnit;

  @UiField
  TextBox organizationName;

  @UiField
  TextBox city;

  @UiField
  TextBox state;

  @UiField
  TextBox country;

  @UiField
  RadioButton publicKeyImported;

  @UiField
  TextArea publicKeyPEM;

  private ValidationHandler keyTypeStepValidators;

  private ValidationHandler certificateStepValidators;

  private ValidationHandler privateKeyValidators;

  private ValidationHandler publicKeyValidators;

  private WizardStepChain stepChain;

  @Inject
  public AddCryptoKeyDialogView(EventBus eventBus) {
    super(eventBus);
    this.dialog = uiBinder.createAndBindUi(this);
  }

  private void initWizardDialog() {

    DefaultWizardStepController p = BranchingWizardStepController.Builder.create(keyTypeStep) //
    .branch(DefaultWizardStepController.Builder.create(privateKeyStep)//
    .title(translations.privateKeyStep())//
    .onValidate(privateKeyValidators)//
    .next(DefaultWizardStepController.Builder.create(publicKeyStep).title(translations.publicKeyStep()).onStepIn(new StepInHandler() {

      @Override
      public void onStepIn() {
        publicKeyCreated.setVisible(privateKeyImported.getValue());
        publicKeyImported.setVisible(privateKeyImported.getValue());
        publicKeyPEM.setVisible(privateKeyImported.getValue());
        updateCertFields();
        if(privateKeyCreated.getValue()) {
          publicKeyForm.removeStyleName("indent");
        } else {
          publicKeyForm.addStyleName("indent");
        }
      }
    }).onValidate(publicKeyValidators).build()).build(), keyPairType)//
    .branch(DefaultWizardStepController.Builder.create(importCertificateStep)//
    .title(translations.importCertificateStep()).onValidate(certificateStepValidators).build(), certificateType)//
    .title(translations.keyTypeStep())//
    .onValidate(keyTypeStepValidators).build();

    stepChain = WizardStepChain.Builder.create(dialog).append(p).onNext().onPrevious().build();

    initImportCertificateStep();
    initPrivateKeyStep();
    initPublicKeyStep();
  }

  private void initImportCertificateStep() {
    certPEM.addFocusHandler(new FocusHandler() {

      @Override
      public void onFocus(FocusEvent event) {
        if(certPEM.getText().equals(translations.pastePublicKeyPEM())) {
          certPEM.setText("");
          certPEM.removeStyleName("default-text");
        }
      }
    });
  }

  private void initPrivateKeyStep() {
    // size.setMaxConstrained(false);
    privateKeyCreated.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> arg0) {
        privateKeyImported.setValue(!privateKeyCreated.getValue());
        privateKeyPEM.setEnabled(!privateKeyCreated.getValue());
        algo.setEnabled(privateKeyCreated.getValue());
        size.setEnabled(privateKeyCreated.getValue());
        if(privateKeyCreated.getValue()) {
          clearPrivateKeyImportForm();
        } else {
          clearPrivateKeyCreateForm();
        }
      }
    });
    privateKeyImported.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        privateKeyCreated.setValue(!privateKeyImported.getValue(), true);
      }
    });

    privateKeyPEM.addFocusHandler(new FocusHandler() {

      @Override
      public void onFocus(FocusEvent arg0) {
        if(privateKeyPEM.getText().equals(getDefaultPrivateKeyText())) {
          privateKeyPEM.setText("");
          privateKeyPEM.removeStyleName("default-text");
        }
      }
    });
  }

  private void updateCertFields() {
    boolean enabled = publicKeyCreated.getValue() || privateKeyCreated.getValue();
    names.setEnabled(enabled);
    organizationalUnit.setEnabled(enabled);
    organizationName.setEnabled(enabled);
    city.setEnabled(enabled);
    state.setEnabled(enabled);
    country.setEnabled(enabled);
  }

  private void initPublicKeyStep() {
    publicKeyCreated.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> arg0) {
        publicKeyImported.setValue(!publicKeyCreated.getValue());
        publicKeyPEM.setEnabled(!publicKeyCreated.getValue());
        updateCertFields();
        if(publicKeyCreated.getValue()) {
          clearPublicKeyImportForm();
        } else {
          clearPublicKeyCreateForm();
        }
      }
    });
    publicKeyImported.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        publicKeyCreated.setValue(!publicKeyImported.getValue(), true);
      }
    });
    publicKeyPEM.addFocusHandler(new FocusHandler() {

      @Override
      public void onFocus(FocusEvent arg0) {
        if(publicKeyPEM.getText().equals(translations.pastePublicKeyPEM())) {
          publicKeyPEM.setText("");
          publicKeyPEM.removeStyleName("default-text");
        }
      }
    });
  }

  private void clearCertificateForm() {
    certPEM.setText(translations.pastePublicKeyPEM());
    certPEM.addStyleName("default-text");
  }

  private void clearPrivateKeyCreateForm() {
    algo.setText("RSA");
    size.setText("2048");
  }

  private void clearPrivateKeyImportForm() {
    privateKeyPEM.setText(getDefaultPrivateKeyText());
    privateKeyPEM.addStyleName("default-text");
  }

  private void clearPublicKeyCreateForm() {
    names.setText("");
    organizationalUnit.setText("");
    organizationName.setText("");
    city.setText("");
    state.setText("");
    country.setText("");
  }

  private void clearPublicKeyImportForm() {
    publicKeyPEM.setText(translations.pastePublicKeyPEM());
    publicKeyPEM.addStyleName("default-text");
  }

  @Override
  public void clear() {
    alias.setText("");
    clearCertificateForm();
    clearPrivateKeyCreateForm();
    clearPrivateKeyImportForm();
    clearPublicKeyCreateForm();
    clearPublicKeyImportForm();

    keyTypeStep.setVisible(true);
    keyPairType.setValue(true, true);
    importCertificateStep.setVisible(false);
    privateKeyStep.setVisible(false);
    privateKeyCreated.setValue(true, true);
    publicKeyCreated.setValue(true, true);
    publicKeyStep.setVisible(false);
  }

  @Override
  public HasText getAlgorithm() {
    return algo;
  }

  @Override
  public HasText getAlias() {
    return alias;
  }

  @Override
  public HasValue<String> getCertificatePem() {
    return this.certPEM;
  }

  @Override
  public HasText getCity() {
    return city;
  }

  @Override
  public HasText getCountry() {
    return country;
  }

  @Override
  public HasCloseHandlers<PopupPanel> getDialog() {
    return dialog;
  }

  @Override
  public HasText getFirstAndLastName() {
    return names;
  }

  @Override
  public HasText getKeySize() {
    return size;
  }

  @Override
  public HasText getOrganizationName() {
    return organizationName;
  }

  @Override
  public HasText getOrganizationalUnit() {
    return organizationalUnit;
  }

  @Override
  public HasValue<String> getPrivateKeyImport() {
    return privateKeyPEM;
  }

  @Override
  public String getDefaultPrivateKeyText() {
    return translations.pastePrivateKeyPEM();
  }

  @Override
  public HasValue<String> getPublicKeyImport() {
    return publicKeyPEM;
  }

  @Override
  public String getDefaultPublicKeyText() {
    return translations.pastePublicKeyPEM();
  }

  @Override
  public HasText getState() {
    return state;
  }

  @Override
  public HasValue<Boolean> isKeyPair() {
    return keyPairType;
  }

  @Override
  public HasValue<Boolean> isCertificate() {
    return this.certificateType;
  }

  @Override
  public HasValue<Boolean> isPrivateKeyCreate() {
    return privateKeyCreated;
  }

  @Override
  public HasValue<Boolean> isPrivateKeyImport() {
    return privateKeyImported;
  }

  @Override
  public HasValue<Boolean> isPublicKeyCreate() {
    return publicKeyCreated;
  }

  @Override
  public HasValue<Boolean> isPublicKeyImport() {
    return publicKeyImported;
  }

  @Override
  public void show() {
    if(stepChain == null) {
      initWizardDialog();
    }

    stepChain.reset();
    clear();
    super.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public Widget asWidget() {
    return dialog;
  }

  @Override
  public HandlerRegistration addFinishClickHandler(final ClickHandler handler) {
    // forward finish event only if the form is valid
    return dialog.addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(stepChain.isValid()) {
          handler.onClick(evt);
        }
      }
    });
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public void setKeyTypeValidationHandler(ValidationHandler handler) {
    this.keyTypeStepValidators = handler;
  }

  @Override
  public void setCertificateStepHandler(ValidationHandler handler) {
    this.certificateStepValidators = handler;
  }

  @Override
  public void setPrivateKeyValidationHandler(ValidationHandler validator) {
    this.privateKeyValidators = validator;
  }

  @Override
  public void setPublicKeyValidationHandler(ValidationHandler validator) {
    this.publicKeyValidators = validator;
  }

}
