package org.obiba.opal.core.domain;

import com.google.common.collect.Lists;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.obiba.opal.spi.analysis.Analysis;

public class OpalAnalysis implements HasUniqueProperties {

  private static final String DEFAULT_ID = "empty";

  private String id;
  private Analysis analysis;

  public OpalAnalysis() {
    this.id = DEFAULT_ID;
  }

  public OpalAnalysis(Analysis analysis) {
    setAnalysis(analysis);
  }

  public String getId() {
    return id;
  }

  public Analysis getAnalysis() {
    return analysis;
  }

  public void setAnalysis(@NotNull Analysis analysis) {
    this.analysis = analysis;
    this.id = analysis.getId();
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
