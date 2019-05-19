/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import org.obiba.opal.web.gwt.ace.client.AceEditor;
import org.obiba.opal.web.gwt.app.client.magma.presenter.ScriptEditorPresenter;
import org.obiba.opal.web.gwt.app.client.support.VariableDtos;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.ViewImpl;

public class ScriptEditorView extends ViewImpl implements ScriptEditorPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, ScriptEditorView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  AceEditor scriptArea;

  @UiField
  Button testScript;

  @UiField
  Chooser valueTypes;

  @UiField
  CheckBox repeatable;

  public ScriptEditorView() {
    widget = uiBinder.createAndBindUi(this);
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
    return scriptArea.addChangeHandler(handler);
  }

  @Override
  public String getBeautifiedScript() {
    return scriptArea.getBeautifiedText();
  }

  @Override
  public void setIsRepeatable(boolean value) {
    repeatable.setValue(value);
  }

  @Override
  public void setValueType(String value) {
    valueTypes.setSelectedValue(value);
  }

  @Override
  public String getScript() {
    return scriptArea.getText();
  }

  @Override
  public String getSelectedScript() {
    return scriptArea.getSelectedText();
  }

  @Override
  public VariableDtos.ValueType getValueType() {
    return VariableDtos.ValueType.fromString(valueTypes.getSelectedValue());
  }

  @Override
  public boolean isRepeatable() {
    return repeatable.getValue();
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
    setScript("");
  }

  @Override
  public void showTest(boolean b) {
    testScript.setVisible(b);
  }
}
