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
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.TemporalVariableDerivationHelper.GroupMethod;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveTemporalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DeriveTemporalVariableStepView extends Composite implements DeriveTemporalVariableStepPresenter.Display {

  @UiTemplate("DeriveTemporalVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveTemporalVariableStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

  @UiField
  WizardStep methodStep;

  @UiField
  WizardStep mapStep;

  @UiField
  ListBox groupBox;

  @UiField
  ValueMapGrid valuesMapGrid;

  //
  // Constructors
  //

  public DeriveTemporalVariableStepView() {
    initWidget(uiBinder.createAndBindUi(this));

    groupBox.addItem("Day of Week", GroupMethod.DAY_OF_WEEK.toString());
    groupBox.addItem("Day of Month", GroupMethod.DAY_OF_MONTH.toString());
    groupBox.addItem("Day of Year", GroupMethod.DAY_OF_YEAR.toString());
    groupBox.addItem("Week of Month", GroupMethod.WEEK_OF_MONTH.toString());
    groupBox.addItem("Week of Year", GroupMethod.WEEK_OF_YEAR.toString());
    groupBox.addItem("Month", GroupMethod.MONTH.toString());
    groupBox.addItem("Quarter", GroupMethod.QUARTER.toString());
    groupBox.addItem("Semester", GroupMethod.SEMESTER.toString());
    groupBox.addItem("Year", GroupMethod.YEAR.toString());
  }

  @Override
  public DefaultWizardStepController.Builder getMethodStepController() {
    return DefaultWizardStepController.Builder.create(methodStep).title(translations.recodeTemporalMethodStepTitle());
  }

  @Override
  public DefaultWizardStepController.Builder getMapStepController() {
    return DefaultWizardStepController.Builder.create(mapStep).title(translations.recodeTemporalMapStepTitle());
  }

  //
  // Widget Display methods
  //

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
  public String getGroupMethod() {
    return groupBox.getValue(groupBox.getSelectedIndex());
  }

  @Override
  public void populateValues(List<ValueMapEntry> valuesMap) {
    valuesMapGrid.populate(valuesMap);
  }

}
