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

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ValueSetCriterionDropdown extends CriterionDropdown {

  protected VariableDto variable;

  protected QueryResultDto queryResult;

  ValueSetCriterionDropdown(VariableDto variableDto, @Nonnull String fieldName, @Nullable QueryResultDto termDto) {
    super(fieldName);
    variable = variableDto;
    queryResult = termDto;
    updateCriterionFilter(translations.criterionFiltersMap().get("all"));
    addRadioButtons(getNoEmptyCount());
    Widget specificControls = createSpecificControls();
    if(specificControls != null) {
      add(specificControls);
    }
  }

  private int getNoEmptyCount() {
    int nb = 0;
    if(queryResult != null) {

      if(queryResult.getFacetsArray().length() > 0) {
        if(queryResult.getFacetsArray().get(0).hasStatistics()) {
          // Statistics facet
          nb += queryResult.getFacetsArray().get(0).getStatistics().getCount();
        } else {
          // Categories frequency facet
          for(int i = 0; i < queryResult.getFacetsArray().get(0).getFrequenciesArray().length(); i++) {
            nb += queryResult.getFacetsArray().get(0).getFrequenciesArray().get(i).getCount();
          }
        }
      }
    }
    return nb;
  }

  private void addRadioButtons(int noEmpty) {
    // All, Empty, Not Empty radio buttons
    RadioButton radioAll = createRadioButtonResetSpecific(translations.criterionFiltersMap().get("all"),
        queryResult == null ? null : queryResult.getTotalHits());
    radioAll.setValue(true);
    radioControls.add(radioAll);

    radioControls.add(createRadioButtonResetSpecific(translations.criterionFiltersMap().get("empty"),
        queryResult == null ? null : queryResult.getTotalHits() - noEmpty));
    radioControls.add(createRadioButtonResetSpecific(translations.criterionFiltersMap().get("not_empty"),
        queryResult == null ? null : noEmpty));
    add(radioControls);
  }

  protected void updateCriterionFilter(String filter) {
    setText(filter.isEmpty() ? variable.getName() : variable.getName() + ": " + filter);
  }

}