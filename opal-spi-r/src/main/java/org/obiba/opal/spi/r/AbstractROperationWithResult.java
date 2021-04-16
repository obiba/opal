/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import org.json.JSONObject;
import org.obiba.opal.spi.r.analysis.RAnalysis;
import org.springframework.util.Assert;

import java.util.NoSuchElementException;

/**
 * Implements ROperationWithResult with local result storage. Overrides {@code #eval(String)} and saves the returned
 * value as the result of the {@code ROperationWithResult}
 */
public abstract class AbstractROperationWithResult extends AbstractROperation implements ROperationWithResult {

  private RServerResult result;

  private boolean ignoreResult;

  public void setIgnoreResult(boolean ignoreResult) {
    this.ignoreResult = ignoreResult;
  }

  public boolean isIgnoreResult() {
    return ignoreResult;
  }

  @Override
  public RServerResult getResult() {
    if (!hasResult()) throw new NoSuchElementException();
    return result;
  }

  @Override
  public boolean hasResult() {
    return !isIgnoreResult() && result != null;
  }

  protected void setResult(RServerResult result) {
    this.result = result;
  }

  protected String getMetadata(RAnalysis analysis) {
    Assert.notNull(analysis, "analysis cannot be null");

    JSONObject metadata = new JSONObject();
    metadata.put("datasource", analysis.getDatasource());
    metadata.put("table", analysis.getTable());
    metadata.put("variableCount", analysis.getVariables().size());
    metadata.put("name", analysis.getName());
    return metadata.toString();
  }

}
