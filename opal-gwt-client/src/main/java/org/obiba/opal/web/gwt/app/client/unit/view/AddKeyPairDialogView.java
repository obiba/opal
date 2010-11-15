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
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
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
  WizardStep publicKeyCreateStep;

  @UiField
  WizardStep publicKeyImportOrCreateStep;

  public AddKeyPairDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    dialog.setGlassEnabled(false);
    dialog.hide();
    dialog.addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        if(privateKeyStep.isVisible()) {

        }
      }
    });
  }

  @Override
  public void clear() {
    privateKeyStep.setVisible(true);
    publicKeyCreateStep.setVisible(false);
    publicKeyImportOrCreateStep.setVisible(false);
    dialog.setPreviousVisible(false);
    dialog.setFinishEnabled(false);
  }

  @Override
  public HasClickHandlers getAddButton() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getAlgorithm() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getAlias() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasClickHandlers getCancelButton() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getCityName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getCountry() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
  }

  @Override
  public HasText getFirstAndLastName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getKeySize() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getOrganizationName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getOrganizationalUnit() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getPrivateKeyImport() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getPublicKeyImport() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasText getStateName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasValue<Boolean> isPrivateKeyCreate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasValue<Boolean> isPrivateKeyImport() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasValue<Boolean> isPublicKeyCreate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HasValue<Boolean> isPublicKeyImport() {
    // TODO Auto-generated method stub
    return null;
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
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

}
