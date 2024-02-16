/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.analysis;

import jakarta.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 * Result of the analysis: status and report.
 */
public interface AnalysisResult<T extends Analysis, I extends AnalysisResultItem> extends AnalysisResultItem {

  /**
   * Analysis result identifier.
   *
   * @return
   */
  String getId();

  /**
   * The original analysis request identifier.
   *
   * @return
   */
  String getAnalysisName();

  /**
   * Date at which the analysis was started.
   *
   * @return
   */
  Date getStartDate();

  /**
   * Date at which the analysis ended.
   *
   * @return
   */
  Date getEndDate();

  /**
   * Get if there are sub-results.
   *
   * @return
   */
  boolean hasResultItems();

  /**
   * Depending on the nature of the analysis, there could be some sub-results (for instance validation of several variables
   * gives one result per variable and a global result).
   *
   * @return
   */
  @Nullable List<I> getResultItems();

  /**
   * Get ellapsed time.
   *
   * @return
   */
  default long getTime() {
    if (getStartDate() == null) return 0;
    if (getEndDate() == null) return new Date().getTime() - getStartDate().getTime();

    return getEndDate().getTime() - getStartDate().getTime();
  }

}
