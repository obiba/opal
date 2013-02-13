/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ScriptEditorPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveCustomVariablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget.ValueTypeBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class DeriveCustomVariableStepView extends ViewImpl implements DeriveCustomVariablePresenter.Display {

  @UiTemplate("DeriveCustomVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveCustomVariableStepView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardStep deriveStep;

  @UiField
  ValueTypeBox valueTypeBox;

  @UiField
  FlowPanel scriptEditor;

  @UiField
  CheckBox repeatable;

  public DeriveCustomVariableStepView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public BranchingWizardStepController.Builder getDeriveStepController() {
    return (BranchingWizardStepController.Builder) BranchingWizardStepController.Builder.create(deriveStep)
        .title(translations.recodeCustomDeriveStepTitle());
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == ScriptEditorPresenter.Display.Slots.Editor) {
      scriptEditor.clear();
      scriptEditor.add(content);
    }
  }

  @Override
  public HasValue<String> getValueType() {
    return valueTypeBox;
  }

  @Override
  public HasValue<Boolean> getRepeatable() {
    return repeatable;
  }

}
