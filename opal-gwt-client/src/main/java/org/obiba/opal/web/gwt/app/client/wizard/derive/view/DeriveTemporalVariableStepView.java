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

import java.util.Date;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.helper.TemporalVariableDerivationHelper.GroupMethod;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveTemporalVariableStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

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

  @UiField
  FlowPanel dates;

  private DateBox fromDate;

  private DateBox toDate;

  //
  // Constructors
  //

  public DeriveTemporalVariableStepView() {
    initWidget(uiBinder.createAndBindUi(this));

    for(GroupMethod method : GroupMethod.values()) {
      // TODO translate
      groupBox.addItem(translations.timeGroupMap().get(method.toString()), method.toString());
    }
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd");

    this.fromDate = new DateBox();
    this.fromDate.setFormat(new DateBox.DefaultFormat(dateFormat));
    fromDate.setValue(new Date());
    dates.insert(fromDate, 1);

    this.toDate = new DateBox();
    this.toDate.setFormat(new DateBox.DefaultFormat(dateFormat));
    toDate.setValue(new Date());
    dates.insert(toDate, 3);

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

  @Override
  public Date getFromDate() {
    return fromDate.getValue();
    // return Long.parseLong(DateTimeFormat.getFormat(PredefinedFormat.YEAR).format(fromDate.getValue()));
  }

  @Override
  public Date getToDate() {
    return toDate.getValue();
    // return Long.parseLong(DateTimeFormat.getFormat(PredefinedFormat.YEAR).format(toDate.getValue()));
  }

}
