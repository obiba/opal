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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;

public class NumericalCriterionDropdown extends CriterionDropdown {

  private static final Translations translations = GWT.create(Translations.class);

  private final Chooser operatorChooser = new Chooser();

  private final Chooser rangeValueChooser = new Chooser();

  private ControlLabel minLabel;

  private final TextBox min = new TextBox();

  private ControlLabel maxLabel;

  private final TextBox max = new TextBox();

  private ControlLabel valuesLabel = new ControlLabel();

  private final TextBox values = new TextBox();

  public NumericalCriterionDropdown(VariableDto variableDto, QueryResultDto termDto) {
    super(variableDto, termDto);
  }

  @Override
  public Widget getSpecificControls() {
    ListItem specificControls = new ListItem();

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

    valuesLabel = new ControlLabel(translations.criterionFiltersMap().get("values"));

    values.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        updateRangeValuesCriterionFilter();
      }
    });

    specificControls.addStyleName("controls");
    specificControls.add(getOperatorsChooserPanel());
    specificControls.add(getRangeValuePanel());

    resetSpecificControls();
    return specificControls;
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
    panel.add(createControlGroup(maxLabel, max));
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

  private FlowPanel getOperatorsChooserPanel() {
    FlowPanel inPanel = new FlowPanel();

    operatorChooser.addStyleName("inline-block");
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
          updateRangeValuesCriterionFilter();
        }
      }
    });

    rangeValueChooser.addStyleName("small-dual-indent");
    rangeValueChooser.addItem(translations.criterionFiltersMap().get("select"));
    rangeValueChooser.addItem(translations.criterionFiltersMap().get("range"));
    rangeValueChooser.addItem(translations.criterionFiltersMap().get("values"));
    rangeValueChooser.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        resetRadioControls();
      }
    });
    rangeValueChooser.addChosenChangeHandler(new UpdateFilterChosenHandler());
    rangeValueChooser.setEnabled(false);

    inPanel.add(operatorChooser);
    inPanel.add(rangeValueChooser);
    return inPanel;
  }

  @Override
  public void resetSpecificControls() {
    operatorChooser.setItemSelected(0, true);
    rangeValueChooser.setItemSelected(0, true);
    minLabel.setVisible(false);
    min.setVisible(false);
    maxLabel.setVisible(false);
    max.setVisible(false);
    valuesLabel.setVisible(false);
    values.setVisible(false);
  }

  @Override
  public String getQueryString() {
    return "";
  }

  private void updateRangeValuesCriterionFilter() {
    String filter = operatorChooser.getItemText(operatorChooser.getSelectedIndex());

    if(rangeValueChooser.getSelectedIndex() > 0) {
      filter += " " + rangeValueChooser.getItemText(rangeValueChooser.getSelectedIndex()).toLowerCase();

      if(rangeValueChooser.isItemSelected(1)) {
        filter += "[" + (min.getText().isEmpty() ? "" : min.getText()) + ", " +
            (max.getText().isEmpty() ? "" : max.getText()) + "]";
      } else {
        filter += "(" + values.getText() + ")";
      }
    }

    updateCriterionFilter(filter);
  }

  private class UpdateFilterChosenHandler implements ChosenChangeEvent.ChosenChangeHandler {
    @Override
    public void onChange(ChosenChangeEvent chosenChangeEvent) {
      resetRadioControls();

      // Show/Hide Range-value textbox
      if(rangeValueChooser.isItemSelected(1)) {
        minLabel.setVisible(true);
        min.setVisible(true);
        maxLabel.setVisible(true);
        max.setVisible(true);
        valuesLabel.setVisible(false);
        values.setVisible(false);
      } else if(rangeValueChooser.isItemSelected(2)) {
        minLabel.setVisible(false);
        min.setVisible(false);
        maxLabel.setVisible(false);
        max.setVisible(false);
        valuesLabel.setVisible(true);
        values.setVisible(true);
      }

      updateRangeValuesCriterionFilter();
    }
  }

}
