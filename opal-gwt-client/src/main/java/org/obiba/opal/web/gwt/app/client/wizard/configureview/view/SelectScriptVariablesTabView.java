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

import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.SelectScriptVariablesTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.SelectScriptVariablesTabPresenter.VariablesToView;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SelectScriptVariablesTabView extends Composite implements SelectScriptVariablesTabPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  Button saveChangesButton;

  @UiField
  ListBox variablesToView;

  @UiField
  SimplePanel scriptWidgetPanel;

  private EvaluateScriptPresenter.Display scriptWidgetDisplay;

  //
  // Constructors
  //

  public SelectScriptVariablesTabView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // SelectScriptVariablesTabPresenter.Display Methods
  //

  @Override
  public void saveChangesEnabled(boolean enabled) {
    saveChangesButton.setEnabled(enabled);
  }

  @Override
  public void setScriptWidget(EvaluateScriptPresenter.Display scriptWidgetDisplay) {
    this.scriptWidgetDisplay = scriptWidgetDisplay;
    scriptWidgetPanel.add(scriptWidgetDisplay.asWidget());
  }

  @Override
  public void setScriptWidgetVisible(boolean visible) {
    scriptWidgetPanel.setVisible(visible);
  }

  @Override
  public VariablesToView getVariablesToView() {
    int selectedIndex = variablesToView.getSelectedIndex();
    return (variablesToView.getValue(selectedIndex).equals("script")) ? VariablesToView.SCRIPT : VariablesToView.ALL;
  }

  @Override
  public HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler) {
    return saveChangesButton.addClickHandler(clickHandler);
  }

  // @Override
  public HandlerRegistration addVariablestoViewChangeHandler(ChangeHandler changeHandler) {
    return variablesToView.addChangeHandler(changeHandler);
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

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("SelectScriptVariablesTabView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, SelectScriptVariablesTabView> {
  }
}
