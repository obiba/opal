package org.obiba.opal.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
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

  private String datasource;
  private String table;

  @Autowired
  public TableAnalysisResource(OpalAnalysisService analysisService,
      OpalAnalysisResultService analysisResultService) {
    this.analysisService = analysisService;
    this.analysisResultService = analysisResultService;
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
  }

  public void setTable(String table) {
    this.table = table;
  }

  @GET
  @Path("/analyses")
  public Response getAnalyses() {
    return Response.ok(analysisService.getAnalysesByDatasourceAndTable(datasource, table)).build();
  }

  @GET
  @Path("/analysis/{id}")
  public Response getAnalysis(@PathParam("id") String id) {
    return Response.ok(analysisService.getAnalysis(id)).build();
  }

  @GET
  @Path("/analysis/{id}/results")
  public Response getAnalysisResults(@PathParam("id") String id) {
    return Response.ok(analysisResultService.getAnalysisResults(id)).build();
  }

  @GET
  @Path("/analysis/{id}/result/{rid}")
  public Response getAnalysisResult(@PathParam("id") String id, @PathParam("rid") String rid) {
    return Response.ok(analysisResultService.getAnalysisResult(id, rid)).build();
  }

}
