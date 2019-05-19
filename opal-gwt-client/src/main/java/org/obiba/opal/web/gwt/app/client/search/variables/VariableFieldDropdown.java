/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.variables;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.*;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.CriterionDropdown;
import org.obiba.opal.web.gwt.app.client.ui.ListItem;
import org.obiba.opal.web.gwt.rql.client.RQLQuery;
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

  private boolean analyzed = false;

  public VariableFieldDropdown(VariableFieldSuggestOracle.VariableFieldSuggestion suggestion, boolean useSuggestion, FacetResultDto facet) {
    super(suggestion.getField().getName().replaceAll("\\.analyzed$",""));
    // when "analyzed", a field has 2 flavors: exact match (in) or ordinary match (like)
    this.analyzed = suggestion.getField().getName().endsWith(".analyzed");
    this.fieldItem = suggestion.getField();
    this.fieldTerms = suggestion.getFieldTerms();
    if (facet != null) {
      for (FacetResultDto.TermFrequencyResultDto termCount : JsArrays.toIterable(facet.getFrequenciesArray()))
        fieldTermFrequencies.put(termCount.getTerm(), termCount.getCount());
    }
    initialize(useSuggestion ? suggestion.getReplacementString() : "");
  }

  public void initialize(RQLQuery rqlQuery) {
    if (rqlQuery == null) return;
    specificControls.setVisible(false);
    divider.setVisible(false);
    getRadioControl(0).setValue(false);
    getRadioControl(2).setValue(false);
    if (hasLike())
      getRadioControl(4).setValue(false);
    String filter;
    if ("exists".equals(rqlQuery.getName())) {
      getRadioControl(0).setValue(true);
      filter = translations.criterionFiltersMap().get("any").toLowerCase();
    } else if ("not".equals(rqlQuery.getName())) {
      RQLQuery subQuery = rqlQuery.getRQLQuery(0);
      if ("exists".equals(subQuery.getName())) {
        getRadioControl(1).setValue(true);
        filter = translations.criterionFiltersMap().get("none").toLowerCase();
      } else if ("like".equals(rqlQuery.getName())) {
        getRadioControl(isFieldTermWildCard(subQuery) ? 4 : 5).setValue(true);
        filter = translations.criterionFiltersMap().get("not_like");
        applySelection(subQuery);
      } else {
        getRadioControl(isFieldTermWildCard(subQuery) ? 2 : 3).setValue(true);
        filter = translations.criterionFiltersMap().get("not_in");
        applySelection(subQuery);
      }
    } else if ("like".equals(rqlQuery.getName())) {
      getRadioControl(isFieldTermWildCard(rqlQuery) ? 5 : 4).setValue(true);
      filter = translations.criterionFiltersMap().get("like");
      applySelection(rqlQuery);
    } else {
      getRadioControl(isFieldTermWildCard(rqlQuery) ? 3 : 2).setValue(true);
      filter = translations.criterionFiltersMap().get("in");
      applySelection(rqlQuery);
    }
    updateCriterionFilter(filter);
  }

  private boolean isFieldTermWildCard(RQLQuery rqlQuery) {
    return hasFieldTerms() && !rqlQuery.isArray(1) && "*".equals(rqlQuery.getString(1));
  }

  private void applySelection(RQLQuery rqlQuery) {
    if (hasFieldTerms()) {
      List<String> selections;
      if (rqlQuery.isArray(1))
        selections = JsArrays.toList(rqlQuery.getArray(1));
      else
        selections = Lists.newArrayList(rqlQuery.getString(1));
      for (CheckBox check : fieldTermChecks) {
        check.setValue(selections.contains(normalizeKeyword(check.getName())));
      }
    } else {
      String selection = rqlQuery.isArray(1) ? rqlQuery.getArray(1).get(0) : rqlQuery.getString(1);
      matches.setText(selection.replace("+", " "));
    }
    specificControls.setVisible(true);
    divider.setVisible(true);
  }

  @Override
  protected void onDropdownChange() {
    String filter = "";
    if (getRadioControl(0).getValue()) {
      filter = translations.criterionFiltersMap().get("any");
    } else if (getRadioControl(1).getValue()) {
      filter = translations.criterionFiltersMap().get("none");
    } else {
      filter = translations.criterionFiltersMap().get((isNot() ? "not_" : "") + (isLikeSelected() ? "like" : "in"));
    }
    updateCriterionFilter(filter);
  }

  @Override
  protected Widget createSpecificControls() {
    return hasFieldTerms() ? createFieldTermsControls() : createMatchQueryControls();
  }

  protected String getSpecificQueryString() {
    return hasFieldTerms() ? getFieldTermsQueryString() : getMatchQueryString();
  }

  protected String getSpecificRQLQueryString() {
    return hasFieldTerms() ? getFieldTermsRQLQueryString() : getMatchRQLQueryString();
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
    if (getRadioControl(0).getValue()) return "_exists_:" + fieldName;
    // None
    if (getRadioControl(1).getValue()) return "NOT _exists_:" + fieldName;
    return getSpecificQueryString();
  }

  @Override
  public String getRQLQueryString() {
    // Any
    if (getRadioControl(0).getValue()) return "exists(" + getRQLField() + ")";
    // None
    if (getRadioControl(1).getValue()) return "not(exists(" + getRQLField() + "))";
    return getSpecificRQLQueryString();
  }

  @Override
  protected void updateCriterionFilter(String filter) {
    if (getRadioControl(0).getValue() || getRadioControl(1).getValue())
      setText(filter.isEmpty() ? fieldItem.getTitle() : fieldItem.getTitle() + ": " + filter.toLowerCase());
    else {
      String text = hasFieldTerms() ? getFieldTermsQueryText() : matches.getText();
      if (Strings.isNullOrEmpty(text)) super.updateCriterionFilter("");
      else if (text.length() > 30) setText(filter + " " + text.substring(0, 30) + "...");
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
    if (controls != null) {
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
        matches.setText(value.replace("+", " "));
      }
    }
    updateCriterionFilter(getRadioControl(0).getValue() ? translations.criterionFiltersMap().get("any")
        : translations.criterionFiltersMap().get(isLikeSelected() ? "like" : "in"));
    if (getRadioControl(0).getValue() || getRadioControl(1).getValue()) {
      resetSpecificControls();
      for (CheckBox checkbox : fieldTermChecks) checkbox.setValue(false);
    }
  }

  private void initializeHeader() {
    ListItem header = new ListItem();
    header.addStyleName("controls");
    header.setTitle(fieldItem.getDescription());
    Label label = new InlineLabel(fieldItem.getTitle());
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

  private void initializeRadioControls(String fieldQuery) {
    String value = extractValue(fieldQuery);
    radioControls.add(createRadioButtonResetSpecific(translations.criterionFiltersMap().get("any"), null));
    radioControls.add(createRadioButtonResetSpecific(translations.criterionFiltersMap().get("none"), null));

    RadioButton in = createRadioButton(translations.criterionFiltersMap().get("in"), null);
    in.addClickHandler(new OperatorClickHandler());
    radioControls.add(in);

    RadioButton notIn = createRadioButton(translations.criterionFiltersMap().get("not_in"), null);
    notIn.addClickHandler(new OperatorClickHandler());
    radioControls.add(notIn);

    if (hasLike()) {
      RadioButton like = createRadioButton(translations.criterionFiltersMap().get("like"), null);
      like.addClickHandler(new OperatorClickHandler());
      radioControls.add(like);

      RadioButton notLike = createRadioButton(translations.criterionFiltersMap().get("not_like"), null);
      notLike.addClickHandler(new OperatorClickHandler());
      radioControls.add(notLike);
    }

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
          updateCriterionFilter(translations.criterionFiltersMap().get((isNot() ? "not_" : "") + (isLikeSelected() ? "like" : "in")));
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
    if (fieldTerms.size() > 10) {
      ScrollPanel scrollPanel = new ScrollPanel();
      scrollPanel.setHeight("200px");
      specificControls.add(scrollPanel);
      FlowPanel content = new FlowPanel();
      checksPanel = new FlowPanel();
      final TextBox filter = new TextBox();
      filter.addStyleName("bordered right-indent");
      filter.setPlaceholder(translations.criterionFiltersMap().get("filter"));
      filter.addKeyUpHandler(new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          List<String> tokens = FilterHelper.tokenize(filter.getText().trim().toLowerCase());
          if (tokens.isEmpty()) {
            for (CheckBox cb : fieldTermChecks)
              cb.getParent().setVisible(true);
          } else {
            for (CheckBox cb : fieldTermChecks) {
              String title = getFieldTermTitle(cb.getName());
              if (title != null) title = title.toLowerCase();
              cb.getParent().setVisible(FilterHelper.matches(title, tokens) || FilterHelper.matches(cb.getName().toLowerCase(), tokens));
            }
          }
        }
      });
      content.add(filter);
      checksPanel.addStyleName("top-margin");
      content.add(checksPanel);
      scrollPanel.add(content);
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
      FlowPanel checkPanel = new FlowPanel();
      CheckBox checkBox = new CheckBox(builder.toSafeHtml());
      checkBox.setName(fieldTerm.getName());
      checkBox.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          updateCriterionFilter(translations.criterionFiltersMap().get(isNot() ? "not_in" : "in"));
          doFilter();
        }
      });
      checkBox.addStyleName("inline-block");
      checkPanel.setTitle(fieldTerm.getDescription());
      checkPanel.add(checkBox);
      if (!checkPanel.getTitle().isEmpty()) {
        Icon info = new Icon(IconType.INFO_SIGN);
        info.addStyleName("small-indent");
        checkPanel.add(info);
      }
      checksPanel.add(checkPanel);
      fieldTermChecks.add(checkBox);
    }
    return specificControls;
  }

  private String getMatchFieldName() {
    return fieldName + (isLikeSelected() ? ".analyzed" : "");
  }

  private String getMatchQueryString() {
    if (matches.getText().isEmpty()) return null;
    return (isNot() ? "NOT " : "") + getMatchFieldName() + ":(" + matches.getText() + ")";
  }

  private String getMatchRQLQueryString() {
    if (matches.getText().isEmpty()) return null;
    String op = isLikeSelected() ? "like" : "in";
    String q = op + "(" + getRQLField() + ",(" + matches.getText() + "))";
    return isNot() ? "not(" + q + ")" : q;
  }

  private String getFieldTermsQueryString() {
    String rval = null;
    for (CheckBox checkbox : fieldTermChecks) {
      if (checkbox.getValue()) {
        if (Strings.isNullOrEmpty(rval)) rval = normalizeESKeyword(checkbox.getName());
        else rval = rval + " OR " + normalizeESKeyword(checkbox.getName());
      }
    }
    if (Strings.isNullOrEmpty(rval)) return (isNot() ? "" : "NOT ") + fieldName + ":*";
    return (isNot() ? "NOT " : "") + fieldName + ":" + (rval.contains(" OR ") ? "(" + rval + ")" : rval);
  }

  private String getFieldTermsRQLQueryString() {
    String rval = null;
    for (CheckBox checkbox : fieldTermChecks) {
      if (checkbox.getValue()) {
        if (Strings.isNullOrEmpty(rval)) rval = normalizeKeyword(checkbox.getName());
        else rval = rval + "," + normalizeKeyword(checkbox.getName());
      }
    }
    if (Strings.isNullOrEmpty(rval)) {
      String q = "in(" + getRQLField() + ",*)";
      return isNot() ? q : "not(" + q + ")";
    }
    String q = "in(" + getRQLField() + ",(" + rval + "))";
    return isNot() ? "not(" + q + ")" : q;
  }

  private String normalizeESKeyword(String keyword) {
    return keyword.contains(" ") ? "\"" + keyword + "\"" : keyword;
  }

  private String normalizeKeyword(String keyword) {
    return keyword.replaceAll(" ", "+");
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
    if (Strings.isNullOrEmpty(text))
      return translations.criterionFiltersMap().get(isNot() ? "any" : "none").toLowerCase();
    return text;
  }

  private String getFieldTermTitle(String name) {
    for (VariableFieldSuggestOracle.FieldItem item : fieldTerms) {
      if (item.getName().equals(name)) return item.getTitle();
    }
    return name;
  }

  /**
   * Not selection: neither in, nor like.
   *
   * @return
   */
  private boolean isNot() {
    // Not in or Not like
    return getRadioControl(3).getValue() || (hasLike() && getRadioControl(5).getValue());
  }

  /**
   * Whether like or not like is selected (when applicable).
   *
   * @return
   */
  private boolean isLikeSelected() {
    return hasLike() && (getRadioControl(4).getValue() || getRadioControl(5).getValue());
  }

  /**
   * No field terms and analyzed field.
   *
   * @return
   */
  private boolean hasLike() {
    return !hasFieldTerms() && analyzed;
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      specificControls.setVisible(true);
      divider.setVisible(true);
    }
  }
}
