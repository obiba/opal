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

import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;

public abstract class DefaultCriterionDropdown extends CriterionDropdown {

  private Chooser operatorChooser;

  private TextBox matches;

  private HelpBlock matchesHelp;

  public DefaultCriterionDropdown(VariableDto variableDto, String fieldName) {
    super(variableDto, fieldName, null);
  }

  @Override
  public Widget getSpecificControls() {
    ListItem specificControls = new ListItem();

    operatorChooser = new Chooser();
    matches = new TextBox();
    matchesHelp = new HelpBlock(translations.criterionFiltersMap().get("wildcards_help"));
    specificControls.addStyleName("controls");

    specificControls.add(getOperatorsChooserPanel());

    matches.setPlaceholder(translations.criterionFiltersMap().get("custom_match_query"));
    matches.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        updateMatchCriteriaFilter();
      }
    });
    matches.setVisible(false);
    matchesHelp.setVisible(false);

    specificControls.add(matches);
    specificControls.add(matchesHelp);
    return specificControls;
  }

  private SimplePanel getOperatorsChooserPanel() {
    SimplePanel inPanel = new SimplePanel();
    operatorChooser.addItem(translations.criterionFiltersMap().get("select_operator"));
    operatorChooser.addItem(translations.criterionFiltersMap().get("like"));
    operatorChooser.addItem(translations.criterionFiltersMap().get("not_like"));
    operatorChooser.addChosenChangeHandler(new UpdateFilterChosenHandler());

    inPanel.add(operatorChooser);
    return inPanel;
  }

  @Override
  public void resetSpecificControls() {
    operatorChooser.setItemSelected(0, true);
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if(emptyNotEmpty != null) return emptyNotEmpty;

    if(operatorChooser.isItemSelected(1)) {
      return fieldName + ":" + matches.getText();
    } else if(operatorChooser.isItemSelected(2)) {
      return "NOT " + fieldName + ":" + matches.getText();
    }

    return fieldName + ":*";
  }

  private class UpdateFilterChosenHandler implements ChosenChangeEvent.ChosenChangeHandler {
    @Override
    public void onChange(ChosenChangeEvent chosenChangeEvent) {
      resetRadioControls();
      updateMatchCriteriaFilter();
    }
  }

  private void updateMatchCriteriaFilter() {
    boolean isOperatorSelected = operatorChooser.getSelectedIndex() > 0;

    matches.setVisible(isOperatorSelected);
    matchesHelp.setVisible(isOperatorSelected);

    if(isOperatorSelected) {
      if(matches.getText().isEmpty()) {
        updateCriterionFilter("");
      } else {

        String prefix = operatorChooser.isItemSelected(1)
            ? translations.criterionFiltersMap().get("like") + " "
            : translations.criterionFiltersMap().get("not_like") + " ";
        updateCriterionFilter(prefix + matches.getText());
        doFilterValueSets();
      }
    }
  }
}
