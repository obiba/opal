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

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.datepicker.client.ui.DateBoxAppended;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;

public abstract class DateTimeCriterionDropdown extends CriterionDropdown {

  private Chooser rangeValueChooser;

  private ControlLabel fromLabel;

  private DateBoxAppended from;

  private ControlLabel toLabel;

  private DateBoxAppended to;

  private ControlLabel dateLabel;

  private DateBoxAppended date;

  public DateTimeCriterionDropdown(VariableDto variableDto, String fieldName) {
    super(variableDto, fieldName, null);
  }

  @Override
  public Widget getSpecificControls() {
    updateRadioButtons();

    ListItem specificControls = new ListItem();
    rangeValueChooser = new Chooser();

    fromLabel = new ControlLabel(translations.criterionFiltersMap().get("from"));
    from = createDateBoxAppended();

    toLabel = new ControlLabel(translations.criterionFiltersMap().get("to"));
    to = createDateBoxAppended();

    dateLabel = new ControlLabel(translations.criterionFiltersMap().get("date"));
    date = createDateBoxAppended();

    specificControls.addStyleName("controls");
    specificControls.add(getRangeDateChooserPanel());
    specificControls.add(getRangeValuePanel());

    resetSpecificControls();
    return specificControls;
  }

  private void updateRadioButtons() {
    // Update radio controls
    RadioButton in = getRadioButton(translations.criterionFiltersMap().get("in"), null);
    in.addClickHandler(new OperatorClickHandler());
    radioControls.add(in);

    RadioButton not_in = getRadioButton(translations.criterionFiltersMap().get("not_in"), null);
    not_in.addClickHandler(new OperatorClickHandler());
    radioControls.add(not_in);
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
        setFilterText();
        doFilterValueSets();
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
    c2.addStyleName("large-dual-indent");
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

  private FlowPanel getRangeDateChooserPanel() {
    FlowPanel panel = new FlowPanel();
    rangeValueChooser.addItem(translations.criterionFiltersMap().get("range"));
    rangeValueChooser.addItem(translations.criterionFiltersMap().get("date"));
    rangeValueChooser.addChosenChangeHandler(new UpdateFilterChosenHandler());

    panel.add(rangeValueChooser);
    return panel;
  }

  @Override
  public void resetSpecificControls() {
    rangeValueChooser.setVisible(false);
    from.setVisible(false);
    fromLabel.setVisible(false);
    to.setVisible(false);
    toLabel.setVisible(false);
    dateLabel.setVisible(false);
    date.setVisible(false);
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if(emptyNotEmpty != null) return emptyNotEmpty;

    DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd");
    if(rangeValueChooser.isItemSelected(0)) {
      // RANGE
      String rangeQuery = fieldName + ":[" + (from.getValue() == null ? "*" : df.format(from.getValue())) + " TO " +
          (to.getValue() == null ? "*" : df.format(to.getValue())) + "]";

      if(((CheckBox) radioControls.getWidget(4)).getValue()) {
        return "NOT " + rangeQuery;
      }
      return rangeQuery;
    }

    // VALUES
    String valuesQuery = fieldName + ":(>=" + df.format(date.getValue()) + " AND <=" + df.format(date.getValue()) + ")";
    if(((CheckBox) radioControls.getWidget(4)).getValue()) {
      return "NOT " + valuesQuery;
    }
    return valuesQuery;

  }

  private void setFilterText() {
    String filter = variable.getName() + ": ";
    filter += ((CheckBox) radioControls.getWidget(3)).getValue()
        ? translations.criterionFiltersMap().get("in")
        : translations.criterionFiltersMap().get("not_in");

    filter += " " + rangeValueChooser.getItemText(rangeValueChooser.getSelectedIndex()).toLowerCase();

    DateTimeFormat df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
    filter += rangeValueChooser.isItemSelected(0)
        ? " [" + (from.getValue() == null ? "*" : df.format(from.getValue())) + " " +
        translations.criterionFiltersMap().get("to") + " " +
        (to.getValue() == null ? "*" : df.format(to.getValue())) + "]"
        : "(" + (date.getValue() == null ? "" : df.format(date.getValue())) + ")";

    setText(filter);
  }

  private class UpdateFilterChosenHandler implements ChosenChangeEvent.ChosenChangeHandler {
    @Override
    public void onChange(ChosenChangeEvent chosenChangeEvent) {
      updateRangeValuesFields();
      setFilterText();
      doFilterValueSets();
    }
  }

  private void updateRangeValuesFields() {
    boolean rangeSelected = rangeValueChooser.isItemSelected(0);
    fromLabel.setVisible(rangeSelected);
    from.setVisible(rangeSelected);
    toLabel.setVisible(rangeSelected);
    to.setVisible(rangeSelected);
    dateLabel.setVisible(!rangeSelected);
    date.setVisible(!rangeSelected);
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      rangeValueChooser.setVisible(true);
      updateRangeValuesFields();
      setFilterText();
    }
  }

}
