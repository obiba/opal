package org.obiba.opal.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.service.OpalAnalysisResultService;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AnalysisResource {

  private final OpalAnalysisService analysisService;
  private final OpalAnalysisResultService analysisResultService;

  @Autowired
  public AnalysisResource(OpalAnalysisService analysisService,
      OpalAnalysisResultService analysisResultService) {
    this.analysisService = analysisService;
    this.analysisResultService = analysisResultService;
  }

  @GET
  @Path("/analysis/{id}")
  public OpalAnalysis getAnalysis(@PathParam("id") String id) {
    return analysisService.getAnalysis(id);
  }

  @GET
  @Path("/analysis/{id}/results")
  public Iterable<OpalAnalysisResult> getAnalysisResults(@PathParam("id") String id) {
    return analysisResultService.getAnalysisResults(id);
  }

  @GET
  @Path("/analysis/{id}/result/{rid}")
  public OpalAnalysisResult getAnalysisResult(String id, String rid) {
    return analysisResultService.getAnalysisResult(id, rid);
  }

}
