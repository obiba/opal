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
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
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
  ListBox valueType;

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
  HTMLPanel optionsTabPanel;

  @UiField
  TextBox occurenceGroup;

  @UiField
  TextBox mimeType;

  @UiField
  TextBox unit;

  MultiWordSuggestOracle suggestions;

  private String entityType;

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
  public HasBeforeSelectionHandlers<Integer> getDetailTabs() {
    return variableDetailTabs;
  }

  @Override
  public void displayDetailTab(int tabNumber) {
    variableDetailTabs.selectTab(tabNumber);
  }

  @Override
  public void addCategoriesTabWidget(Widget categoriesTabWidget) {
    categoriesTabPanel.clear();
    categoriesTabPanel.add(categoriesTabWidget);
  }

  @Override
  public void addAttributesTabWidget(Widget attributesTabWidget) {
    attributesTabPanel.clear();
    attributesTabPanel.add(attributesTabWidget);
  }

  @Override
  public void addVariableNameSuggestion(String variableName) {
    suggestions.add(variableName);
  }

  @Override
  public void clearVariableNameSuggestions() {
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
  public HandlerRegistration addVariableNameChangedHandler(ValueChangeHandler<String> handler) {
    return variableNameSuggestBox.addValueChangeHandler(handler);
  }

  @Override
  public HandlerRegistration addVariableNameSelectedHandler(SelectionHandler<Suggestion> handler) {
    return variableNameSuggestBox.addSelectionHandler(handler);
  }

  @Override
  public HandlerRegistration addVariableNameEnterKeyPressed(KeyDownHandler handler) {
    return variableNameSuggestBox.addKeyDownHandler(handler);
  }

  @Override
  public void setSelectedVariableName(String variableName, String previousVariableName, String nextVariableName) {
    variableNameSuggestBox.setText(variableName);
    previous.setTitle(previousVariableName);
    next.setTitle(nextVariableName);

    previous.setEnabled(previousVariableName != null);
    next.setEnabled(nextVariableName != null);
  }

  @Override
  public String getSelectedVariableName() {
    return variableNameSuggestBox.getTextBox().getText();
  }

  @Override
  public HandlerRegistration addRepeatableValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    return repeatableCheckbox.addValueChangeHandler(handler);
  }

  @Override
  public void setEnabledOccurenceGroup(Boolean enabled) {
    occurenceGroup.setEnabled(enabled);
  }

  @Override
  public void clearOccurrenceGroup() {
    occurenceGroup.setText("");
  }

  @Override
  public HasText getOccurenceGroup() {
    return occurenceGroup;
  }

  @Override
  public HasValue<Boolean> getRepeatable() {
    return repeatableCheckbox;
  }

  @Override
  public HandlerRegistration addSaveChangesClickHandler(ClickHandler handler) {
    return saveChangesButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addAddVariableClickHandler(ClickHandler handler) {
    return addButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addVariableClickHandler(ClickHandler handler) {
    return addButton.addClickHandler(handler);
  }

  @Override
  public void setNewVariable(VariableDto variableDto) {
    variableName.setValue(variableDto.getName());
    entityType = variableDto.getEntityType();

  }

  @Override
  public VariableDto getVariableDto() {
    VariableDto variableDto = VariableDto.create();
    variableDto.setName(variableName.getValue());
    variableDto.setIsRepeatable(repeatableCheckbox.getValue());
    variableDto.setValueType(valueType.getValue(valueType.getSelectedIndex()));
    variableDto.setEntityType(entityType);
    return variableDto;
  }

}
