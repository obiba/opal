/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.common.collect.Lists;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Joiner;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;

import java.util.List;

public abstract class NumericalCriterionDropdown extends ValueSetCriterionDropdown {

  private Chooser rangeValueChooser;

  private ControlLabel minLabel;

  private TextBox min;

  private ControlLabel maxLabel;

  private TextBox max;

  private ControlLabel valuesLabel;

  private TextBox values;

  public NumericalCriterionDropdown(String datasource, String table, VariableDto variableDto, String fieldName, QueryResultDto termDto) {
    super(datasource, table, variableDto, fieldName, termDto);
  }

  public NumericalCriterionDropdown(RQLValueSetVariableCriterionParser criterion, QueryResultDto termDto) {
    this(criterion.getDatasourceName(), criterion.getTableName(), criterion.getVariable(), criterion.getField(), termDto);
    initialize(criterion);
  }

  @Override
  public Widget createSpecificControls() {
    updateRadioButtons();

    rangeValueChooser = new Chooser();
    min = new TextBox();
    min.addStyleName("bordered");
    max = new TextBox();
    max.addStyleName("bordered small-indent");
    valuesLabel = new ControlLabel();
    values = new TextBox();
    values.setText("*");
    values.addStyleName("bordered");

    initMinMaxControls();
    initValuesControls();

    ListItem specificControls = new ListItem();
    specificControls.addStyleName("controls");

    specificControls.add(getRangeValuesChooserPanel());
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
    }
    else if (criterion.hasValue()) {
      if (criterion.isRange()) selectRange(criterion.getValues());
      else selectValues(criterion.getValueString());
      ((CheckBox)radioControls.getWidget(criterion.isNot() ? 4 : 3)).setValue(true);
      updateRangeValuesFields();
      divider.setVisible(true);
    }
    else if (criterion.isExists())
      ((CheckBox)radioControls.getWidget(criterion.isNot() ? 1 : 2)).setValue(true);
    setFilterText();
  }

  private void selectRange(List<String> values) {
    rangeValueChooser.setSelectedIndex(0);
    if (!values.isEmpty() && !"*".equals(values.get(0))) min.setText(values.get(0));
    if (values.size()>1 && !"*".equals(values.get(1))) max.setText(values.get(1));
  }

  private void selectValues(String valueString) {
    rangeValueChooser.setSelectedIndex(1);
    values.setText(valueString);
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

  private void initValuesControls() {
    valuesLabel = new ControlLabel(translations.criterionFiltersMap().get("values"));
    values.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (values.getText().isEmpty()) return;
        updateRangeValuesCriterionFilter();
      }
    });
  }

  private void initMinMaxControls() {
    minLabel = new ControlLabel(translations.criterionFiltersMap().get("min"));
    minLabel.setFor(min.getId());
    min.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        updateRangeValuesCriterionFilter();
      }
    });

    maxLabel = new ControlLabel(translations.criterionFiltersMap().get("max"));
    maxLabel.addStyleName("small-indent");
    maxLabel.setFor(max.getId());
    max.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        updateRangeValuesCriterionFilter();
      }
    });
  }

  private Widget getRangeValuePanel() {
    FlowPanel panel = new FlowPanel();

    // TODO: Round digit
    min.setPlaceholder(">= " + queryResult.getFacetsArray().get(0).getStatistics().getMin());
    min.setWidth("100px");

    // TODO: Round digit
    max.setPlaceholder("<= " + queryResult.getFacetsArray().get(0).getStatistics().getMax());
    max.setWidth("100px");

    panel.add(createControlGroup(minLabel, min));
    ControlGroup maxGroup = createControlGroup(maxLabel, max);
    panel.add(maxGroup);
    panel.add(createControlGroup(valuesLabel, values));

    return panel;
  }

  private FlowPanel getRangeValuesChooserPanel() {
    FlowPanel panel = new FlowPanel();

    rangeValueChooser.addItem(translations.criterionFiltersMap().get("range"));
    rangeValueChooser.addItem(translations.criterionFiltersMap().get("values"));
    rangeValueChooser.addChosenChangeHandler(new UpdateFilterChosenHandler());
    rangeValueChooser.setVisible(false);

    panel.add(rangeValueChooser);
    return panel;
  }

  @Override
  public void resetSpecificControls() {
    rangeValueChooser.setVisible(false);
    minLabel.setVisible(false);
    min.setVisible(false);
    maxLabel.setVisible(false);
    max.setVisible(false);
    valuesLabel.setVisible(false);
    values.setVisible(false);
    if (divider != null) {
      divider.setVisible(false);
      doFilter();
    }
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if(emptyNotEmpty != null) return emptyNotEmpty;

    // RANGE
    if(rangeValueChooser.isItemSelected(0)) {
      String rangeQuery = fieldName + ":[" + (min.getText().isEmpty() ? "*" : min.getText()) + " TO " +
          (max.getText().isEmpty() ? "*" : max.getText()) + "]";

      if(((CheckBox) radioControls.getWidget(4)).getValue()) {
        return "NOT " + rangeQuery;
      }

      return rangeQuery;
    }

    // VALUES
    if(rangeValueChooser.isItemSelected(1) && !values.getText().isEmpty()) {
      // Parse numbers
      String[] numbers = values.getText().trim().split(",");
      String valuesQuery = fieldName + ":(" + Joiner.on(" OR ").join(numbers) + ")";

      if(((CheckBox) radioControls.getWidget(4)).getValue()) {
        return "NOT " + valuesQuery;
      }

      return valuesQuery;
    }

    return "";
  }

  @Override
  public String getRQLQueryString() {
    String emptyNotEmpty = super.getRQLQueryString();
    if(emptyNotEmpty != null) return emptyNotEmpty;

    String rqlField = getRQLField();
    // RANGE
    if(rangeValueChooser.isItemSelected(0)) {
      String rangeQuery = "range(" + rqlField + ",(" + (min.getText().isEmpty() ? "*" : min.getText()) + "," +
          (max.getText().isEmpty() ? "*" : max.getText()) + "))";

      if(((CheckBox) radioControls.getWidget(4)).getValue()) {
        return "not(" + rangeQuery + ")";
      }

      return rangeQuery;
    }

    // VALUES
    if(rangeValueChooser.isItemSelected(1) && !values.getText().isEmpty()) {
      // Parse numbers
      List<String> numbers = Lists.newArrayList();
      for (String nb : values.getText().trim().split("\\s+")) {
        if (!nb.trim().isEmpty()) numbers.add(nb);
      }
      String valuesQuery = "in(" + rqlField + ",(" + Joiner.on(",").join(numbers) + "))";

      if(((CheckBox) radioControls.getWidget(4)).getValue()) {
        return "not(" + valuesQuery + ")";
      }

      return valuesQuery;
    }

    return "";
  }

  private void updateRangeValuesCriterionFilter() {
    setFilterText();
    doFilter();
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
    return rangeValueChooser.isItemSelected(0) ? " [" + (min.getText().isEmpty() ? "*" : min.getText()) + " " +
        translations.criterionFiltersMap().get("to").toLowerCase() + " " +
        (max.getText().isEmpty() ? "*" : max.getText()) + "]" : " (" + values.getText() + ")";
  }

  private class UpdateFilterChosenHandler implements ChosenChangeEvent.ChosenChangeHandler {
    @Override
    public void onChange(ChosenChangeEvent chosenChangeEvent) {
      updateRangeValuesFields();
      updateRangeValuesCriterionFilter();
    }
  }

  private void updateRangeValuesFields() {
    boolean rangeSelected = rangeValueChooser.isItemSelected(0);
    rangeValueChooser.setVisible(true);
    minLabel.setVisible(rangeSelected);
    min.setVisible(rangeSelected);
    maxLabel.setVisible(rangeSelected);
    max.setVisible(rangeSelected);
    valuesLabel.setVisible(!rangeSelected);
    values.setVisible(!rangeSelected);
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      divider.setVisible(true);
      rangeValueChooser.setVisible(true);
      updateRangeValuesFields();
      setFilterText();
      doFilter();
    }
  }
}

