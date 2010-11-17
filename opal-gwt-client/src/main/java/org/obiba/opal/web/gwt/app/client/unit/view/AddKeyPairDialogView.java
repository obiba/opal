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

import org.obiba.opal.web.gwt.app.client.unit.presenter.AddKeyPairDialogPresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.AddKeyPairDialogPresenter.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class AddKeyPairDialogView extends Composite implements AddKeyPairDialogPresenter.Display {

  @UiTemplate("AddKeyPairDialogView.ui.xml")
  interface AddKeyPairDialogUiBinder extends UiBinder<DialogBox, AddKeyPairDialogView> {
  }

  private static AddKeyPairDialogUiBinder uiBinder = GWT.create(AddKeyPairDialogUiBinder.class);

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep privateKeyStep;

  @UiField
  WizardStep publicKeyStep;

  @UiField
  TextBox alias;

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

  private ValidationHandler privateKeyValidators;

  private ValidationHandler publicKeyValidators;

  public AddKeyPairDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWizardDialog();
    initPrivateKeyStep();
    initPublicKeyStep();
  }

  private void initWizardDialog() {
    dialog.setGlassEnabled(false);
    dialog.hide();
    dialog.addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        // validate
        if(privateKeyValidators.validate()) {
          privateKeyStep.setVisible(false);

          publicKeyStep.setVisible(true);
          publicKeyCreated.setValue(true, true);
          publicKeyCreated.setVisible(privateKeyImported.getValue());
          publicKeyImported.setVisible(privateKeyImported.getValue());
          publicKeyPEM.setVisible(privateKeyImported.getValue());
          if(privateKeyCreated.getValue()) {
            publicKeyForm.removeStyleName("indent");
          } else {
            publicKeyForm.addStyleName("indent");
          }

          dialog.setPreviousEnabled(true);
          dialog.setNextEnabled(false);
          dialog.setFinishEnabled(true);
        }
      }
    });
    dialog.addPreviousClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        privateKeyStep.setVisible(true);
        publicKeyStep.setVisible(false);
        dialog.setPreviousEnabled(false);
        dialog.setNextEnabled(true);
        dialog.setFinishEnabled(false);
      }
    });
  }

  private void initPrivateKeyStep() {
    privateKeyStep.setStepTitle("Alias and Private Key definition");
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
        if(privateKeyPEM.getText().equals("(paste private key in PEM format)")) {
          privateKeyPEM.setText("");
          privateKeyPEM.removeStyleName("default-text");
        }
      }
    });
  }

  private void initPublicKeyStep() {
    publicKeyStep.setStepTitle("Public Certificate definition");
    publicKeyCreated.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> arg0) {
        publicKeyImported.setValue(!publicKeyCreated.getValue());
        publicKeyPEM.setEnabled(!publicKeyCreated.getValue());
        names.setEnabled(publicKeyCreated.getValue());
        organizationalUnit.setEnabled(publicKeyCreated.getValue());
        organizationName.setEnabled(publicKeyCreated.getValue());
        city.setEnabled(publicKeyCreated.getValue());
        state.setEnabled(publicKeyCreated.getValue());
        country.setEnabled(publicKeyCreated.getValue());
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
        if(publicKeyPEM.getText().equals("(paste public certificate in PEM format)")) {
          publicKeyPEM.setText("");
          publicKeyPEM.removeStyleName("default-text");
        }
      }
    });
  }

  private void clearPrivateKeyCreateForm() {
    algo.setText("");
    size.setText("512");
  }

  private void clearPrivateKeyImportForm() {
    privateKeyPEM.setText("(paste private key in PEM format)");
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
    publicKeyPEM.setText("(paste public certificate in PEM format)");
    publicKeyPEM.addStyleName("default-text");
  }

  @Override
  public void clear() {
    alias.setText("");
    clearPrivateKeyCreateForm();
    clearPrivateKeyImportForm();
    clearPublicKeyCreateForm();
    clearPublicKeyImportForm();

    privateKeyStep.setVisible(true);
    privateKeyCreated.setValue(true, true);
    publicKeyCreated.setValue(true, true);
    publicKeyStep.setVisible(false);

    dialog.setPreviousEnabled(false);
    dialog.setNextEnabled(true);
    dialog.setFinishEnabled(false);
  }

  @Override
  public HasClickHandlers getAddButton() {
    // TODO Auto-generated method stub
    return null;
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
  public HasClickHandlers getCancelButton() {
    // TODO Auto-generated method stub
    return null;
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
  public HasCloseHandlers getDialog() {
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
  public HasText getPrivateKeyImport() {
    return privateKeyPEM;
  }

  @Override
  public HasText getPublicKeyImport() {
    return publicKeyPEM;
  }

  @Override
  public HasText getState() {
    return state;
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
  public void showDialog() {
    clear();
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
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
  public HandlerRegistration addFinishClickHandler(final ClickHandler handler) {
    // forward finish event only if the form is valid
    return dialog.addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(publicKeyValidators.validate()) {
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
  public void setPrivateKeyValidationHandler(ValidationHandler validator) {
    this.privateKeyValidators = validator;
  }

  @Override
  public void setPublicKeyValidationHandler(ValidationHandler validator) {
    this.publicKeyValidators = validator;
  }

}
