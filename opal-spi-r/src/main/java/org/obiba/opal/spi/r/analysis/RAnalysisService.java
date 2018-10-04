package org.obiba.opal.spi.r.analysis;

import org.obiba.opal.spi.analysis.AnalysisService;

public interface RAnalysisService extends AnalysisService<RAnalysis, RAnalysisResult> {

  String SERVICE_TYPE = "opal-analysis-r";

}
