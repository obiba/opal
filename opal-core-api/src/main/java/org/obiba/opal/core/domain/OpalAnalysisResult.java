package org.obiba.opal.core.domain;

import com.google.common.collect.Lists;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.obiba.opal.spi.analysis.AnalysisResult;

public class OpalAnalysisResult implements HasUniqueProperties {

  private static final String DEFAULT_ID = "empty";

  private String id;
  private AnalysisResult analysisResult;

  public OpalAnalysisResult() {
    this.id = DEFAULT_ID;
  }

  public OpalAnalysisResult(@NotNull AnalysisResult analysisResult) {
    setAnalysisResult(analysisResult);
  }

  public String getId() {
    return id;
  }

  public AnalysisResult getAnalysisResult() {
    return analysisResult;
  }

  public void setAnalysisResult(AnalysisResult analysisResult) {
    this.analysisResult = analysisResult;
    this.id = analysisResult.getId();
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("id");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.newArrayList(id);
  }
}
