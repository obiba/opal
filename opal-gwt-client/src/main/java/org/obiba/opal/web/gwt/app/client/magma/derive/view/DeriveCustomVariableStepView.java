/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.magma.derive.presenter.DeriveCustomVariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.derive.view.widget.ValueTypeBox;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
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
  FlowPanel scriptEditor;

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
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == Slots.Editor) {
      scriptEditor.clear();
      scriptEditor.add(content.asWidget());
    }
  }
}
