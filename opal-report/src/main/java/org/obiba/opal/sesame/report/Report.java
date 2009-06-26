/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.report;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class Report {

  private static final Logger log = LoggerFactory.getLogger(Report.class);

  private boolean withOccurrence;

  private List<IDataItemSelection> selections;

  private List<IDataItemFilter> filters;

  public boolean isWithOccurrence() {
    return withOccurrence;
  }

  public List<IDataItemSelection> getSelections() {
    return selections;
  }

  public List<IDataItemFilter> getFilters() {
    return (List<IDataItemFilter>) (filters != null ? filters : Collections.emptyList());
  }
}
