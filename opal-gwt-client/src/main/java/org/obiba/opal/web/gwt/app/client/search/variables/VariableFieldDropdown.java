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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.*;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CriterionDropdown;
import org.obiba.opal.web.gwt.app.client.ui.ListItem;
import org.obiba.opal.web.model.client.search.FacetResultDto;

import java.util.List;
import java.util.Map;

public class VariableFieldDropdown extends CriterionDropdown {

  private VariableFieldSuggestOracle.FieldItem fieldItem;

  private ListItem divider;

  private ListItem specificControls;

  private TextBox matches;

  private List<VariableFieldSuggestOracle.FieldItem> fieldTerms;

  private Map<String, Integer> fieldTermFrequencies = Maps.newHashMap();

  private List<CheckBox> fieldTermChecks = Lists.newArrayList();

  public VariableFieldDropdown(VariableFieldSuggestOracle.VariableFieldSuggestion suggestion, FacetResultDto facet) {
    super(suggestion.getField().getName());
    this.fieldItem = suggestion.getField();
    this.fieldTerms = suggestion.getFieldTerms();
    if (facet != null) {
      for (FacetResultDto.TermFrequencyResultDto termCount : JsArrays.toIterable(facet.getFrequenciesArray()))
        fieldTermFrequencies.put(termCount.getTerm(), termCount.getCount());
    }
    initialize(suggestion.getReplacementString());
  }

  @Override
  protected Widget createSpecificControls() {
    return hasFieldTerms() ?  createFieldTermsControls() : createMatchQueryControls();
  }

  protected String getSpecificQueryString() {
    return hasFieldTerms() ? getFieldTermsQueryString() : getMatchQueryString();
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
      setText(filter.isEmpty() ? fieldItem.getTitle() : fieldItem.getTitle() + ": " + filter);
    else {
      String text = hasFieldTerms() ? getFieldTermsQueryText() : matches.getText();
      if (Strings.isNullOrEmpty(text)) super.updateCriterionFilter("");
      else if (text.length()>30) setText(filter + " " + text.substring(0, 30) + "...");
      else setText(filter + " " + text);
    }
    setTitle(getQueryString());
  }

  //
  // Private methods
  //

  private boolean hasFieldTerms() {
    return !fieldTerms.isEmpty();
  }

  private void initialize(String fieldQuery) {
    initializeHeader();
    initializeRadioControls(fieldQuery);
    Widget controls = createSpecificControls();
    if(controls != null) {
      divider = new ListItem();
      divider.addStyleName("divider");
      add(divider);
      add(controls);
      String value = extractValue(fieldQuery);
      if (hasFieldTerms()) {
        // select the appropriate checkbox
        for (CheckBox checkbox : fieldTermChecks) {
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

  private void initializeHeader() {
    ListItem header = new ListItem();
    header.addStyleName("controls");
    Label label = new Label(fieldItem.getTitle());
    label.setTitle(fieldItem.getDescription());
    header.add(label);
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

  private Widget createFieldTermsControls() {
    specificControls = new ListItem();
    specificControls.addStyleName("controls");
    ComplexPanel checksPanel;
    if (fieldTerms.size()>10) {
      ScrollPanel scrollPanel = new ScrollPanel();
      scrollPanel.setHeight("200px");
      specificControls.add(scrollPanel);
      checksPanel = new FlowPanel();
      scrollPanel.add(checksPanel);
    } else {
      checksPanel = specificControls;
    }
    fieldTermChecks.clear();
    for (VariableFieldSuggestOracle.FieldItem fieldTerm : fieldTerms) {
      SafeHtmlBuilder builder = new SafeHtmlBuilder().appendEscaped(fieldTerm.getTitle());
      if (fieldTermFrequencies.containsKey(fieldTerm.getName().toLowerCase())) { // facet term is lower case...
        builder.appendHtmlConstant("<span style=\"font-size:x-small\"> (")
            .append(fieldTermFrequencies.get(fieldTerm.getName().toLowerCase())).appendEscaped(")")
            .appendHtmlConstant("</span>");
      }
      CheckBox checkBox = new CheckBox(builder.toSafeHtml());
      checkBox.setName(fieldTerm.getName());
      checkBox.setTitle(fieldTerm.getDescription());
      checkBox.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          updateCriterionFilter(translations.criterionFiltersMap().get(isNot() ? "not_in" : "in"));
          doFilter();
        }
      });
      checksPanel.add(checkBox);
      fieldTermChecks.add(checkBox);
    }
    return specificControls;
  }

  private String getMatchQueryString() {
    if (matches.getText().isEmpty()) return null;
    return (isNot() ? "NOT " : "") + fieldName + ":" + matches.getText();
  }

  private String getFieldTermsQueryString() {
    String rval = null;
    for (CheckBox checkbox : fieldTermChecks) {
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
  private String getFieldTermsQueryText() {
    String text = "";
    for (CheckBox checkbox : fieldTermChecks) {
      if (checkbox.getValue()) {
        String title = getFieldTermTitle(checkbox.getName());
        if (Strings.isNullOrEmpty(text)) text = title;
        else text = text + "," + title;
      }
    }
    return text;
  }

  private String getFieldTermTitle(String name) {
    for (VariableFieldSuggestOracle.FieldItem item : fieldTerms) {
      if (item.getName().equals(name)) return item.getTitle();
    }
    return name;
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
