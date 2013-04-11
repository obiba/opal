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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.user.datepicker.client.DateBox;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DeriveTemporalVariableStepView extends ViewImpl implements DeriveTemporalVariableStepPresenter.Display {

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
  Panel dates;

  @UiField
  FlowPanel from;

  @UiField
  FlowPanel to;

  private final DateBox fromDate;

  private final DateBox toDate;

  //
  // Constructors
  //

  public DeriveTemporalVariableStepView() {
    widget = uiBinder.createAndBindUi(this);

    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd");

    fromDate = new DateBox();
    fromDate.setFormat(new DateBox.DefaultFormat(dateFormat));
    Date now = new Date();
    CalendarUtil.addDaysToDate(now, -3650);
    CalendarUtil.setToFirstDayOfMonth(now);
    fromDate.setValue(now);
    fromDate.setWidth("6em");
    from.insert(fromDate, 0);

    toDate = new DateBox();
    toDate.setFormat(new DateBox.DefaultFormat(dateFormat));
    now = new Date();
    toDate.setValue(now);
    toDate.setWidth("6em");
    to.insert(toDate, 0);

    spanRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setSpanEnabled(true);
      }
    });

    rangeRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        setSpanEnabled(false);
      }
    });

    spanRadio.setValue(true, true);
  }

  private void setSpanEnabled(boolean enabled) {
    spanBox.setEnabled(enabled);
    rangeBox.setEnabled(!enabled);
    fromDate.setEnabled(!enabled);
    toDate.setEnabled(!enabled);
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
