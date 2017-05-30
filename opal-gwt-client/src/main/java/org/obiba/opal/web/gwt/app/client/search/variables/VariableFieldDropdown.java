/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.variables;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.ui.CriterionDropdown;
import org.obiba.opal.web.gwt.app.client.ui.ListItem;

public class VariableFieldDropdown extends CriterionDropdown {

  private TextBox matches;

  public VariableFieldDropdown(VariableFieldSuggestOracle.VariableFieldSuggestion suggestion) {
    super(suggestion.getReplacementString());
    setText(extractValue(suggestion.getReplacementString()));
    initialize(suggestion.getReplacementString());
  }

  public VariableFieldDropdown(String field) {
    super(field);
    setText(extractValue(field));
    initialize(field);
  }

  @Override
  protected Widget getSpecificControls() {
    ListItem specificControls = new ListItem();
    specificControls.addStyleName("controls");
    matches = new TextBox();
    matches.addStyleName("bordered");
    matches.setPlaceholder(translations.criterionFiltersMap().get("custom_match_query"));
    matches.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        //updateMatchCriteriaFilter();
      }
    });
    matches.setVisible(false);

    specificControls.add(matches);
    return specificControls;
  }

  protected String getSpecificQueryString() {
    if (matches.getText().isEmpty()) return null;
    // In
    if(getCheckBox(2).getValue()) return fieldName + ":" + matches.getText();
    // Not in
    if(getCheckBox(3).getValue()) return "NOT " + fieldName + ":" + matches.getText();

    return null;
  }

  @Override
  protected void resetSpecificControls() {
    matches.setVisible(false);
    matches.setText("");
  }

  @Override
  public void doFilter() {

  }

  @Override
  public String getQueryString() {
    // Any
    if(getCheckBox(0).getValue()) return "_exists_:" + fieldName;
    // None
    if(getCheckBox(1).getValue()) return "NOT _exists_:" + fieldName;
    return getSpecificQueryString();
  }

  @Override
  protected void updateCriterionFilter(String filter) {
    super.updateCriterionFilter(filter + " " + matches.getText());
  }

  private void initialize(String fieldQuery) {
    //updateCriterionFilter(translations.criterionFiltersMap().get("all"));
    addRadioButtons(fieldQuery);
    Widget specificControls = getSpecificControls();
    if(specificControls != null) {
      add(specificControls);
    }
    matches.setText(extractValue(fieldQuery));
    matches.setVisible(!matches.getText().isEmpty());
  }

  private void addRadioButtons(String fieldQuery) {
    String value = extractValue(fieldQuery);
    radioControls.add(getRadioButtonResetSpecific(translations.criterionFiltersMap().get("any"), null));
    radioControls.add(getRadioButtonResetSpecific(translations.criterionFiltersMap().get("none"), null));
    RadioButton in = getRadioButton(translations.criterionFiltersMap().get("in"), null);
    in.addClickHandler(new OperatorClickHandler());
    radioControls.add(in);

    RadioButton not_in = getRadioButton(translations.criterionFiltersMap().get("not_in"), null);
    not_in.addClickHandler(new OperatorClickHandler());
    radioControls.add(not_in);
    if (value.isEmpty()) getCheckBox(0).setValue(true);
    else in.setValue(true);
    add(radioControls);
  }

  private CheckBox getCheckBox(int index) {
    return (CheckBox) radioControls.getWidget(index);
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      matches.setVisible(true);
    }
  }
}
