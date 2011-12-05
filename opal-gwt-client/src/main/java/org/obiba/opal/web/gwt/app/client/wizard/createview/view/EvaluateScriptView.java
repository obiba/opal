/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.view;

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
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class EvaluateScriptView extends Composite implements EvaluateScriptPresenter.Display {

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  TextArea scriptArea;

  @UiField
  Button testScript;

  public EvaluateScriptView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiTemplate("EvaluateScriptView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, EvaluateScriptView> {
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
  public HandlerRegistration addTestScriptClickHandler(ClickHandler handler) {
    return testScript.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addScriptChangeHandler(ChangeHandler handler) {
    return scriptArea.addChangeHandler(handler);
  }

  @Override
  public String getScript() {
    return scriptArea.getText();
  }

  public String getSelectedScript() {
    int start = scriptArea.getCursorPos();
    if(start < 0) {
      return "";
    }
    int length = scriptArea.getSelectionLength();
    String selected = scriptArea.getText().substring(start, start + length);
    GWT.log("selected=" + selected);
    return selected;
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    scriptArea.setReadOnly(readOnly);
  }

  @Override
  public void setScript(String script) {
    scriptArea.setText(script);
  }

  @Override
  public void formEnable(boolean enabled) {
    scriptArea.setEnabled(enabled);
    testScript.setEnabled(enabled);
  }

  @Override
  public void formClear() {
    scriptArea.setText("");
  }
}
