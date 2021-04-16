/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.analysis;

import org.json.JSONObject;
import org.obiba.magma.ValueType;
import org.obiba.opal.spi.analysis.AnalysisTemplate;

import java.nio.file.Path;
import java.util.List;

public class AnalysisTemplateImpl implements AnalysisTemplate {
  private String name;
  private String title;
  private String description;
  private JSONObject schemaForm;
  private List<ValueType> valueTypes;

  private Path routinePath;
  private Path reportPath;

  AnalysisTemplateImpl(String name) {
    this.name = name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getDescription() {
    return description;
  }

  /**
   * @param schemaForm the schemaForm to set
   */
  public void setSchemaForm(JSONObject schemaForm) {
    this.schemaForm = schemaForm;
  }

  @Override
  public JSONObject getJSONSchemaForm() {
    return schemaForm;
  }

  /**
   * @param valueTypes the valueTypes to set
   */
  public void setValueTypes(List<ValueType> valueTypes) {
    this.valueTypes = valueTypes;
  }

  @Override
  public List<ValueType> getValueTypes() {
    return valueTypes;
  }

  @Override
  public Path getReportPath() {
    return reportPath;
  }

  /**
   * @param reportPath the reportPath to set
   */
  public void setReportPath(Path reportPath) {
    this.reportPath = reportPath;
  }

  @Override
  public Path getRoutinePath() {
    return routinePath;
  }

  /**
   * @param routinePath the routinePath to set
   */
  public void setRoutinePath(Path routinePath) {
    this.routinePath = routinePath;
  }

}
