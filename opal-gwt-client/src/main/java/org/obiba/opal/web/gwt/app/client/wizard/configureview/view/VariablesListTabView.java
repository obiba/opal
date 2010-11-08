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

import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.VariablesListTabPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class VariablesListTabView extends Composite implements VariablesListTabPresenter.Display {

  private static VariablesListTabViewUiBinder uiBinder = GWT.create(VariablesListTabViewUiBinder.class);

  @UiField(provided = true)
  SuggestBox variableNameSuggestBox;

  @UiField
  Anchor previous;

  @UiField
  Anchor next;

  @UiField
  Button saveChangesButton;

  @UiField
  Button addButton;

  @UiField
  Button removeButton;

  @UiField
  TextBox variableName;

  @UiField
  ListBox entityType;

  @UiField
  CheckBox repeatableCheckbox;

  @UiField
  SimplePanel scriptWidgetPanel;

  @UiField
  TabLayoutPanel variableDetailTabs;

  @UiField
  SimplePanel categoriesTabPanel;

  @UiField
  SimplePanel attributesTabPanel;

  @UiField
  SimplePanel optionsTabPanel;

  MultiWordSuggestOracle suggestions;

  public VariablesListTabView() {
    variableNameSuggestBox = new SuggestBox(suggestions = new MultiWordSuggestOracle());
    initWidget(uiBinder.createAndBindUi(this));
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
  public void addVariableNameSuggestion(String variableName) {
    suggestions.add(variableName);
  }

  @Override
  public void clearVariableListSuggestions() {
    suggestions.clear();
  }

  @UiTemplate("VariablesListTabView.ui.xml")
  interface VariablesListTabViewUiBinder extends UiBinder<Widget, VariablesListTabView> {
  }

  @Override
  public HandlerRegistration addPreviousVariableNameClickHandler(ClickHandler handler) {
    return previous.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addNextVariableNameClickHandler(ClickHandler handler) {
    return next.addClickHandler(handler);
  }

  @Override
  public void setSelectedVariableName(String variableName) {
    variableNameSuggestBox.setText(variableName);
  }

  @Override
  public HandlerRegistration addVariableNameChangedHandler(ValueChangeHandler<String> handler) {
    return variableNameSuggestBox.addValueChangeHandler(handler);
  }

  @Override
  public HandlerRegistration addVariableNameSelectedHandler(SelectionHandler<Suggestion> handler) {
    return variableNameSuggestBox.addSelectionHandler(handler);
  }

}
