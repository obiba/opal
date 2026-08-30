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
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.obiba.opal.core.domain.converter.AnalysisStatusConverter;
import org.obiba.opal.core.domain.converter.OpalAnalysisResultItemListConverter;
import org.obiba.opal.spi.analysis.Analysis;
import org.obiba.opal.spi.analysis.AnalysisResult;
import org.obiba.opal.spi.analysis.AnalysisStatus;

@Entity
@Table(name = "opal_analysis_results",
    indexes = {
        @Index(name = "idx_opal_analysis_results_analysis", columnList = "analysis_name"),
        @Index(name = "idx_opal_analysis_results_table", columnList = "datasource, table_name")
    })
public class OpalAnalysisResult<T extends Analysis>
  extends AbstractTimestamped
  implements AnalysisResult<T, OpalAnalysisResultItem> {

  private static final String DEFAULT_ID = "empty";

  private String datasource;

  @Column(name = "table_name")
  private String table;

  @Column(name = "analysis_name")
  private String analysisName;

  /**
   * Assigned by the analysis that produced the result, not generated here, so it is the primary key as it stands.
   */
  @Id
  private String id;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "start_date")
  private Date startDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "end_date")
  private Date endDate;

  @Lob
  @Convert(converter = OpalAnalysisResultItemListConverter.class)
  @Column(name = "result_items")
  private List<OpalAnalysisResultItem> resultItems;

  @Convert(converter = AnalysisStatusConverter.class)
  private AnalysisStatus status;

  @Lob
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
  public AnalysisStatus getStatus() {
    return status;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
