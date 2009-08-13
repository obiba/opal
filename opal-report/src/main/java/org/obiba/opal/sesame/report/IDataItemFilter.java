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

import org.obiba.opal.elmo.concepts.DataItem;
import org.openrdf.elmo.sesame.SesameManager;

/**
 * Contract by which a {@code DataItem} can be filtered from a report.
 */
public interface IDataItemFilter {

  /**
   * Returns true when the data item should be included in the report.
   * 
   * @param dataItem
   * @return
   */
  public boolean accept(DataItem dataItem);

  public void contribute(ReportQueryBuilder builder, SesameManager manager);
}
