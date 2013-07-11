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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class SelectScriptVariablesTabView extends ViewImpl implements SelectScriptVariablesTabPresenter.Display {

  @UiTemplate("SelectScriptVariablesTabView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, SelectScriptVariablesTabView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  Button saveChangesButton;

  @UiField
  ListBox variablesToView;

  @UiField
  SimplePanel scriptWidgetPanel;

  private SelectScriptVariablesTabViewHelp helpWidget;

  public SelectScriptVariablesTabView() {
    widget = uiBinder.createAndBindUi(this);
  }

  //
  // SelectScriptVariablesTabPresenter.Display Methods
  //

  @Override
  public Widget getHelpWidget() {
    if(helpWidget == null) {
      helpWidget = new SelectScriptVariablesTabViewHelp();
    }
    return helpWidget;
  }

  @Override
  public void saveChangesEnabled(boolean enabled) {
    saveChangesButton.setEnabled(enabled);
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == Slots.Test) {
      scriptWidgetPanel.add(content);
    }
  }

  @Override
  public void setScriptWidgetVisible(boolean visible) {
    scriptWidgetPanel.setVisible(visible);
  }

  @Override
  public void setVariablesToView(VariablesToView scriptOrAll) {
    String valueToSelect = scriptOrAll == VariablesToView.SCRIPT ? "script" : "all";

    for(int i = 0; i < variablesToView.getItemCount(); i++) {
      if(variablesToView.getValue(i).equals(valueToSelect)) {
        variablesToView.setSelectedIndex(i);
        break;
      }
    }

    setScriptWidgetVisible(scriptOrAll == VariablesToView.SCRIPT);
  }

  @Override
  public VariablesToView getVariablesToView() {
    return "script".equals(variablesToView.getValue(variablesToView.getSelectedIndex()))
        ? VariablesToView.SCRIPT
        : VariablesToView.ALL;
  }

  @Override
  public HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler) {
    return saveChangesButton.addClickHandler(clickHandler);
  }

  @Override
  public HandlerRegistration addVariablesToViewChangeHandler(ChangeHandler changeHandler) {
    return variablesToView.addChangeHandler(changeHandler);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

}
