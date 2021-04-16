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

import org.json.JSONObject;
import org.obiba.magma.ValueType;

import java.nio.file.Path;
import java.util.List;

public interface AnalysisTemplate {

  /**
   * Analysis template unique name.
   *
   * @return
   */
  String getName();

  /**
   * Title of the analysis.
   *
   * @return
   */
  String getTitle();

  /**
   * Markdown can be used to describe the analysis.
   *
   * @return
   */
  String getDescription();

  /**
   * The form to collect analysis parameters, described by a JSON object.
   *
   * @return
   */
  JSONObject getJSONSchemaForm();

  /**
   * Get the value types handled by the analysis. If empty, any value type is handled.
   *
   * @return
   */
  List<ValueType> getValueTypes();

  /**
   * @return the reportPath
   */
  Path getReportPath();


  /**
   * @return the routinePath
   */
  Path getRoutinePath();

}
