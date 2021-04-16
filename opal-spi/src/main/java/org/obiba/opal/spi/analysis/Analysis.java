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

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * An analysis represents the user data analysis request.
 */
public interface Analysis {

  Path ANALYSES_HOME = Paths.get(System.getProperty("OPAL_HOME"), "data", "analyses");

  /**
   * Datasource used by analysis.
   *
   * @return
   */
  @NotNull
  String getDatasource();

  /**
   * Table used by analysis.
   *
    * @return
   */
  @NotNull
  String getTable();

  /**
   * Unique analysis request identifier, Analysis name.
   *
   * @return
   */
  @NotNull
  String getName();

  /**
   * The name of the plugin used to create this analysis.
   *
   * @return
   */
  String getPluginName();

  /**
   * Refers to one of the {@link AnalysisTemplate} made available by the analysis service.
   *
   * @return
   */
  @NotNull
  String getTemplateName();

  /**
   * The parameters to apply to the analysis instance.
   *
   * @return
   */
  JSONObject getParameters();

  /**
   * Get the variable names on which the analysis is to be applied. If empty, all variables of the table is potentially analysable.
   *
   * @return
   */
  List<String> getVariables();

}
