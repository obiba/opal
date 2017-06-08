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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.common.base.Joiner;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.watopi.chosen.client.event.ChosenChangeEvent;

public abstract class CategoricalCriterionDropdown extends ValueSetCriterionDropdown {

  private Chooser categories;

  public CategoricalCriterionDropdown(String datasource, String table, VariableDto variableDto, String fieldName, QueryResultDto termDto) {
    super(datasource, table, variableDto, fieldName, termDto);
  }

  @Override
  public Widget createSpecificControls() {
    // Update radio controls
    RadioButton in = createRadioButton(translations.criterionFiltersMap().get("in"), null);
    in.addClickHandler(new OperatorClickHandler());
    radioControls.add(in);

    RadioButton not_in = createRadioButton(translations.criterionFiltersMap().get("not_in"), null);
    not_in.addClickHandler(new OperatorClickHandler());
    radioControls.add(not_in);

    categories = new Chooser(true);
    ListItem specificControls = new ListItem();
    specificControls.addStyleName("controls");
    specificControls.add(getCategoriesChooserPanel());

    return specificControls;
  }

  private SimplePanel getCategoriesChooserPanel() {
    SimplePanel categoriesPanel = new SimplePanel();
    if (variable.getValueType().equals("boolean")) {
      categories.addItem(getCategoryItem("T", "true"), "T");
      categories.addItem(getCategoryItem("F", "false"), "F");
    } else {
      for(CategoryDto cat : JsArrays.toIterable(variable.getCategoriesArray())) {
        categories.addItem(getCategoryItem(cat), cat.getName());
      }
    }

    categories.addChosenChangeHandler(new UpdateFilterChosenHandler());
    categories.setVisible(false);

    categoriesPanel.add(categories);
    return categoriesPanel;
  }

  @Override
  public void resetSpecificControls() {
    categories.setVisible(false);
    divider.setVisible(false);
  }

  private String getCategoryItem(String catName, String catLabel) {
    // Get the frequency of this category
    int count = 0;
    for(FacetResultDto.TermFrequencyResultDto result : JsArrays
        .toIterable(queryResult.getFacetsArray().get(0).getFrequenciesArray())) {
      if(result.getTerm().equals(catName)) {
        count = result.getCount();
        break;
      }
    }

    StringBuilder labelBuilder = new StringBuilder(catName);
    String freqLabel = count > 0 ? " (" + count + ")" : "";
    // OPAL-2693 max label length: truncate cat label if necessary
    int maxLength = 20 - labelBuilder.length() - freqLabel.length();

    if(catLabel.isEmpty()) return labelBuilder.append(freqLabel).toString();
    if(maxLength <= 0) return labelBuilder.append(freqLabel).toString();

    if(catLabel.length() > maxLength) {
      labelBuilder.append(": ").append(catLabel.substring(0, maxLength)).append("...");
    } else {
      labelBuilder.append(": ").append(catLabel);
    }
    return labelBuilder.append(freqLabel).toString();
  }

  private String getCategoryItem(CategoryDto cat) {
    return getCategoryItem(cat.getName(), getCategoryLabel(cat));
  }

  private String getCategoryLabel(CategoryDto cat) {
    StringBuilder label = new StringBuilder();
    for(AttributeDto attr : JsArrays.toIterable(cat.getAttributesArray())) {
      if(!attr.hasNamespace() && attr.getName().equals("label")) {
        if(label.length() > 0) label.append(" ");
        if(attr.hasLocale()) {
          label.append("[").append(attr.getLocale()).append("] ");
        }
        label.append(attr.getValue());
      }
    }
    return label.toString();
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if(emptyNotEmpty != null) return emptyNotEmpty;

    List<String> selected = Lists.newArrayList();
    for (String sel : getSelectedCategories()) {
      selected.add(sel.replaceAll(" ", "+"));
    }

    // in
    if(((CheckBox) radioControls.getWidget(3)).getValue() && !selected.isEmpty()) {
      if (selected.size() == 1) return fieldName + ":" + selected.get(0);
      return fieldName + ":(\"" + Joiner.on("\" OR \"").join(selected) + "\")";
    }

    // not in
    if(((CheckBox) radioControls.getWidget(4)).getValue() && !selected.isEmpty()) {
      if (selected.size() == 1) return "NOT " + fieldName + ":" + selected.get(0);
      return "NOT " + fieldName + ":(\"" + Joiner.on("\" OR \"").join(selected) + "\")";
    }

    return null;
  }

  private class UpdateFilterChosenHandler implements ChosenChangeEvent.ChosenChangeHandler {
    @Override
    public void onChange(ChosenChangeEvent chosenChangeEvent) {
      setFilterText();
      doFilter();
    }
  }

  private void setFilterText() {
    List<String> selected = getSelectedCategories();

    if(selected.isEmpty()) {
      setText(variable.getName());
    } else if(((CheckBox) radioControls.getWidget(3)).getValue()) {
      setText(variable.getName() + ": " + translations.criterionFiltersMap().get("in") + " (" +
          Joiner.on(",").join(selected) + ")");
    } else {
      setText(variable.getName() + ": " + translations.criterionFiltersMap().get("not_in") + " (" +
          Joiner.on(",").join(selected) + ")");
    }
  }

  private List<String> getSelectedCategories() {
    List<String> selectedCategories = Lists.newArrayList();
    for(int i = 0; i < categories.getItemCount(); i++) {
      if(categories.isItemSelected(i)) {
        selectedCategories.add(categories.getValue(i));
      }
    }
    return selectedCategories;
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      categories.setVisible(true);
      divider.setVisible(true);
      setFilterText();
    }
  }
}
