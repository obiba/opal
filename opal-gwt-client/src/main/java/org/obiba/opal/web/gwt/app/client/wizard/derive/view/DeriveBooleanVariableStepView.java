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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveBooleanVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DeriveBooleanVariableStepView extends ViewImpl implements DeriveBooleanVariableStepPresenter.Display {

  @UiTemplate("DeriveBooleanVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveBooleanVariableStepView> {
  }

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardStep mapStep;

  @UiField
  ValueMapGrid valuesMapGrid;

  //
  // Constructors
  //

  public DeriveBooleanVariableStepView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public DefaultWizardStepController.Builder getMapStepController() {
    return DefaultWizardStepController.Builder.create(mapStep).title(translations.recodeBooleanStepTitle());
  }

  @Override
  public void populateValues(List<ValueMapEntry> valuesMap, List<String> derivedCategories) {
    valuesMapGrid.populate(valuesMap, derivedCategories);
  }

  //
  // Widget Display methods
  //

  @Override
  public Widget asWidget() {
    return widget;
  }

}
