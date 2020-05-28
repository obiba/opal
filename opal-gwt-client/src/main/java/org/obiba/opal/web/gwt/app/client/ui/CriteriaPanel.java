/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The criteria panel holds a list of {@link org.obiba.opal.web.gwt.app.client.ui.CriterionPanel}s.
 */
public class CriteriaPanel extends FlowPanel {

  /**
   * Check if there is at least one criterion.
   *
   * @return
   */
  public boolean hasCriteria() {
    return !getQueryStrings().isEmpty();
  }

  public void addCriterion(CriterionDropdown criterion) {
    add(new CriterionPanel(criterion));
  }

  public void addCriterion(CriterionDropdown criterion, boolean removeable, boolean opened) {
    add(new CriterionPanel(criterion, removeable, opened));
  }

  /**
   * Get the string representation of the query.
   *
   * @return
   */
  public String getQueryString() {
    Collection<String> filters = getQueryStrings();
    return filters.isEmpty() ? "*" : Joiner.on(" AND ").join(filters);
  }

  /**
   * Get each of the criterion query string in their order of appearance.
   *
   * @return
   */
  public List<String> getQueryStrings() {
    List<String> filters = Lists.newArrayList();
    for(int i = 0; i < getWidgetCount(); i++) {
      if(getWidget(i) instanceof CriterionPanel) {
        String queryString = ((CriterionPanel) getWidget(i)).getQueryString();
        if(!Strings.isNullOrEmpty(queryString)) filters.add(queryString);
      }
    }
    return filters;
  }

  /**
   * Get the RQL string representation of the query.
   *
   * @return
   */
  public String getRQLQueryString() {
    Collection<String> filters = getRQLQueryStrings();
    return filters.isEmpty() ? "*" : Joiner.on(",").join(filters);
  }

  /**
   * Get each of the criterion RQL query string in their order of appearance.
   *
   * @return
   */
  public List<String> getRQLQueryStrings() {
    List<String> filters = Lists.newArrayList();
    for(int i = 0; i < getWidgetCount(); i++) {
      if(getWidget(i) instanceof CriterionPanel) {
        String queryString = ((CriterionPanel) getWidget(i)).getRQLQueryString();
        if(!Strings.isNullOrEmpty(queryString)) filters.add(queryString);
      }
    }
    return filters;
  }

  /**
   * Get the fields for each criterion.
   *
   * @return
   */
  public List<String> getRQLFields() {
    List<String> fieldNames = Lists.newArrayList();
    for(int i = 0; i < getWidgetCount(); i++) {
      if(getWidget(i) instanceof CriterionPanel) {
        String fieldName = ((CriterionPanel) getWidget(i)).getRQLField();
        if(!Strings.isNullOrEmpty(fieldName)) fieldNames.add(fieldName);
      }
    }
    return fieldNames;
  }

  /**
   * Get query expressed as Magma javascript statements.
   *
   * @return
   */
  public List<String> getMagmaJsStatements() {
    List<String> statements = Lists.newArrayList();
    for(int i = 0; i < getWidgetCount(); i++) {
      if(getWidget(i) instanceof CriterionPanel) {
        String statement = ((CriterionPanel) getWidget(i)).getMagmaJsStatement();
        if(!Strings.isNullOrEmpty(statement)) statements.add(statement);
      }
    }
    return statements;
  }

  /**
   * Get human readable query string.
   *
   * @return
   */
  public String getQueryText() {
    List<String> texts = getQueryTexts();
    return texts.isEmpty() ? "*" : Joiner.on(" AND ").join(texts);
  }

  /**
   * Get each of the criterion query human readable string in their order of appearance.
   *
   * @return
   */
  public List<String> getQueryTexts() {
    List<String> texts = Lists.newArrayList();
    for(int i = 0; i < getWidgetCount(); i++) {
      if(getWidget(i) instanceof CriterionPanel) {
        String queryString = ((CriterionPanel) getWidget(i)).getQueryText();
        if(!Strings.isNullOrEmpty(queryString)) texts.add(queryString);
      }
    }
    return texts;
  }

  /**
   * Get criterion dropdowns in their order of appearance.
   *
   * @return
   */
  public List<CriterionDropdown> getCriterions() {
    List<CriterionDropdown> widgets = Lists.newArrayList();
    for(int i = 0; i < getWidgetCount(); i++) {
      if (getWidget(i) instanceof CriterionPanel) {
        widgets.add(((CriterionPanel) getWidget(i)).getCriterion());
      }
    }
    return widgets;
  }
}
