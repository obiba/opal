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

import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.EntitiesTabPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.EntitiesTabPresenter.EntitiesToView;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.SelectScriptVariablesTabPresenter.VariablesToView;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter.Display;

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

public class EntitiesTabView extends Composite implements EntitiesTabPresenter.Display {

  @UiTemplate("EntitiesTabView.ui.xml")
  interface myUiBinder extends UiBinder<Widget, EntitiesTabView> {
  }

  private static myUiBinder uiBinder = GWT.create(myUiBinder.class);

  @UiField
  ListBox entitiesToView;

  @UiField
  Button saveChangesButton;

  @UiField
  SimplePanel scriptWidgetPanel;

  private EvaluateScriptPresenter.Display scriptWidgetDisplay;

  public EntitiesTabView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  @Override
  public HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler) {
    return saveChangesButton.addClickHandler(clickHandler);
  }

  @Override
  public void saveChangesEnabled(boolean enabled) {
    saveChangesButton.setEnabled(enabled);
  }

  @Override
  public void setScriptWidget(Display scriptWidgetDisplay) {
    this.scriptWidgetDisplay = scriptWidgetDisplay;
    scriptWidgetPanel.add(scriptWidgetDisplay.asWidget());
  }

  @Override
  public void setScriptWidgetVisible(boolean visible) {
    scriptWidgetPanel.setVisible(visible);
  }

  @Override
  public void setScript(String script) {
    scriptWidgetDisplay.setScript(script);
  }

  @Override
  public String getScript() {
    return scriptWidgetDisplay.getScript();
  }

  @Override
  public void setEntitiesToView(EntitiesToView scriptOrAll) {
    String valueToSelect = (scriptOrAll.equals(VariablesToView.SCRIPT)) ? "script" : "all";

    for(int i = 0; i < entitiesToView.getItemCount(); i++) {
      if(entitiesToView.getValue(i).equals(valueToSelect)) {
        entitiesToView.setSelectedIndex(i);
        break;
      }
    }

    setScriptWidgetVisible(scriptOrAll.equals(EntitiesToView.SCRIPT));
  }

  @Override
  public EntitiesToView getEntitiesToView() {
    int selectedIndex = entitiesToView.getSelectedIndex();
    return (entitiesToView.getValue(selectedIndex).equals("script")) ? EntitiesToView.SCRIPT : EntitiesToView.ALL;
  }

  @Override
  public HandlerRegistration addEntitiestoViewChangeHandler(ChangeHandler changeHandler) {
    return entitiesToView.addChangeHandler(changeHandler);
  }

  @Override
  public HandlerRegistration addScriptChangeHandler(ChangeHandler handler) {
    return scriptWidgetDisplay.addScriptChangeHandler(handler);
  }

}
