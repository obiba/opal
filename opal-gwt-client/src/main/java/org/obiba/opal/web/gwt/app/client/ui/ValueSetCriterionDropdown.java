/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ValueSetCriterionDropdown extends CriterionDropdown {

  protected final String datasource;

  protected final String table;

  protected final VariableDto variable;

  protected final QueryResultDto facetDto;

  protected ListItem divider;

  ValueSetCriterionDropdown(String datasource, String table, VariableDto variableDto, String fieldName, @Nullable QueryResultDto facetDto) {
    super(fieldName);
    this.datasource = datasource;
    this.table = table;
    variable = variableDto;
    this.facetDto = facetDto;
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
    updateCriterionFilter(translations.criterionFiltersMap().get("all").toLowerCase());
    initializeRadioControls(getNoEmptyCount());
    Widget specificControls = createSpecificControls();
    if (specificControls != null) {
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
    FlowPanel namePanel = new FlowPanel();
    header.add(namePanel);
    Label label = new InlineLabel(getHeaderText());
    namePanel.add(label);
    if (!header.getTitle().isEmpty()) {
      Icon info = new Icon(IconType.INFO_SIGN);
      info.addStyleName("small-indent");
      namePanel.add(info);
    }
    String subtitle = getHeaderSubTitle();
    if (!Strings.isNullOrEmpty(subtitle)) {
      FlowPanel tablePanel = new FlowPanel();
      Label tableLabel = new Label(subtitle);
      tableLabel.addStyleName("italic");
      tablePanel.add(tableLabel);
      header.add(tablePanel);
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

  protected String getHeaderSubTitle() {
    if (!Strings.isNullOrEmpty(datasource) && !Strings.isNullOrEmpty(table)) {
      return datasource + " - " + table;
    }
    return "";
  }

  @Override
  protected void onDropdownChange() {
  }

  @Override
  protected String getRQLField() {
    return datasource + "." + table + ":" + variable.getName();
  }

  protected String getMagmaJsStatement() {
    String statement = "$('" + variable.getName() + "')";
    if (getRadioButtonValue(1)) {
      // Empty
      return statement + ".isNull()";
    }
    if (getRadioButtonValue(2)) {
      // Not empty
      return statement + ".isNull().not()";
    }
    if (getRadioButtonValue(0)) {
      // All: No filter is necessary
      return "";
    }
    return "";
  }


  protected ControlGroup createControlGroup(ControlLabel label, TextBox textBox) {
    ControlGroup c = new ControlGroup();
    c.addStyleName("inline-block");
    c.add(label);
    c.add(textBox);
    return c;
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
    if (facetDto == null || facetDto.getFacetsArray().length() == 0) return nb;

    if (facetDto.getFacetsArray().get(0).hasStatistics()) {
      // Statistics facet
      nb += facetDto.getFacetsArray().get(0).getStatistics().getCount();
    } else if (facetDto.getFacetsArray().get(0).getFrequenciesArray() != null) {
      // Categories frequency facet
      for (int i = 0; i < facetDto.getFacetsArray().get(0).getFrequenciesArray().length(); i++) {
        nb += facetDto.getFacetsArray().get(0).getFrequenciesArray().get(i).getCount();
      }
    }
    return nb;
  }

  private void initializeRadioControls(int noEmpty) {
    // All, Empty, Not Empty radio buttons
    RadioButton radioAll = createRadioButtonResetSpecific(translations.criterionFiltersMap().get("all"),
      facetDto == null || variable.getIsRepeatable() ? null : facetDto.getTotalHits());
    radioAll.setValue(true);
    radioControls.add(radioAll);

    radioControls.add(createRadioButtonResetSpecific(translations.criterionFiltersMap().get("empty"),
      facetDto == null || variable.getIsRepeatable() ? null : facetDto.getTotalHits() - noEmpty));
    radioControls.add(createRadioButtonResetSpecific(translations.criterionFiltersMap().get("not_empty"),
      facetDto == null || variable.getIsRepeatable() ? null : noEmpty));
    add(radioControls);
  }

  protected void updateCriterionFilter(String filter) {
    setText(filter.isEmpty() ? variable.getName() : variable.getName() + " " + filter.toLowerCase());
  }

}