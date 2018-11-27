package org.obiba.opal.web.analysis;

import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.service.AnalysisExportService;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.project.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedOutputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Scope("request")
@Path("/project/{name}")
public class ProjectAnalysisResource {

  @Autowired
  private OpalAnalysisService analysisService;

  @Autowired
  private AnalysisExportService analysisExportService;

  @Autowired
  private ProjectService projectService;

  @Autowired
  private ApplicationContext applicationContext;

  @PathParam("name")
  private String name;


  @GET
  @Path("/analyses")
  public Projects.OpalAnalysesDto getAnalyses() {
    getProject();

    return Projects.OpalAnalysesDto.newBuilder()
      .addAllAnalyses(StreamSupport.stream(analysisService.getAnalysesByDatasource(name).spliterator(), false)
        .map(analysis -> Dtos.asDto(analysis).build()).collect(Collectors.toList())).build();
  }

  @GET
  @Path("/analyses/_export")
  @Produces("application/zip")
  public Response exportProjectAnalysis(@QueryParam("all") @DefaultValue("false") boolean all) {
    getProject();

    StreamingOutput outputStream =
      stream -> analysisExportService.exportProjectAnalyses(name, new BufferedOutputStream(stream), !all);

    String fileName = String.format("%s-analysis.zip", name);
    String mimeType = new MimetypesFileTypeMap().getContentType(fileName);

    return Response.ok(outputStream, mimeType)
      .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
  }

  @Path("/table/{table}")
  public TableAnalysisResource getProjectTableAnalyses(@PathParam("table") String table) {
    Project project = getProject();
    project.getDatasource().getValueTable(table);

    TableAnalysisResource bean = applicationContext.getBean(TableAnalysisResource.class);
    bean.setDatasourceName(name);
    bean.setTableName(table);
    return bean;
  }


  private Project getProject() {
    return projectService.getProject(name);
  }
}
