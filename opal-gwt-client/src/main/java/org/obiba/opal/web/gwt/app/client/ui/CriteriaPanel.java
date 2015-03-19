/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The criteria panel holds a list of {@link org.obiba.opal.web.gwt.app.client.ui.CriterionPanel}s.
 */
public class CriteriaPanel extends FlowPanel {

  /**
   * Check if there is at least one criterion.
   * @return
   */
  public boolean hasCriteria() {
    for(int i = 0; i < getWidgetCount(); i++) {
      if(getWidget(i) instanceof CriterionPanel) {
        return true;
      }
    }
    return false;
  }

  public void addCriterion(CriterionDropdown criterion) {
    add(new CriterionPanel(criterion));
  }

  public void addCriterion(CriterionDropdown criterion, boolean removeable, boolean opened) {
    add(new CriterionPanel(criterion, removeable, opened));
  }

  /**
   * Get the string representation of the query.
   * @return
   */
  public String getQueryString() {
    Collection<String> filters = new ArrayList<String>();
    for(int i = 0; i < getWidgetCount(); i++) {
      if(getWidget(i) instanceof CriterionPanel) {
        String queryString = ((CriterionPanel) getWidget(i)).getQueryString();
        if(!Strings.isNullOrEmpty(queryString)) filters.add(queryString);
      }
    }

    return filters.isEmpty() ? "*" : Joiner.on(" AND ").join(filters);
  }
}
