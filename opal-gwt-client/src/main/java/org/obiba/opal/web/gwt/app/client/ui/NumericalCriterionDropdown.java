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

public abstract class NumericalCriterionDropdown extends CriterionDropdown {

  private Chooser rangeValueChooser;

  private ControlLabel minLabel;

  private TextBox min;

  private ControlLabel maxLabel;

  private TextBox max;

  private ControlLabel valuesLabel;

  private TextBox values;

  public NumericalCriterionDropdown(VariableDto variableDto, String fieldName, QueryResultDto termDto) {
    super(variableDto, fieldName, termDto);
  }

  @Override
  public Widget getSpecificControls() {
    updateRadioButtons();

    rangeValueChooser = new Chooser();
    min = new TextBox();
    min.addStyleName("bordered");
    max = new TextBox();
    max.addStyleName("bordered");
    valuesLabel = new ControlLabel();
    values = new TextBox();

    initMinMaxControls();
    initValuesControls();

    ListItem specificControls = new ListItem();
    specificControls.addStyleName("controls");

    specificControls.add(getRangeValuesChooserPanel());
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

  private void initValuesControls() {
    valuesLabel = new ControlLabel(translations.criterionFiltersMap().get("values"));
    values.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
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
    maxGroup.addStyleName("small-indent");
    panel.add(maxGroup);
    panel.add(createControlGroup(valuesLabel, values));

    return panel;
  }

  private ControlGroup createControlGroup(ControlLabel label, TextBox textBox) {
    ControlGroup c = new ControlGroup();
    c.addStyleName("inline-block");
    c.add(label);
    c.add(textBox);
    return c;
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
    if(rangeValueChooser.isItemSelected(1) && values.getText().length() > 0) {
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

  private void updateRangeValuesCriterionFilter() {
    setFilterText();
    doFilterValueSets();
  }

  private void setFilterText() {
    String filter = variable.getName() + ": ";
    filter += ((CheckBox) radioControls.getWidget(3)).getValue()
        ? translations.criterionFiltersMap().get("in")
        : translations.criterionFiltersMap().get("not_in");

    filter += " " + rangeValueChooser.getItemText(rangeValueChooser.getSelectedIndex()).toLowerCase();

    filter += rangeValueChooser.isItemSelected(0) ? " [" + (min.getText().isEmpty() ? "*" : min.getText()) + " " +
        translations.criterionFiltersMap().get("to") + " " +
        (max.getText().isEmpty() ? "*" : max.getText()) + "]" : "(" + values.getText() + ")";

    setText(filter);
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
      rangeValueChooser.setVisible(true);
      updateRangeValuesFields();
      setFilterText();
    }
  }
}

