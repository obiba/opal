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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;

public class CategoricalCriterionDropdown extends CriterionDropdown {

  private static final Translations translations = GWT.create(Translations.class);

  private final Chooser operatorChooser = new Chooser();

  private final Chooser categories = new Chooser(true);

  private final TextBox matches = new TextBox();

  public CategoricalCriterionDropdown(VariableDto variableDto, QueryResultDto termDto) {
    super(variableDto, termDto);
  }

  @Override
  public Widget getSpecificControls() {
    ListItem specificControls = new ListItem();

    specificControls.addStyleName("controls");

    specificControls.add(getOperatorsChooserPanel());
    specificControls.add(getCategoriesChooserPanel());

    matches.setPlaceholder(translations.criterionFiltersMap().get("custom_match_query"));
    matches.setVisible(false);
    matches.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        resetRadioControls();
      }
    });
    matches.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        updateMatchCriteriaFilter();
      }
    });

    specificControls.add(matches);
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
    operatorChooser.addItem(translations.criterionFiltersMap().get("like"));
    operatorChooser.addChosenChangeHandler(new UpdateFilterChosenHandler());

    inPanel.add(operatorChooser);
    return inPanel;
  }

  @Override
  public void resetSpecificControls() {
    operatorChooser.setItemSelected(0, true);
    matches.setVisible(false);
    categories.setVisible(false);
  }

  private String getCategoryItem(String name) {
    // Get the frequency of this category
    // TODO: Validate that for 1 variable there is only 1 facet
    for(int i = 0; i < queryResult.getFacetsArray().get(0).getFrequenciesArray().length(); i++) {
      if(queryResult.getFacetsArray().get(0).getFrequenciesArray().get(i).getTerm().equals(name)) {
        return name + " (" + queryResult.getFacetsArray().get(0).getFrequenciesArray().get(i).getCount() + ")";
      }
    }

    return name;

  }

  @Override
  public String getQueryString() {
    return "";
  }

  private class UpdateFilterChosenHandler implements ChosenChangeEvent.ChosenChangeHandler {
    @Override
    public void onChange(ChosenChangeEvent chosenChangeEvent) {
      Collection<String> filter = new ArrayList<String>();

      resetRadioControls();

      matches.setVisible(operatorChooser.isItemSelected(3));
      categories.setVisible(operatorChooser.getSelectedIndex() > 0 && !operatorChooser.isItemSelected(3));

      // If MATCHES is selected, hide chooser of categories and show a simple textbox
      if(operatorChooser.isItemSelected(3)) {
        updateMatchCriteriaFilter();

      } else {
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
        } else {
          updateCriterionFilter("");
        }
      }
    }
  }

  private void updateMatchCriteriaFilter() {
    if(matches.getText().isEmpty()) {
      updateCriterionFilter("");
    } else {
      updateCriterionFilter(translations.criterionFiltersMap().get("like") + " " + matches.getText());
    }
  }
}
