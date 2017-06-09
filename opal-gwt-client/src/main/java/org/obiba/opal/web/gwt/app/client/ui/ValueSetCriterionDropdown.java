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

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ValueSetCriterionDropdown extends CriterionDropdown {

  protected final String datasource;

  protected final String table;

  protected final VariableDto variable;

  protected final QueryResultDto queryResult;

  protected ListItem divider;

  ValueSetCriterionDropdown(String datasource, String table, VariableDto variableDto, @Nonnull String fieldName, @Nullable QueryResultDto termDto) {
    super(fieldName);
    this.datasource = datasource;
    this.table = table;
    variable = variableDto;
    queryResult = termDto;
    initialize();
  }

  public String getDatasource() {
    return datasource;
  }

  public String getTable() {
    return table;
  }

  public VariableDto getVariable() {
    return variable;
  }

  private void initialize() {
    initializeHeader();
    updateCriterionFilter(translations.criterionFiltersMap().get("all"));
    initializeRadioControls(getNoEmptyCount());
    Widget specificControls = createSpecificControls();
    if(specificControls != null) {
      divider = new ListItem();
      divider.addStyleName("divider");
      divider.setVisible(false);
      add(divider);
      add(specificControls);
    }
  }

  private void initializeHeader() {
    ListItem header = new ListItem();
    header.addStyleName("controls");
    header.setTitle(getHeaderTitle());
    Label label = new InlineLabel(getHeaderText());
    header.add(label);
    if (!header.getTitle().isEmpty()) {
      Icon info = new Icon(IconType.INFO_SIGN);
      info.addStyleName("small-indent");
      header.add(info);
    }
    add(header);
    ListItem headerDivider = new ListItem();
    headerDivider.addStyleName("divider");
    add(headerDivider);
  }

  protected String getHeaderText() {
    return variable.getName();
  }

  protected String getHeaderTitle() {
    return getVariableLabel();
  }

  private String getVariableLabel() {
    if (variable == null) return "";
    List<AttributeDto> labels = Lists.newArrayList();
    for (AttributeDto attribute : JsArrays.toList(variable.getAttributesArray())) {
      if (attribute.getName().equals("label")) labels.add(attribute);
    }
    if (!labels.isEmpty()) {
      // TODO get correct locale
      return labels.get(0).getValue();
    }
    return "";
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

  private void initializeRadioControls(int noEmpty) {
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