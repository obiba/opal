/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.github.gwtbootstrap.datepicker.client.ui.DateBoxAppended;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.Date;
import java.util.List;

public abstract class DateTimeCriterionDropdown extends ValueSetCriterionDropdown {

  private Chooser rangeValueChooser;

  private ControlGroup fromGroup;

  private DateBoxAppended from;

  private ControlGroup toGroup;

  private DateBoxAppended to;

  private ControlGroup dateGroup;

  private DateBoxAppended date;

  public DateTimeCriterionDropdown(String datasource, String table, VariableDto variableDto, String fieldName) {
    super(datasource, table, variableDto, fieldName, null);
  }

  public DateTimeCriterionDropdown(RQLValueSetVariableCriterionParser criterion) {
    this(criterion.getDatasourceName(), criterion.getTableName(), criterion.getVariable(), criterion.getField());
    initialize(criterion);
  }

  @Override
  public Widget createSpecificControls() {
    updateRadioButtons();

    ListItem specificControls = new ListItem();
    rangeValueChooser = new Chooser();

    from = createDateBoxAppended();
    to = createDateBoxAppended();
    date = createDateBoxAppended();

    specificControls.addStyleName("controls");
    specificControls.add(getRangeDateChooserPanel());
    specificControls.add(getRangeValuePanel());

    resetSpecificControls();
    return specificControls;
  }

  private void initialize(RQLValueSetVariableCriterionParser criterion) {
    if (criterion.hasWildcardValue()) {
      if (criterion.isNot()) {
        ((CheckBox) radioControls.getWidget(3)).setValue(true);
        updateRangeValuesFields();
        divider.setVisible(true);
      }
    } else if (criterion.hasValue()) {
      if (criterion.isRange()) selectRange(criterion.getValues());
      else selectValues(criterion.getValueString());
      ((CheckBox) radioControls.getWidget(criterion.isNot() ? 4 : 3)).setValue(true);
      updateRangeValuesFields();
      divider.setVisible(true);
    } else if (criterion.isExists())
      ((CheckBox) radioControls.getWidget(criterion.isNot() ? 1 : 2)).setValue(true);
    setFilterText();
  }

  private void selectRange(List<String> values) {
    rangeValueChooser.setSelectedIndex(0);
    Date minDate = null;
    Date maxDate = null;
    if (!values.isEmpty() && !"*".equals(values.get(0))) minDate = getDate(values.get(0));
    if (values.size() > 1 && !"*".equals(values.get(1))) maxDate = getDate(values.get(1));
    if (minDate != null) from.setValue(minDate);
    if (maxDate != null) to.setValue(maxDate);
  }

  private void selectValues(String valueString) {
    rangeValueChooser.setSelectedIndex(1);
    date.setValue(getDate(valueString));
  }

  private Date getDate(String dateString) {
    String nDate = dateString.trim();
    Date result = null;
    try {
      DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("yyyy-MM-dd");
      result = dateTimeFormat.parse(nDate);
    } catch (Exception e) {
      // ignore
    }
    return result;
  }

  private void updateRadioButtons() {
    // Update radio controls
    RadioButton in = createRadioButton(translations.criterionFiltersMap().get("in"), null);
    in.addClickHandler(new OperatorClickHandler());
    radioControls.add(in);

    RadioButton not_in = createRadioButton(translations.criterionFiltersMap().get("not_in"), null);
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
        doFilter();
      }
    });

    return dateBox;
  }

  private Widget getRangeValuePanel() {
    FlowPanel panel = new FlowPanel();

    ControlLabel fromLabel = new ControlLabel(translations.criterionFiltersMap().get("from"));
    ControlLabel toLabel = new ControlLabel(translations.criterionFiltersMap().get("to"));
    ControlLabel dateLabel = new ControlLabel(translations.criterionFiltersMap().get("date"));

    fromGroup = new ControlGroup();
    fromGroup.addStyleName("inline-block");
    fromGroup.add(fromLabel);
    fromGroup.add(from);

    toGroup = new ControlGroup();
    toGroup.addStyleName("inline-block");
    toGroup.addStyleName("large-dual-indent");
    toGroup.add(toLabel);
    toGroup.add(to);

    dateGroup = new ControlGroup();
    dateGroup.addStyleName("inline-block");
    dateGroup.add(dateLabel);
    dateGroup.add(date);

    panel.add(fromGroup);
    panel.add(toGroup);
    panel.add(dateGroup);

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
    fromGroup.setVisible(false);
    toGroup.setVisible(false);
    dateGroup.setVisible(false);
    if (divider != null) {
      divider.setVisible(false);
      doFilter();
    }
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if (emptyNotEmpty != null) return emptyNotEmpty;

    DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd");
    if (rangeValueChooser.isItemSelected(0)) {
      // RANGE
      String rangeQuery = fieldName + ":[" + (from.getValue() == null ? "*" : df.format(from.getValue())) + " TO " +
          (to.getValue() == null ? "*" : df.format(to.getValue())) + "]";

      if (((CheckBox) radioControls.getWidget(4)).getValue()) {
        return "NOT " + rangeQuery;
      }
      return rangeQuery;
    }

    // VALUES
    String valuesQuery = fieldName + ":(>=" + df.format(date.getValue()) + " AND <=" + df.format(date.getValue()) + ")";
    if (((CheckBox) radioControls.getWidget(4)).getValue()) {
      return "NOT " + valuesQuery;
    }
    return valuesQuery;
  }

  @Override
  public String getRQLQueryString() {
    String emptyNotEmpty = super.getRQLQueryString();
    if (emptyNotEmpty != null) return emptyNotEmpty;

    DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd");
    if (rangeValueChooser.isItemSelected(0)) {
      // RANGE
      String rangeQuery = "range(" + getRQLField() + ",(" + (from.getValue() == null ? "*" : df.format(from.getValue())) + "," +
          (to.getValue() == null ? "*" : df.format(to.getValue())) + "))";

      if (((CheckBox) radioControls.getWidget(4)).getValue()) {
        return "not(" + rangeQuery + ")";
      }
      return rangeQuery;
    }

    // VALUES
    String valuesQuery = "in(" + getRQLField() + ",(" + df.format(date.getValue()) + "))";
    if (((CheckBox) radioControls.getWidget(4)).getValue()) {
      return "not(" + valuesQuery + ")";
    }
    return valuesQuery;
  }

  @Override
  protected String getMagmaJsStatement() {
    String statement = super.getMagmaJsStatement();
    if (!Strings.isNullOrEmpty(statement)) return statement;

    String dateAccessor = "$('" + variable.getName() + "')";
    if ("datetime".equals(variable.getValueType()))
      dateAccessor = dateAccessor + ".type('date')";

    statement = dateAccessor;
    DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd");
    if(rangeValueChooser.isItemSelected(0)) {
      if (from.getValue() == null && to.getValue() == null) {
        return ((CheckBox) radioControls.getWidget(4)).getValue() ?
            statement + ".isNull()" : "";
      }
      else if (from.getValue() != null && to.getValue() != null)
        statement = statement + ".after('" + df.format(from.getValue()) + "').and(" + statement + ".before('" + df.format(to.getValue()) + "'))";
      else if (from.getValue() != null) statement = statement + ".after('" + df.format(from.getValue()) + "')";
      else if (to.getValue() != null) statement = statement + ".before('" + df.format(to.getValue()) + "')";

      if(((CheckBox) radioControls.getWidget(4)).getValue())
         statement = statement + ".not()";

      if (variable.getIsRepeatable()) {
        statement = statement.replace(dateAccessor, "v");
        statement = dateAccessor + ".any(function(v) { return " + statement + " })";
      }

      return statement;
    }

    // VALUES
    if(rangeValueChooser.isItemSelected(1) && date.getValue() != null) {
      statement = dateAccessor + ".any('" + df.format(date.getValue()) + "')";

      if(((CheckBox) radioControls.getWidget(4)).getValue()) {
        statement = statement + ".not()";
      }

      return statement;
    }

    return "";
  }

  private void setFilterText() {
    String filter = variable.getName() + " ";
    if (getRadioButtonValue(1))
      setText(filter + translations.criterionFiltersMap().get("empty").toLowerCase());
    else if (getRadioButtonValue(2))
      setText(filter + translations.criterionFiltersMap().get("not_empty").toLowerCase());
    else if (getRadioButtonValue(3)) {
      filter += translations.criterionFiltersMap().get("in").toLowerCase();
      setText(filter + getRangeOrValueFilterText());
    }
    else if (getRadioButtonValue(4)) {
      filter += translations.criterionFiltersMap().get("not_in").toLowerCase();
      setText(filter + getRangeOrValueFilterText());
    }
    else
      setText(filter + translations.criterionFiltersMap().get("all").toLowerCase());
  }

  private String getRangeOrValueFilterText() {
    DateTimeFormat df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
    return rangeValueChooser.isItemSelected(0)
        ? " [" + (from.getValue() == null ? "*" : df.format(from.getValue())) + " " +
        translations.criterionFiltersMap().get("to") + " " +
        (to.getValue() == null ? "*" : df.format(to.getValue())) + "]"
        : " (" + (date.getValue() == null ? "" : df.format(date.getValue())) + ")";
  }

  private class UpdateFilterChosenHandler implements ChosenChangeEvent.ChosenChangeHandler {
    @Override
    public void onChange(ChosenChangeEvent chosenChangeEvent) {
      updateRangeValuesFields();
      setFilterText();
      doFilter();
    }
  }

  private void updateRangeValuesFields() {
    boolean rangeSelected = rangeValueChooser.isItemSelected(0);
    rangeValueChooser.setVisible(true);
    fromGroup.setVisible(rangeSelected);
    toGroup.setVisible(rangeSelected);
    dateGroup.setVisible(!rangeSelected);
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      rangeValueChooser.setVisible(true);
      updateRangeValuesFields();
      setFilterText();
      doFilter();
    }
  }

}
