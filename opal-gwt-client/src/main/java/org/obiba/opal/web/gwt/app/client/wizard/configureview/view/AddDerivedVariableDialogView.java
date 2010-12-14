/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.AddDerivedVariableDialogPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

public class AddDerivedVariableDialogView extends Composite implements AddDerivedVariableDialogPresenter.Display {

  @UiTemplate("AddDerivedVariableDialogView.ui.xml")
  interface AddDerivedVariableDialogUiBinder extends UiBinder<DialogBox, AddDerivedVariableDialogView> {
  }

  private static AddDerivedVariableDialogUiBinder uiBinder = GWT.create(AddDerivedVariableDialogUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  Button addButton;

  @UiField
  Button cancelButton;

  @UiField(provided = true)
  SuggestBox variableNameSuggestBox;

  MultiWordSuggestOracle suggestions;

  public AddDerivedVariableDialogView() {
    variableNameSuggestBox = new SuggestBox(suggestions = new MultiWordSuggestOracle());
    uiBinder.createAndBindUi(this);
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
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return cancelButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addAddVariableClickHandler(ClickHandler handler) {
    return addButton.addClickHandler(handler);
  }

  @Override
  public HasText getVariableName() {
    return variableNameSuggestBox;
  }

  @Override
  public void addVariableSuggestion(String suggestion) {
    suggestions.add(suggestion);
  }

  @Override
  public void clearVariableSuggestions() {
    suggestions.clear();
  }
}
