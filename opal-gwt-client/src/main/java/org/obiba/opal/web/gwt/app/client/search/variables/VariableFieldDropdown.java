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
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CriterionDropdown;
import org.obiba.opal.web.gwt.app.client.ui.ListItem;
import org.obiba.opal.web.model.client.search.FacetResultDto;

import java.util.List;
import java.util.Map;

public class VariableFieldDropdown extends CriterionDropdown {

  private ListItem divider;

  private ListItem specificControls;

  private TextBox matches;

  private List<String> categories;

  Map<String, Integer> categoryFrequencies = Maps.newHashMap();

  public VariableFieldDropdown(VariableFieldSuggestOracle.VariableFieldSuggestion suggestion, FacetResultDto facet) {
    super(suggestion.getReplacementString());
    this.categories = suggestion.getCategories();
    if (facet != null) {
      for (FacetResultDto.TermFrequencyResultDto termCount : JsArrays.toIterable(facet.getFrequenciesArray()))
        categoryFrequencies.put(termCount.getTerm(), termCount.getCount());
    }
    initialize(suggestion.getReplacementString());
  }

  @Override
  protected Widget createSpecificControls() {
    return hasCategories() ?  createCategoriesControls() : createMatchQueryControls();
  }

  protected String getSpecificQueryString() {
    return hasCategories() ? getCategoriesQueryString() : getMatchQueryString();
  }

  @Override
  protected void resetSpecificControls() {
    specificControls.setVisible(false);
    divider.setVisible(false);
  }

  @Override
  public void doFilter() {

  }

  @Override
  public String getQueryString() {
    // Any
    if(getRadioControl(0).getValue()) return "_exists_:" + fieldName;
    // None
    if(getRadioControl(1).getValue()) return "NOT _exists_:" + fieldName;
    return getSpecificQueryString();
  }

  @Override
  protected void updateCriterionFilter(String filter) {
    if (getRadioControl(0).getValue() || getRadioControl(1).getValue())
      super.updateCriterionFilter(filter);
    else {
      String text = hasCategories() ? getCategoriesQueryText() : matches.getText();
      if (Strings.isNullOrEmpty(text)) super.updateCriterionFilter("");
      else if (text.length()>30) setText(filter + " " + text.substring(0, 30) + "...");
      else setText(filter + " " + text);
    }
    setTitle(getQueryString());
  }

  //
  // Private methods
  //

  private boolean hasCategories() {
    return !categories.isEmpty();
  }

  private void initialize(String fieldQuery) {
    initializeHeader(fieldQuery);
    initializeRadioControls(fieldQuery);
    Widget controls = createSpecificControls();
    if(controls != null) {
      divider = new ListItem();
      divider.addStyleName("divider");
      add(divider);
      add(controls);
      String value = extractValue(fieldQuery);
      if (hasCategories()) {
        // select the appropriate checkbox
        for (int i = 0; i<specificControls.getWidgetCount(); i++) {
          CheckBox checkbox = (CheckBox) specificControls.getWidget(i);
          checkbox.setValue(normalizeKeyword(checkbox.getName()).equals(value));
        }
      } else {
        matches.setText(value);
      }
    }
    updateCriterionFilter(getRadioControl(0).getValue() ? translations.criterionFiltersMap().get("any")
        : translations.criterionFiltersMap().get("in"));
    if (getRadioControl(0).getValue() || getRadioControl(1).getValue()) resetSpecificControls();
  }

  private void initializeHeader(String fieldQuery) {
    ListItem header = new ListItem();
    header.addStyleName("controls");
    header.add(new Label(fieldName));
    add(header);
    ListItem headerDivider = new ListItem();
    headerDivider.addStyleName("divider");
    add(headerDivider);
  }

  private void initializeRadioControls(String fieldQuery) {
    String value = extractValue(fieldQuery);
    GWT.log("initializeRadioControls.value=" + value);
    radioControls.add(createRadioButtonResetSpecific(translations.criterionFiltersMap().get("any"), null));
    radioControls.add(createRadioButtonResetSpecific(translations.criterionFiltersMap().get("none"), null));
    RadioButton in = createRadioButton(translations.criterionFiltersMap().get("in"), null);
    in.addClickHandler(new OperatorClickHandler());
    radioControls.add(in);

    RadioButton not_in = createRadioButton(translations.criterionFiltersMap().get("not_in"), null);
    not_in.addClickHandler(new OperatorClickHandler());
    radioControls.add(not_in);
    if (value.isEmpty() || "*".equals(value)) getRadioControl(0).setValue(true);
    else in.setValue(true);
    add(radioControls);
  }

  private RadioButton getRadioControl(int index) {
    return (RadioButton) radioControls.getWidget(index);
  }

  private Widget createMatchQueryControls() {
    specificControls = new ListItem();
    specificControls.addStyleName("controls");
    matches = new TextBox();
    matches.addStyleName("bordered");
    matches.setPlaceholder(translations.criterionFiltersMap().get("custom_match_query"));
    matches.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        //updateMatchCriteriaFilter();
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          updateCriterionFilter(translations.criterionFiltersMap().get(isNot() ? "not_in" : "in"));
          doFilter();
        }
      }
    });
    specificControls.add(matches);
    return specificControls;
  }

  private Widget createCategoriesControls() {
    specificControls = new ListItem();
    specificControls.addStyleName("controls");
    for (String category : categories) {
      SafeHtmlBuilder builder = new SafeHtmlBuilder().appendEscaped(category);
      if (categoryFrequencies.containsKey(category.toLowerCase())) { // facet term is lower case...
        builder.appendHtmlConstant("<span style=\"font-size:x-small\"> (")
            .append(categoryFrequencies.get(category.toLowerCase())).appendEscaped(")")
            .appendHtmlConstant("</span>");
      }
      CheckBox checkBox = new CheckBox(builder.toSafeHtml());
      checkBox.setName(category);
      checkBox.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          updateCriterionFilter(translations.criterionFiltersMap().get(isNot() ? "not_in" : "in"));
          doFilter();
        }
      });
      specificControls.add(checkBox);
    }
    return specificControls;
  }

  private String getMatchQueryString() {
    if (matches.getText().isEmpty()) return null;
    return (isNot() ? "NOT " : "") + fieldName + ":" + matches.getText();
  }

  private String getCategoriesQueryString() {
    String rval = null;
    for (int i = 0; i<specificControls.getWidgetCount(); i++) {
      CheckBox checkbox = (CheckBox) specificControls.getWidget(i);
      if (checkbox.getValue()) {
        if (Strings.isNullOrEmpty(rval)) rval = normalizeKeyword(checkbox.getName());
        else rval = rval + " OR " + normalizeKeyword(checkbox.getName());
      }
    }
    if (Strings.isNullOrEmpty(rval)) return null;
    return (isNot() ? "NOT " : "") + fieldName + ":" + (rval.contains(" OR ") ? "(" + rval + ")" : rval);
  }

  private String normalizeKeyword(String keyword) {
    return keyword.replaceAll(" ","+");
  }

  /**
   * For humans.
   *
   * @return
   */
  private String getCategoriesQueryText() {
    String rval = "";
    for (int i = 0; i<specificControls.getWidgetCount(); i++) {
      CheckBox checkbox = (CheckBox) specificControls.getWidget(i);
      if (checkbox.getValue()) {
        if (Strings.isNullOrEmpty(rval)) rval = checkbox.getName();
        else rval = rval + "," + checkbox.getName();
      }
    }
    return rval;
  }

  private boolean isNot() {
    // Not in
    return getRadioControl(3).getValue();
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      specificControls.setVisible(true);
      divider.setVisible(true);
    }
  }
}
