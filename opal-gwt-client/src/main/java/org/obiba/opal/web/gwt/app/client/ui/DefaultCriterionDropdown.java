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

import com.github.gwtbootstrap.client.ui.*;
import com.google.common.base.Joiner;
import com.google.gwt.user.client.ui.FlowPanel;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Widget;

public abstract class DefaultCriterionDropdown extends CriterionDropdown {

  private TextBox matches;

  private HelpBlock matchesHelp;

  private ControlLabel valuesLabel;
  private TextBox values;

  public DefaultCriterionDropdown(VariableDto variableDto, String fieldName) {
    super(variableDto, fieldName, null);
  }

  @Override
  public Widget getSpecificControls() {

    valuesLabel = new ControlLabel();
    values = new TextBox();

    initValuesControls();
    updateRadioButtons();

    ListItem specificControls = new ListItem();
    matches = new TextBox();
    matchesHelp = new HelpBlock(translations.criterionFiltersMap().get("wildcards_help"));
    specificControls.addStyleName("controls");

    matches.setPlaceholder(translations.criterionFiltersMap().get("custom_match_query"));
    matches.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        updateMatchCriteriaFilter();
      }
    });
    matches.setVisible(false);
    matchesHelp.setVisible(false);

    specificControls.add(getValuesPanel());

    specificControls.add(matches);
    specificControls.add(matchesHelp);

      updateControls();
    return specificControls;
  }

    private void updateRadioButtons() {
        // Update radio controls
        RadioButton like = getRadioButton(translations.criterionFiltersMap().get("like"), null);
        like.addClickHandler(new OperatorClickHandler());
        radioControls.add(like);

        RadioButton not_like = getRadioButton(translations.criterionFiltersMap().get("not_like"), null);
        not_like.addClickHandler(new OperatorClickHandler());
        radioControls.add(not_like);

        RadioButton in = getRadioButton(translations.criterionFiltersMap().get("in"), null);
        in.addClickHandler(new OperatorClickHandler());
        radioControls.add(in);

        RadioButton not_in = getRadioButton(translations.criterionFiltersMap().get("not_in"), null);
        not_in.addClickHandler(new OperatorClickHandler());
        radioControls.add(not_in);
    }

  @Override
  public void resetSpecificControls() {
    matches.setVisible(false);
    matchesHelp.setVisible(false);
    valuesLabel.setVisible(false);
    values.setVisible(false);
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if(emptyNotEmpty != null) return emptyNotEmpty;

    if (isLikeSelected() && !matches.getText().isEmpty()) {
      return fieldName + ":" + matches.getText();
    }

    if (isNotLikeSelected() && !matches.getText().isEmpty()) {
      return "NOT " + fieldName + ":" + matches.getText();
    }

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

  private boolean isLikeSelected() {
      return isCheckSelected(3);
  }

  private boolean isNotLikeSelected() {
      return isCheckSelected(4);
  }

  private boolean isInSelected() {
      return isCheckSelected(5);
  }

  private boolean isNotInSelected() {
      return isCheckSelected(6);
  }

  private boolean isCheckSelected(int idx) {
      return ((CheckBox) radioControls.getWidget(idx)).getValue();
  }

  private void updateMatchCriteriaFilter() {
    setFilterText();
    doFilterValueSets();
  }

  private void setFilterText() {

      String op = null;
      String value = null;
      if (isLikeSelected()) {
          op = translations.criterionFiltersMap().get("like");
          value = matches.getText();
      } else if (isNotLikeSelected()) {
          op = translations.criterionFiltersMap().get("not_like");
          value = matches.getText();
      } else if (isInSelected()) {
          op = translations.criterionFiltersMap().get("in");
          value = "(" + values.getText() + ")";
      } else if (isNotInSelected()) {
          op = translations.criterionFiltersMap().get("not_in");
          value = "(" + values.getText() + ")";
      }

      if (op == null) {
          updateCriterionFilter("");
      } else {
          updateCriterionFilter(op + " " + value);
      }
  }

    private void initValuesControls() {
        valuesLabel = new ControlLabel(translations.criterionFiltersMap().get("values"));
        values.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateMatchCriteriaFilter();
            }
        });
    }

    private Widget getValuesPanel() {
        FlowPanel panel = new FlowPanel();

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

    private void updateControls() {
        boolean likeBased = isLikeSelected() || isNotLikeSelected();
        boolean inBased = isInSelected() || isNotInSelected();
        matches.setVisible(likeBased);
        matchesHelp.setVisible(likeBased);
        valuesLabel.setVisible(inBased);
        values.setVisible(inBased);

        setFilterText();
    }

    private class OperatorClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            updateControls();
        }
    }
}
