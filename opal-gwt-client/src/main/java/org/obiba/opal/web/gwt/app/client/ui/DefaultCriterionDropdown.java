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
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.List;

public abstract class DefaultCriterionDropdown extends ValueSetCriterionDropdown {

  private Panel valuesPanel;

  private TextBox values;

  public DefaultCriterionDropdown(String datasource, String table, VariableDto variableDto, String fieldName) {
    super(datasource, table, variableDto, fieldName, null);
  }

  public DefaultCriterionDropdown(RQLValueSetVariableCriterionParser criterion) {
    this(criterion.getDatasourceName(), criterion.getTableName(), criterion.getVariable(), criterion.getField());
    initialize(criterion);
  }

  @Override
  protected void onDropdownChange() {
    updateMatchCriteriaFilter();
  }

  @Override
  public Widget createSpecificControls() {
    ListItem specificControls = new ListItem();
    specificControls.addStyleName("controls");

    valuesPanel = new FlowPanel();
    ControlLabel valuesLabel = new ControlLabel(translations.criterionFiltersMap().get("values"));
    values = new TextBox();
    values.setText("*");
    values.addStyleName("bordered");
    values.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          updateMatchCriteriaFilter();
        }
      }
    });
    valuesPanel.add(createControlGroup(valuesLabel, values));
    updateRadioButtons();

    specificControls.add(valuesPanel);

    updateControls(false);
    return specificControls;
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

  @Override
  public void resetSpecificControls() {
    valuesPanel.setVisible(false);
    if (divider != null) {
      divider.setVisible(false);
      doFilter();
    }
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if (emptyNotEmpty != null) return emptyNotEmpty;

    if (!values.getText().isEmpty()) {
      String[] vals = values.getText().trim().split(",");
      String valuesQuery = fieldName + ":(" + Joiner.on(" OR ").join(vals) + ")";
      if (isInSelected()) {
        return valuesQuery;
      } else if (isNotInSelected()) {
        return "NOT " + valuesQuery;
      }
    }

    return null;
  }

  @Override
  public String getRQLQueryString() {
    String emptyNotEmpty = super.getRQLQueryString();
    if (emptyNotEmpty != null) return emptyNotEmpty;

    if (!values.getText().isEmpty()) {
      List<String> vals = Lists.newArrayList();
      for (String val : values.getText().trim().split("\\s+")) {
        for (String v : val.trim().split(","))
          if (!v.trim().isEmpty()) vals.add(v.trim().replace("%","%25"));
      }
      String valuesQuery = "in(" + getRQLField() + ",(" + Joiner.on(",").join(vals) + "))";
      if (isInSelected()) {
        return valuesQuery;
      } else if (isNotInSelected()) {
        return "not(" + valuesQuery + ")";
      }
    }

    return null;
  }

  @Override
  protected String getMagmaJsStatement() {
    String statement = super.getMagmaJsStatement();
    if (!Strings.isNullOrEmpty(statement)) return statement;

    statement = "$('" + variable.getName() + "')";
    if (!values.getText().isEmpty()) {
      List<String> vals = Lists.newArrayList();
      for (String val : values.getText().trim().split("\\s+")) {
        for (String v : val.trim().split(","))
          if (!v.trim().isEmpty()) vals.add(v.trim());
      }
      statement = statement + ".any('" + Joiner.on("','").join(vals) + "')";
      if (isInSelected()) return statement;
      else if (isNotInSelected()) return statement + ".not()";
    }

    return null;
  }

  private void initialize(RQLValueSetVariableCriterionParser criterion) {
    if (criterion.hasValue()) {
      ((CheckBox) radioControls.getWidget(criterion.isNot() ? 4 : 3)).setValue(true);
      values.setText(criterion.getValueString());
      valuesPanel.setVisible(true);
      divider.setVisible(true);
    } else if (criterion.isExists())
      ((CheckBox) radioControls.getWidget(criterion.isNot() ? 1 : 2)).setValue(true);
    setFilterText();
  }

  private boolean isValuesSelected() {
    return isInSelected() || isNotInSelected();
  }

  private boolean isInSelected() {
    return isCheckSelected(3);
  }

  private boolean isNotInSelected() {
    return isCheckSelected(4);
  }

  private boolean isCheckSelected(int idx) {
    return getRadioButtonValue(idx);
  }

  private void updateMatchCriteriaFilter() {
    if (isValuesSelected()) {
      if (!values.getValue().isEmpty()) {
        setFilterText();
        doFilter();
      }
    } else {
      setFilterText();
      doFilter();
    }
  }

  private void setFilterText() {
    if (isCheckSelected(1))
      updateCriterionFilter(translations.criterionFiltersMap().get("empty").toLowerCase());
    else if (isCheckSelected(2))
      updateCriterionFilter(translations.criterionFiltersMap().get("not_empty").toLowerCase());
    else if (isInSelected() || isNotInSelected()){
      String op = null;
      String value = null;
      if (isInSelected()) {
        op = translations.criterionFiltersMap().get("in").toLowerCase();
        value = "(" + values.getText() + ")";
      } else if (isNotInSelected()) {
        op = translations.criterionFiltersMap().get("not_in").toLowerCase();
        value = "(" + values.getText() + ")";
      }

      if (op == null) {
        updateCriterionFilter("");
      } else {
        updateCriterionFilter(op + " " + value);
      }
    }
    else
    updateCriterionFilter(translations.criterionFiltersMap().get("all").toLowerCase());
  }

  private void updateControls(boolean trigger) {
    valuesPanel.setVisible(isValuesSelected());
    if (divider != null) divider.setVisible(isValuesSelected());
    if (trigger) updateMatchCriteriaFilter();
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      updateControls(true);
    }
  }
}
