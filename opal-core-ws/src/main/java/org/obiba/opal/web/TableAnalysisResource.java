package org.obiba.opal.web;

import com.google.common.collect.Lists;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.opal.core.service.OpalAnalysisResultService;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TableAnalysisResource {

  private final OpalAnalysisService analysisService;
  private final OpalAnalysisResultService analysisResultService;

  private String analysisId;

  @Autowired
  public TableAnalysisResource(OpalAnalysisService analysisService,
      OpalAnalysisResultService analysisResultService) {
    this.analysisService = analysisService;
    this.analysisResultService = analysisResultService;
  }

  public void setAnalysisId(String analysisId) {
    this.analysisId = analysisId;
  }

  @GET
  public String getAnalysis() {
    return new JSONObject(analysisService.getAnalysis(analysisId)).toString();
  }

  @GET
  @Path("/results")
  public String getAnalysisResults() {
    return new JSONArray(Lists.newArrayList(analysisResultService.getAnalysisResults(analysisId))).toString();
  }

  @GET
  @Path("/result/{rid}")
  public String getAnalysisResult(@PathParam("rid") String rid) {
    return new JSONObject(analysisResultService.getAnalysisResult(analysisId, rid)).toString();
  }

}
