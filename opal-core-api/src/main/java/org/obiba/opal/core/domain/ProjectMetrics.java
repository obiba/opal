/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

import java.util.Date;
import java.util.List;

public class ProjectMetrics implements HasUniqueProperties, Comparable<ProjectMetrics>{

  private String name;

  private Date timestamp;

  private int tableCount = -1;

  private int variableCount = -1;

  private int entityCount = -1;

  private int resourceCount = -1;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getTableCount() {
    return tableCount;
  }

  public void setTableCount(int tableCount) {
    this.tableCount = tableCount;
  }

  public int getVariableCount() {
    return variableCount;
  }

  public void setVariableCount(int variableCount) {
    this.variableCount = variableCount;
  }

  public int getEntityCount() {
    return entityCount;
  }

  public void setEntityCount(int entityCount) {
    this.entityCount = entityCount;
  }

  public int getResourceCount() {
    return resourceCount;
  }

  public void setResourceCount(int resourceCount) {
    this.resourceCount = resourceCount;
  }

  @Override
  public int compareTo(ProjectMetrics projectMetrics) {
    return timestamp.compareTo(projectMetrics.timestamp);
  }

  @Override
  public List<String> getUniqueProperties() {
    return null;
  }

  @Override
  public List<Object> getUniqueValues() {
    return null;
  }
}
