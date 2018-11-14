package org.obiba.opal.core.domain;

import com.google.common.collect.Lists;
import java.util.List;
import org.obiba.opal.spi.analysis.AnalysisResult;

public class AnalysisResultWrapper implements HasUniqueProperties {

  private static final String DEFAULT_ID = "empty";

  private String id;
  private AnalysisResult analysisResult;

  public AnalysisResultWrapper() {
    this.id = DEFAULT_ID;
  }

  public AnalysisResultWrapper(AnalysisResult analysisResult) {
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
