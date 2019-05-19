/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.search.FacetResultDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import java.util.List;

public abstract class CategoricalCriterionDropdown extends ValueSetCriterionDropdown {

  private ListItem specificControls;

  private List<CheckBox> categoryChecks;

  public CategoricalCriterionDropdown(String datasource, String table, VariableDto variableDto, String fieldName, QueryResultDto facetDto) {
    super(datasource, table, variableDto, fieldName, facetDto);
  }

  public CategoricalCriterionDropdown(RQLValueSetVariableCriterionParser criterion, QueryResultDto facetDto) {
    this(criterion.getDatasourceName(), criterion.getTableName(), criterion.getVariable(), criterion.getField(), facetDto);
    initialize(criterion);
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
    if (variable.getCategoriesArray() != null && variable.getCategoriesArray().length() > 10) {
      ScrollPanel scrollPanel = new ScrollPanel();
      scrollPanel.setHeight("200px");
      specificControls.add(scrollPanel);
      FlowPanel content = new FlowPanel();
      scrollPanel.add(content);
      final TextBox filter = new TextBox();
      filter.addStyleName("bordered right-indent");
      filter.setPlaceholder(translations.criterionFiltersMap().get("filter"));
      filter.addKeyUpHandler(new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          List<String> tokens = FilterHelper.tokenize(filter.getText().trim().toLowerCase());
          if (tokens.isEmpty()) {
            for (CheckBox cb : categoryChecks)
              cb.getParent().setVisible(true);
          } else {
            for (CheckBox cb : categoryChecks) {
              String title = cb.getTitle();
              if (title != null) title = title.toLowerCase();
              cb.getParent().setVisible(FilterHelper.matches(title, tokens) || FilterHelper.matches(cb.getName().toLowerCase(), tokens));
            }
          }
        }
      });
      content.add(filter);
      checksPanel = new FlowPanel();
      checksPanel.addStyleName("top-margin");
      content.add(checksPanel);
    } else {
      checksPanel = specificControls;
    }
    categoryChecks = Lists.newArrayList();
    if (isBooleanVariable()) {
      appendCategoryCheck(checksPanel, "T", translations.trueLabel(), "", getCategoryFrequency("1"));
      appendCategoryCheck(checksPanel, "F", translations.falseLabel(), "", getCategoryFrequency("0"));
    } else
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
    if (emptyNotEmpty != null) return emptyNotEmpty;

    List<String> selected = Lists.newArrayList();
    for (String sel : getSelectedCategories()) {
      selected.add(sel.replace(" ", "+"));
    }

    // in
    if (((CheckBox) radioControls.getWidget(3)).getValue()) {
      if (selected.isEmpty()) return "NOT " + fieldName + ":*";
      if (selected.size() == 1) return fieldName + ":" + selected.get(0);
      return fieldName + ":(\"" + Joiner.on("\" OR \"").join(selected) + "\")";
    }

    // not in
    if (((CheckBox) radioControls.getWidget(4)).getValue()) {
      if (selected.isEmpty()) return fieldName + ":*";
      if (selected.size() == 1) return "NOT " + fieldName + ":" + selected.get(0);
      return "NOT " + fieldName + ":(\"" + Joiner.on("\" OR \"").join(selected) + "\")";
    }

    return null;
  }

  @Override
  public String getRQLQueryString() {
    String emptyNotEmpty = super.getRQLQueryString();
    if (emptyNotEmpty != null) return emptyNotEmpty;

    List<String> selected = Lists.newArrayList();
    for (String sel : getSelectedCategories()) {
      selected.add(URL.encode(sel).replace(" ", "+"));
    }

    String rqlField = getRQLField();
    // in
    if (((CheckBox) radioControls.getWidget(3)).getValue()) {
      if (selected.isEmpty()) return "not(in(" + rqlField + ",*))";
      return "in(" + rqlField + ",(" + Joiner.on(",").join(selected) + "))";
    }

    // not in
    if (((CheckBox) radioControls.getWidget(4)).getValue()) {
      if (selected.isEmpty()) return "in(" + rqlField + ",*)";
      return "not(in(" + rqlField + ",(" + Joiner.on(",").join(selected) + ")))";
    }

    return null;
  }

  @Override
  protected String getMagmaJsStatement() {
    String statement = super.getMagmaJsStatement();
    if (!Strings.isNullOrEmpty(statement)) return statement;

    statement = "$('" + variable.getName() + "')";
    List<String> selected = getSelectedCategories();
    if (isBooleanVariable()) {
      for (int i = 0; i < selected.size(); i++) {
        if (selected.get(i).equals("T")) selected.set(i, "true");
        else if (selected.get(i).equals("F")) selected.set(i, "false");
      }
    }
    // in
    if (((CheckBox) radioControls.getWidget(3)).getValue()) {
      if (selected.isEmpty()) return statement + ".isNull()";
      return statement + ".any('" + Joiner.on("','").join(selected) + "')";
    }

    // not in
    if (((CheckBox) radioControls.getWidget(4)).getValue()) {
      if (selected.isEmpty()) return "";
      return statement + ".any('" + Joiner.on("','").join(selected) + "').not()";
    }

    return "";
  }

  private void initialize(RQLValueSetVariableCriterionParser criterion) {
    if (criterion.hasWildcardValue()) {
      if (criterion.isNot()) {
        ((CheckBox) radioControls.getWidget(3)).setValue(true);
        specificControls.setVisible(true);
        divider.setVisible(true);
      }
    } else if (criterion.hasValue()) {
      for (String value : criterion.getValues()) {
        selectCategory(value);
      }
      ((CheckBox) radioControls.getWidget(criterion.isNot() ? 4 : 3)).setValue(true);
      specificControls.setVisible(true);
      divider.setVisible(true);
    } else if (criterion.isExists())
      ((CheckBox) radioControls.getWidget(criterion.isNot() ? 1 : 2)).setValue(true);
    setFilterText();
  }

  private void selectCategory(String value) {
    String valueStr = value.replace("+", " ");
    for (CheckBox checkBox : categoryChecks) {
      if (checkBox.getName().equals(valueStr)) {
        checkBox.setValue(true);
        break;
      }
    }
  }

  private void setFilterText() {
    String filter = variable.getName() + " ";
    if (getRadioButtonValue(1)) {
      setText(filter + translations.criterionFiltersMap().get("empty").toLowerCase());
      return;
    }
    if (getRadioButtonValue(2)) {
      setText(filter + translations.criterionFiltersMap().get("not_empty").toLowerCase());
      return;
    }

    List<String> selected = getSelectedCategories();
    if (getRadioButtonValue(3)) {
      if (selected.isEmpty()) setText(filter + translations.criterionFiltersMap().get("none").toLowerCase());
      else setText(filter + translations.criterionFiltersMap().get("in").toLowerCase() + " (" +
        Joiner.on(",").join(selected) + ")");
      return;
    }

    if (getRadioButtonValue(4)) {
      if (selected.isEmpty()) setText(filter + translations.criterionFiltersMap().get("all").toLowerCase());
      else setText(filter + translations.criterionFiltersMap().get("not_in").toLowerCase() + " (" +
        Joiner.on(",").join(selected) + ")");
      return;
    }

    if (getRadioButtonValue(0)) {
      setText(filter + translations.criterionFiltersMap().get("all").toLowerCase());
      return;
    }

    setText(filter);
  }

  private List<String> getSelectedCategories() {
    List<String> selectedCategories = Lists.newArrayList();
    for (CheckBox check : categoryChecks) {
      if (check.getValue()) selectedCategories.add(check.getName());
    }
    return selectedCategories;
  }

  private void appendCategoryCheck(ComplexPanel checksPanel, CategoryDto cat) {
    appendCategoryCheck(checksPanel, cat.getName(), cat.getName(), getCategoryLabel(cat), getCategoryFrequency(cat));
  }

  private void appendCategoryCheck(ComplexPanel checksPanel, String name, String title, String label, int count) {
    SafeHtmlBuilder builder = new SafeHtmlBuilder().appendEscaped(title);
    if (!variable.getIsRepeatable())
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
    for (FacetResultDto.TermFrequencyResultDto result : JsArrays.toIterable(facetDto.getFacetsArray().get(0).getFrequenciesArray())) {
      if (result.getTerm().equals(name)) {
        count = result.getCount();
        break;
      }
    }
    return count;
  }

  private String getCategoryLabel(CategoryDto cat) {
    StringBuilder label = new StringBuilder();
    for (AttributeDto attr : JsArrays.toIterable(cat.getAttributesArray())) {
      if (!attr.hasNamespace() && attr.getName().equals("label")) {
        if (label.length() > 0) label.append(" ");
        if (attr.hasLocale()) {
          label.append("(").append(attr.getLocale()).append(") ");
        }
        label.append(attr.getValue());
      }
    }
    return label.toString();
  }

  private boolean isBooleanVariable() {
    return variable.getValueType().equals("boolean");
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
