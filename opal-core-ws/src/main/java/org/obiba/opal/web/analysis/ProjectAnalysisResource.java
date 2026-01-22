/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.analysis;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.service.AnalysisExportService;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.web.BaseResource;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.project.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.activation.MimetypesFileTypeMap;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.BufferedOutputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Scope("request")
@Path("/project/{name}")
@Tag(name = "Projects", description = "Operations on projects")
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

  @OPTIONS
  @Path("/analyses")
  @Operation(
    summary = "Get project analyses options",
    description = "Returns the allowed HTTP methods for the project analyses endpoint, used for CORS and capability checking."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Options request completed successfully")
  })
  public Response getAnalysesOptions() {
    return Response.ok().build();
  }

  @GET
  @Path("/analyses")
  @Operation(
    summary = "Get project analyses",
    description = "Retrieves all analyses associated with the project, including analysis metadata, status, and results from all data tables within the project."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Project analyses successfully retrieved"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access project analyses"),
    @ApiResponse(responseCode = "404", description = "Project not found")
  })
  public Projects.OpalAnalysesDto getAnalyses() {
    getProject();

    return Projects.OpalAnalysesDto.newBuilder()
      .addAllAnalyses(StreamSupport.stream(analysisService.getAnalysesByDatasource(name).spliterator(), false)
        .map(analysis -> Dtos.asDto(analysis).build()).collect(Collectors.toList())).build();
  }

  @GET
  @Path("/analyses/_export")
  @Produces("application/zip")
  @Operation(
    summary = "Export project analyses",
    description = "Exports all project analyses as a ZIP archive. Can include all analyses or only successful ones based on the 'all' parameter. The archive contains analysis results and metadata."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Project analyses successfully exported as ZIP file"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to export project analyses"),
    @ApiResponse(responseCode = "404", description = "Project not found"),
    @ApiResponse(responseCode = "500", description = "Error during export process")
  })
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
  @Operation(
    summary = "Get table analyses resource",
    description = "Provides access to analyses for a specific data table within the project. Returns a sub-resource for managing table-specific analyses, variables, and statistical computations."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Table analyses resource successfully accessed"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access table analyses"),
    @ApiResponse(responseCode = "404", description = "Project or table not found")
  })
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
