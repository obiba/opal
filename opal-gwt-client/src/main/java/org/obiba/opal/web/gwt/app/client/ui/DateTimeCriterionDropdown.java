/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import java.util.Date;

import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.datepicker.client.ui.DateBoxAppended;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;

public abstract class DateTimeCriterionDropdown extends CriterionDropdown {

  Chooser operatorChooser;

  Chooser rangeValueChooser;

  ControlLabel fromLabel;

  DateBoxAppended from;

  ControlLabel toLabel;

  DateBoxAppended to;

  ControlLabel dateLabel;

  DateBoxAppended date;

  public DateTimeCriterionDropdown(VariableDto variableDto, String fieldName) {
    super(variableDto, fieldName, null);
  }

  @Override
  public Widget getSpecificControls() {
    ListItem specificControls = new ListItem();
    operatorChooser = new Chooser();
    rangeValueChooser = new Chooser();

    fromLabel = new ControlLabel(translations.criterionFiltersMap().get("from"));
    from = createDateBoxAppended();

    toLabel = new ControlLabel(translations.criterionFiltersMap().get("to"));
    to = createDateBoxAppended();

    dateLabel = new ControlLabel(translations.criterionFiltersMap().get("date"));
    date = createDateBoxAppended();

    specificControls.addStyleName("controls");
    specificControls.add(getOperatorsChooserPanel());
    specificControls.add(getRangeDateChooserPanel());
    specificControls.add(getRangeValuePanel());

    resetSpecificControls();
    return specificControls;
  }

  private DateBoxAppended createDateBoxAppended() {
    DateBoxAppended dateBox = new DateBoxAppended();
    dateBox.setValue(null);
    dateBox.setIcon(IconType.CALENDAR);
    dateBox.setAutoClose(true);
    dateBox.setFormat("yyyy-mm-dd");
    dateBox.setWidth("100px");
    dateBox.addStyleName("small-input");
    dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        updateDateCriterionFilter();
      }
    });

    return dateBox;
  }

  private Widget getRangeValuePanel() {
    FlowPanel panel = new FlowPanel();

    ControlGroup c = new ControlGroup();
    c.addStyleName("inline-block");
    c.add(fromLabel);
    c.add(from);

    ControlGroup c2 = new ControlGroup();
    c2.addStyleName("inline-block");
    c2.addStyleName("dual-indent");
    c2.add(toLabel);
    c2.add(to);

    ControlGroup c3 = new ControlGroup();
    c3.addStyleName("inline-block");
    c3.add(dateLabel);
    c3.add(date);

    panel.add(c);
    panel.add(c2);
    panel.add(c3);

    return panel;
  }

  private FlowPanel getOperatorsChooserPanel() {
    FlowPanel panel = new FlowPanel();
    operatorChooser.addItem(translations.criterionFiltersMap().get("select_operator"));
    operatorChooser.addItem(translations.criterionFiltersMap().get("in"));
    operatorChooser.addItem(translations.criterionFiltersMap().get("not_in"));
    operatorChooser.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        resetRadioControls();
      }
    });
    operatorChooser.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent event) {
        rangeValueChooser.setEnabled(operatorChooser.getSelectedIndex() > 0);

        if(operatorChooser.getSelectedIndex() > 0) {
          resetRadioControls();
          updateDateCriterionFilter();
        }
      }
    });

    panel.add(operatorChooser);
    return panel;
  }

  private FlowPanel getRangeDateChooserPanel() {
    FlowPanel panel = new FlowPanel();
    rangeValueChooser.addItem(translations.criterionFiltersMap().get("select"));
    rangeValueChooser.addItem(translations.criterionFiltersMap().get("range"));
    rangeValueChooser.addItem(translations.criterionFiltersMap().get("date"));
    rangeValueChooser.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        resetRadioControls();
      }
    });
    rangeValueChooser.addChosenChangeHandler(new UpdateFilterChosenHandler());
    rangeValueChooser.setEnabled(false);

    panel.add(rangeValueChooser);
    return panel;
  }

  @Override
  public void resetSpecificControls() {
    operatorChooser.setItemSelected(0, true);
    rangeValueChooser.setItemSelected(0, true);
    rangeValueChooser.setEnabled(false);
    fromLabel.setVisible(false);
    from.setVisible(false);
    toLabel.setVisible(false);
    to.setVisible(false);
    dateLabel.setVisible(false);
    date.setVisible(false);
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if(emptyNotEmpty != null) return emptyNotEmpty;

    DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd");
    if(rangeValueChooser.isItemSelected(1)) {
      // RANGE
      String rangeQuery = fieldName + ":[" + (from.getValue() == null ? "*" : df.format(from.getValue())) + " TO " +
          (to.getValue() == null ? "*" : df.format(to.getValue())) + "]";

      if(operatorChooser.isItemSelected(2)) {
        return "NOT " + rangeQuery;
      }
      return rangeQuery;
    }

    // VALUES
    String valuesQuery = fieldName + ":(>=" + df.format(date.getValue()) + " AND <=" + df.format(date.getValue()) + ")";
    if(operatorChooser.isItemSelected(2)) {
      return "NOT " + valuesQuery;
    }
    return valuesQuery;

  }

  private void updateDateCriterionFilter() {
    String filter = operatorChooser.getItemText(operatorChooser.getSelectedIndex());

    if(rangeValueChooser.getSelectedIndex() > 0) {
      filter += " " + rangeValueChooser.getItemText(rangeValueChooser.getSelectedIndex()).toLowerCase();

      DateTimeFormat df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
      filter += rangeValueChooser.isItemSelected(1)
          ? "[" + (from.getValue() == null ? "" : df.format(from.getValue())) + " TO " +
          (to.getValue() == null ? "" : df.format(to.getValue())) + "]"
          : "(" + (date.getValue() == null ? "" : df.format(date.getValue())) + ")";
    }

    updateCriterionFilter(filter);
    doFilterValueSets();
  }

  private class UpdateFilterChosenHandler implements ChosenChangeEvent.ChosenChangeHandler {
    @Override
    public void onChange(ChosenChangeEvent chosenChangeEvent) {
      resetRadioControls();

      // Show/Hide Range-value textbox
      if(rangeValueChooser.isItemSelected(1)) {
        fromLabel.setVisible(true);
        from.setVisible(true);
        toLabel.setVisible(true);
        to.setVisible(true);
        dateLabel.setVisible(false);
        date.setVisible(false);
      } else if(rangeValueChooser.isItemSelected(2)) {
        fromLabel.setVisible(false);
        from.setVisible(false);
        toLabel.setVisible(false);
        to.setVisible(false);
        dateLabel.setVisible(true);
        date.setVisible(true);
      }

      updateDateCriterionFilter();
    }
  }

}
