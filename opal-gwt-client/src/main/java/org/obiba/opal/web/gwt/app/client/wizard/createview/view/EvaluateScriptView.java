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

import org.obiba.opal.web.gwt.ace.client.AceEditor;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class EvaluateScriptView extends ViewImpl implements EvaluateScriptPresenter.Display {

  @UiTemplate("EvaluateScriptView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, EvaluateScriptView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  AceEditor scriptArea;

  @UiField
  Button testScript;

  public EvaluateScriptView() {
    widget = uiBinder.createAndBindUi(this);
    GWT.log("EvaluateScriptView", new Exception());
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public HandlerRegistration addTestScriptClickHandler(ClickHandler handler) {
    return testScript.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addScriptChangeHandler(ChangeHandler handler) {
    //TODO ace
//    return scriptArea.addChangeHandler(handler);
    return null;
  }

  @Override
  public String getScript() {
    return scriptArea.getText();
  }

  @Override
  public String getSelectedScript() {
    String selectedText = scriptArea.getSelectedText();
    GWT.log("selectedText: " + selectedText);
    return selectedText;
  }

  @Override
  public void setScript(String script) {
    scriptArea.setText(script);
  }

  @Override
  public void formEnable(boolean enabled) {
    //TODO ace
//    scriptArea.setEnabled(enabled);
    testScript.setEnabled(enabled);
  }

  @Override
  public void formClear() {
    setScript("");
  }

  @Override
  public void showTest(boolean b) {
    testScript.setVisible(b);
  }
}
