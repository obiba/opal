package org.obiba.opal.core.domain;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import org.obiba.opal.spi.analysis.Analysis;
import org.obiba.opal.spi.analysis.AnalysisResult;
import org.obiba.opal.spi.analysis.AnalysisResultItem;
import org.obiba.opal.spi.analysis.AnalysisStatus;

public class OpalAnalysisResult<T extends Analysis> extends AbstractTimestamped implements AnalysisResult<T>, HasUniqueProperties {

  private static final String DEFAULT_ID = "empty";

  private String datasource;
  private String table;

  private String analysisId;
  private String id;
  private Date startDate;
  private Date endDate;
  private List<AnalysisResultItem> resultItems;
  private AnalysisStatus status;
  private String message;

  public OpalAnalysisResult() {
    this.id = DEFAULT_ID;
  }

  public OpalAnalysisResult(@NotNull AnalysisResult<T> analysisResult, String datasource, String table) {
    id = analysisResult.getId();
    analysisId = analysisResult.getAnalysisId();
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
  public String getAnalysisId() {
    return analysisId;
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
  public List<AnalysisResultItem> getResultItems() {
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
