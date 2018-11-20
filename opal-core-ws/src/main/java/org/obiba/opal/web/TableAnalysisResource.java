package org.obiba.opal.web;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.service.OpalAnalysisResultService;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.obiba.opal.web.model.Projects.OpalAnalysisDto;
import org.obiba.opal.web.model.Projects.OpalAnalysisDto.Builder;
import org.obiba.opal.web.model.Projects.OpalAnalysisResultDto;
import org.obiba.opal.web.project.Dtos;
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
  public OpalAnalysisDto getAnalysis() {
    Builder builder = Dtos.asDto(analysisService.getAnalysis(analysisId));

    builder.addAllAnalysisResults(StreamSupport.stream(analysisResultService.getAnalysisResults(analysisId).spliterator(), false)
        .map(analysisResult -> Dtos.asDto(analysisResult).build()).collect(Collectors.toList()));

    return builder.build();
  }

  @GET
  @Path("/results")
  public List<OpalAnalysisResultDto> getAnalysisResults() {
    return StreamSupport.stream(analysisResultService.getAnalysisResults(analysisId).spliterator(), false)
        .map(analysisResult -> Dtos.asDto(analysisResult).build()).collect(Collectors.toList());
  }

  @GET
  @Path("/analysis/{id}/result/{rid}")
  public OpalAnalysisResultDto getAnalysisResult(@PathParam("rid") String rid) {
    return Dtos.asDto(analysisResultService.getAnalysisResult(analysisId, rid)).build();
  }

}
