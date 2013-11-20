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

import java.util.ArrayList;
import java.util.Collection;

import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.google.common.base.Joiner;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;

public abstract class CategoricalCriterionDropdown extends CriterionDropdown {

  private Chooser operatorChooser;

  private Chooser categories;

  public CategoricalCriterionDropdown(VariableDto variableDto, String fieldName, QueryResultDto termDto) {
    super(variableDto, fieldName, termDto);
  }

  @Override
  public Widget getSpecificControls() {
    ListItem specificControls = new ListItem();

    operatorChooser = new Chooser();
    categories = new Chooser(true);

    specificControls.addStyleName("controls");

    specificControls.add(getOperatorsChooserPanel());
    specificControls.add(getCategoriesChooserPanel());

    return specificControls;
  }

  private SimplePanel getCategoriesChooserPanel() {
    SimplePanel categoriesPanel = new SimplePanel();
    for(int i = 0; i < variable.getCategoriesArray().length(); i++) {
      categories.addItem(getCategoryItem(variable.getCategoriesArray().get(i).getName()),
          variable.getCategoriesArray().get(i).getName());
    }
    categories.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        resetRadioControls();
      }
    });
    categories.addChosenChangeHandler(new UpdateFilterChosenHandler());
    categories.setVisible(false);

    categoriesPanel.add(categories);
    return categoriesPanel;
  }

  private SimplePanel getOperatorsChooserPanel() {
    SimplePanel inPanel = new SimplePanel();
    operatorChooser.addItem(translations.criterionFiltersMap().get("select_operator"));
    operatorChooser.addItem(translations.criterionFiltersMap().get("in"));
    operatorChooser.addItem(translations.criterionFiltersMap().get("not_in"));
    operatorChooser.addChosenChangeHandler(new UpdateFilterChosenHandler());

    inPanel.add(operatorChooser);
    return inPanel;
  }

  @Override
  public void resetSpecificControls() {
    operatorChooser.setItemSelected(0, true);
    categories.setVisible(false);
  }

  private String getCategoryItem(String name) {
    // Get the frequency of this category
    for(int i = 0; i < queryResult.getFacetsArray().get(0).getFrequenciesArray().length(); i++) {
      if(queryResult.getFacetsArray().get(0).getFrequenciesArray().get(i).getTerm().equals(name)) {
        return name + " (" + queryResult.getFacetsArray().get(0).getFrequenciesArray().get(i).getCount() + ")";
      }
    }

    return name;
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if(emptyNotEmpty != null) return emptyNotEmpty;

    Collection<String> selected = new ArrayList<String>();
    for(int i = 0; i < categories.getItemCount(); i++) {
      if(categories.isItemSelected(i)) {
        selected.add(categories.getValue(i));
      }
    }

    // Not in
    if(operatorChooser.isItemSelected(1)) {
      return fieldName + ":(" + Joiner.on(" OR ").join(selected) + ")";
    }

    return "NOT " + fieldName + ":(" + Joiner.on(" OR ").join(selected) + ")";
  }

  private class UpdateFilterChosenHandler implements ChosenChangeEvent.ChosenChangeHandler {
    @Override
    public void onChange(ChosenChangeEvent chosenChangeEvent) {
      Collection<String> filter = new ArrayList<String>();

      resetRadioControls();

      categories.setVisible(operatorChooser.getSelectedIndex() > 0);

      boolean update = false;
      for(int i = 0; i < categories.getItemCount(); i++) {
        if(categories.isItemSelected(i)) {
          filter.add(categories.getValue(i));
          update = true;
        }
      }

      if(update) {
        updateCriterionFilter(
            operatorChooser.getItemText(operatorChooser.getSelectedIndex()) + " (" + Joiner.on(", ").join(filter) +
                ")");

        doFilterValueSets();
      } else {
        updateCriterionFilter("");
      }
    }

  }
}
