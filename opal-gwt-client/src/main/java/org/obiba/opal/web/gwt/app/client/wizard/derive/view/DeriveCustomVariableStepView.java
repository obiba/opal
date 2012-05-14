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
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveCustomVariablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget.ScriptSuggestBox;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget.ValueTypeBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class DeriveCustomVariableStepView extends ViewImpl implements DeriveCustomVariablePresenter.Display {

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiTemplate("DeriveCustomVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveCustomVariableStepView> {
  }

  private final Widget widget;

  @UiField
  WizardStep deriveStep;

  @UiField
  ValueTypeBox valueTypeBox;

  @UiField
  ScriptSuggestBox scriptBox;

  @UiField
  CheckBox repeatable;

  @UiField
  HasClickHandlers testButton;

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
  public void add(Widget widget) {
    deriveStep.add(widget);
  }

  @Override
  public ScriptSuggestBox getScriptBox() {
    return scriptBox;
  }

  @Override
  public HasValue<String> getValueType() {
    return valueTypeBox;
  }

  @Override
  public void addSuggestions(TableDto table) {
    scriptBox.addAsyncSuggestions(table);
  }

  @Override
  public HasClickHandlers getTestButton() {
    return testButton;
  }

  @Override
  public HasValue<Boolean> getRepeatable() {
    return repeatable;
  }

}
