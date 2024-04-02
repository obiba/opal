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

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.obiba.opal.spi.analysis.Analysis;
import org.obiba.opal.spi.analysis.AnalysisResult;
import org.obiba.opal.spi.analysis.AnalysisStatus;

public class OpalAnalysisResult<T extends Analysis>
  extends AbstractTimestamped
  implements AnalysisResult<T, OpalAnalysisResultItem>, HasUniqueProperties {

  private static final String DEFAULT_ID = "empty";

  private String datasource;
  private String table;

  private String analysisName;
  private String id;
  private Date startDate;
  private Date endDate;
  private List<OpalAnalysisResultItem> resultItems;
  private AnalysisStatus status;
  private String message;

  public OpalAnalysisResult() {
    this.id = DEFAULT_ID;
  }

  public OpalAnalysisResult(@NotNull AnalysisResult<T, OpalAnalysisResultItem> analysisResult, String datasource, String table) {
    id = analysisResult.getId();
    analysisName = analysisResult.getAnalysisName();
    startDate = analysisResult.getStartDate();
    endDate = analysisResult.getEndDate();
    resultItems = analysisResult.getResultItems();
    status = analysisResult.getStatus();
    message = analysisResult.getMessage();

    this.datasource = datasource;
    this.table = table;
  }

  public String getDatasource() {
    return datasource;
  }

  public String getTable() {
    return table;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getAnalysisName() {
    return analysisName;
  }

  @Override
  public Date getStartDate() {
    return startDate;
  }

  @Override
  public Date getEndDate() {
    return endDate;
  }

  @Override
  public boolean hasResultItems() {
    return resultItems != null && resultItems.size() > 0;
  }

  @Nullable
  @Override
  public List<OpalAnalysisResultItem> getResultItems() {
    return resultItems;
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("id");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(id);
  }

  @Override
  public AnalysisStatus getStatus() {
    return status;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
