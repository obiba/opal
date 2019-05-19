/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.web.bindery.event.shared.EventBus;

/**
 * Helper class to execute filter queries on variables. Will try to filter variables through ElasticSearch
 * if it fails, will query through Magma
 */
public abstract class AbstractVariablesFilter {

  public static final String SORT_DESCENDING = "DESC";

  public static final String SORT_ASCENDING = "ASC";

  private AbstractVariablesFilter nextFilter;

  protected EventBus eventBus;

  protected TableDto table;

  protected List<VariableDto> results = new ArrayList<VariableDto>();

  public abstract void filter(EventBus eventBus, TableDto table);

  public void next(EventBus eventBus, TableDto table, List<VariableDto> results) {
    if(nextFilter != null) nextFilter.filter(eventBus, table);
  }

  public void setNextFilter(AbstractVariablesFilter nextFilter) {
    this.nextFilter = nextFilter;
  }

  public AbstractVariablesFilter nextFilter(AbstractVariablesFilter next) {
    setNextFilter(next);
    return next;
  }
}
