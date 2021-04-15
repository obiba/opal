/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive;

import java.util.Date;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.derive.helper.TemporalVariableDerivationHelper.GroupMethod;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController;

import com.github.gwtbootstrap.client.ui.Column;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.datepicker.client.ui.DateBoxAppended;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 *
 */
public class DeriveTemporalVariableStepView extends ViewWithUiHandlers<DerivationUiHandlers>
    implements DeriveTemporalVariableStepPresenter.Display {

  @UiTemplate("DeriveTemporalVariableStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveTemporalVariableStepView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardStep methodStep;

  @UiField
  WizardStep mapStep;

  @UiField
  RadioButton spanRadio;

  @UiField
  ListBox spanBox;

  @UiField
  RadioButton rangeRadio;

  @UiField
  ListBox rangeBox;

  @UiField
  ValueMapGrid valuesMapGrid;

  @UiField
  DateBoxAppended fromDate;

  @UiField
  DateBoxAppended toDate;

  @UiField
  Column dateRangeColumn;

  //
  // Constructors
  //

  public DeriveTemporalVariableStepView() {
    widget = uiBinder.createAndBindUi(this);

    fromDate.setFormat("yyyy-mm-dd");
    Date now = new Date();
    CalendarUtil.addDaysToDate(now, -3650);
    CalendarUtil.setToFirstDayOfMonth(now);
    fromDate.setValue(now);
    fromDate.setWidth("6em");

    toDate.setFormat("yyyy-mm-dd");
    now = new Date();
    toDate.setValue(now);
    toDate.setWidth("6em");
    spanRadio.setValue(true, true);
  }

  private void setSpanEnabled(boolean enabled) {
    spanBox.setEnabled(enabled);
    rangeBox.setEnabled(!enabled);
    dateRangeColumn.setVisible(!enabled);
  }

  @UiHandler({ "spanBox", "rangeBox" })
  void onMethodChanged(ChangeEvent event) {
    getUiHandlers().onMethodChange();
  }

  @UiHandler({ "fromDate", "toDate" })
  void onDateChanged(ValueChangeEvent<Date> event) {
    getUiHandlers().onMethodChange();
  }

  @UiHandler({ "spanRadio", "rangeRadio" })
  void onSpanClick(ClickEvent event) {
    setSpanEnabled(spanRadio.getValue());
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
    return widget;
  }

  @Override
  public String getGroupMethod() {
    return spanRadio.getValue()
        ? spanBox.getValue(spanBox.getSelectedIndex())
        : rangeBox.getValue(rangeBox.getSelectedIndex());
  }

  @Override
  public void populateValues(List<ValueMapEntry> valuesMap, List<String> derivedCategories) {
    valuesMapGrid.populate(valuesMap, derivedCategories);
  }

  @Override
  public Date getFromDate() {
    return fromDate.getValue();
  }

  @Override
  public Date getToDate() {
    return toDate.getValue();
  }

  @Override
  public boolean spanSelected() {
    return spanRadio.getValue();
  }

  @Override
  public boolean rangeSelected() {
    return rangeRadio.getValue();
  }

  @Override
  public void setTimeType(String valueType) {
    spanBox.clear();
    rangeBox.clear();
    for(GroupMethod method : GroupMethod.values()) {
      if(method.isForTimeType(valueType)) {
        if(method.isTimeSpan()) {
          spanBox.addItem(translations.timeGroupMap().get(method.toString()), method.toString());
        } else {
          rangeBox.addItem(translations.timeGroupMap().get(method.toString()), method.toString());
        }
      }
    }
  }

}
