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
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveCustomVariablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget.ScriptSuggestBox;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget.ValueTypeBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.LinkDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public class DeriveCustomVariableStepView extends Composite implements DeriveCustomVariablePresenter.Display {

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

  @UiTemplate("DeriveCustomVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveCustomVariableStepView> {
  }

  @UiField
  WizardStep deriveStep;

  @UiField
  ValueTypeBox valueTypeBox;

  @UiField
  ScriptSuggestBox script;

  @UiField
  Button testButton;

  public DeriveCustomVariableStepView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public DefaultWizardStepController.Builder getDeriveStepController() {
    return DefaultWizardStepController.Builder.create(deriveStep).title(translations.recodeCustomDeriveStepTitle());
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void add(Widget widget) {
    deriveStep.add(widget);
  }

  @Override
  public HasValue<String> getScript() {
    return script;
  }

  @Override
  public HasValue<String> getValueType() {
    return valueTypeBox;
  }

  @Override
  public void pushSuggestions(LinkDto parentLink) {
    script.pushAsyncSuggestions(parentLink);
  }

  @Override
  public HasClickHandlers getTestButton() {
    return testButton;
  }

}
