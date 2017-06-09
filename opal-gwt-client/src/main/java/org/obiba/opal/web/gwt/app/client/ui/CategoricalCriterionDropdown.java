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

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.*;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.List;

public abstract class CategoricalCriterionDropdown extends ValueSetCriterionDropdown {

  private ListItem specificControls;

  private List<CheckBox> categoryChecks;

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

    specificControls = new ListItem();
    specificControls.addStyleName("controls");
    ComplexPanel checksPanel;
    if (variable.getCategoriesArray() != null && variable.getCategoriesArray().length()>10) {
      ScrollPanel scrollPanel = new ScrollPanel();
      scrollPanel.setHeight("200px");
      specificControls.add(scrollPanel);
      checksPanel = new FlowPanel();
      scrollPanel.add(checksPanel);
    } else {
      checksPanel = specificControls;
    }
    categoryChecks = Lists.newArrayList();
    if (variable.getValueType().equals("boolean")) {
      appendCategoryCheck(checksPanel, "T", translations.trueLabel(), "", getCategoryFrequency("T"));
      appendCategoryCheck(checksPanel, "F", translations.falseLabel(), "", getCategoryFrequency("F"));
    }
    else
      for (CategoryDto cat : JsArrays.toIterable(variable.getCategoriesArray()))
        appendCategoryCheck(checksPanel, cat);

    specificControls.setVisible(false);

    return specificControls;
  }


  @Override
  public void resetSpecificControls() {
    specificControls.setVisible(false);
    divider.setVisible(false);
    doFilter();
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
    if(((CheckBox) radioControls.getWidget(3)).getValue()) {
      if (selected.isEmpty()) return "NOT " + fieldName + ":*";
      if (selected.size() == 1) return fieldName + ":" + selected.get(0);
      return fieldName + ":(\"" + Joiner.on("\" OR \"").join(selected) + "\")";
    }

    // not in
    if(((CheckBox) radioControls.getWidget(4)).getValue()) {
      if (selected.isEmpty()) return fieldName + ":*";
      if (selected.size() == 1) return "NOT " + fieldName + ":" + selected.get(0);
      return "NOT " + fieldName + ":(\"" + Joiner.on("\" OR \"").join(selected) + "\")";
    }

    return null;
  }

  private void setFilterText() {
    List<String> selected = getSelectedCategories();

    if(((CheckBox) radioControls.getWidget(3)).getValue()) {
      if(selected.isEmpty()) setText(variable.getName()+ ": " + translations.criterionFiltersMap().get("none"));
      else setText(variable.getName() + ": " + translations.criterionFiltersMap().get("in") + " (" +
          Joiner.on(",").join(selected) + ")");
    } else {
      if(selected.isEmpty()) setText(variable.getName()+ ": " + translations.criterionFiltersMap().get("all"));
      else setText(variable.getName() + ": " + translations.criterionFiltersMap().get("not_in") + " (" +
          Joiner.on(",").join(selected) + ")");
    }
  }

  private List<String> getSelectedCategories() {
    List<String> selectedCategories = Lists.newArrayList();
    for(CheckBox check : categoryChecks) {
      if(check.getValue()) selectedCategories.add(check.getName());
    }
    return selectedCategories;
  }

  private void appendCategoryCheck(ComplexPanel checksPanel, CategoryDto cat) {
    appendCategoryCheck(checksPanel, cat.getName(), cat.getName(), getCategoryLabel(cat), getCategoryFrequency(cat));
  }

  private void appendCategoryCheck(ComplexPanel checksPanel, String name, String title, String label, int count) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder().appendEscaped(title);
    builder.appendHtmlConstant("<span style=\"font-size:x-small\"> (")
        .append(count).appendEscaped(")")
        .appendHtmlConstant("</span>");
    FlowPanel checkPanel = new FlowPanel();
    CheckBox checkBox = new CheckBox(builder.toSafeHtml());
    checkBox.setName(name);
    checkBox.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        setFilterText();
        doFilter();
      }
    });
    checkBox.addStyleName("inline-block");
    checkPanel.setTitle(label);
    checkPanel.add(checkBox);
    if (!checkPanel.getTitle().isEmpty()) {
      Icon info = new Icon(IconType.INFO_SIGN);
      info.addStyleName("small-indent");
      checkPanel.add(info);
    }
    checksPanel.add(checkPanel);
    categoryChecks.add(checkBox);
  }

  private int getCategoryFrequency(CategoryDto cat) {
    return getCategoryFrequency(cat.getName());
  }

  private int getCategoryFrequency(String name) {
    int count = 0;
    for(FacetResultDto.TermFrequencyResultDto result : JsArrays.toIterable(queryResult.getFacetsArray().get(0).getFrequenciesArray())) {
      if(result.getTerm().equals(name)) {
        count = result.getCount();
        break;
      }
    }
    return count;
  }

  private String getCategoryLabel(CategoryDto cat) {
    StringBuilder label = new StringBuilder();
    for(AttributeDto attr : JsArrays.toIterable(cat.getAttributesArray())) {
      if(!attr.hasNamespace() && attr.getName().equals("label")) {
        if(label.length() > 0) label.append(" ");
        if(attr.hasLocale()) {
          label.append("(").append(attr.getLocale()).append(") ");
        }
        label.append(attr.getValue());
      }
    }
    return label.toString();
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      specificControls.setVisible(true);
      divider.setVisible(true);
      setFilterText();
      doFilter();
    }
  }
}
