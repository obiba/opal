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

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.service.*;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.model.Projects.OpalAnalysisDto;
import org.obiba.opal.web.model.Projects.OpalAnalysisDto.Builder;
import org.obiba.opal.web.model.Projects.OpalAnalysisResultDto;
import org.obiba.opal.web.model.Projects.OpalAnalysisResultsDto;
import org.obiba.opal.web.project.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Tag(name = "Projects", description = "Operations on projects")
public class TableAnalysisResource {

  private final OpalAnalysisService analysisService;
  private final OpalAnalysisResultService analysisResultService;
  private final AnalysisExportService analysisExportService;

  private String datasourceName;
  private String tableName;

  @Autowired
  public TableAnalysisResource(OpalAnalysisService analysisService,
                                OpalAnalysisResultService analysisResultService,
                                AnalysisExportService analysisExportService) {

    this.analysisService = analysisService;
    this.analysisResultService = analysisResultService;
    this.analysisExportService = analysisExportService;
  }

  @OPTIONS
  @Path("/analyses")
  @Operation(
    summary = "Get table analyses options",
    description = "Returns the allowed HTTP methods for the table analyses endpoint, used for CORS and capability checking."
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
    summary = "Get table analyses",
    description = "Retrieves all analyses associated with a specific data table within a project. Includes analysis metadata, configuration, and the most recent result for each analysis."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Table analyses successfully retrieved"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access table analyses"),
    @ApiResponse(responseCode = "404", description = "Project or table not found")
  })
  public Projects.OpalAnalysesDto getProjectTableAnalyses() {
    return Projects.OpalAnalysesDto.newBuilder()
      .addAllAnalyses(
        StreamSupport
          .stream(analysisService.getAnalysesByDatasourceAndTable(datasourceName, tableName).spliterator(), false)
          .map(analysis -> {
            Builder builder = Dtos.asDto(analysis);
            OpalAnalysisResultDto lastResult = getLastResult(analysis.getName());
            if (lastResult != null) builder.setLastResult(lastResult);
            return builder.build();
          }).collect(Collectors.toList()))
        .build();
  }

  @GET
  @Path("/analysis/{analysisName}")
  @Operation(
    summary = "Get table analysis",
    description = "Retrieves a specific analysis within a table, including analysis configuration and optionally all historical results. Can limit results to only the most recent result."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Analysis successfully retrieved"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access analysis"),
    @ApiResponse(responseCode = "404", description = "Project, table, or analysis not found")
  })
  public OpalAnalysisDto getAnalysis(@PathParam("analysisName") String analysisName,
                                     @QueryParam("lastResult") @DefaultValue("false") boolean lastResult) {

    getAnalysis(analysisName);
    Builder builder = Dtos.asDto(getAnalysis(analysisName));

    builder.addAllAnalysisResults(
      StreamSupport
        .stream(analysisResultService.getAnalysisResults(datasourceName, tableName, analysisName, lastResult).spliterator(), false)
        .map(analysisResult -> Dtos.asDto(analysisResult).build())
        .collect(Collectors.toList()));

    return builder.build();
  }

  @DELETE
  @Path("/analysis/{analysisName}")
  @Operation(
    summary = "Delete table analysis",
    description = "Permanently removes a specific analysis and all its associated results from the table. This action cannot be undone."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Analysis successfully deleted"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to delete analysis"),
    @ApiResponse(responseCode = "404", description = "Project, table, or analysis not found")
  })
  public Response deleteAnalysis(@PathParam("analysisName") String analysisName) {
    analysisService.delete(getAnalysis(analysisName));
    return Response.ok().build();
  }

  @GET
  @Path("/analysis/{analysisName}/results")
  @Operation(
    summary = "Get analysis results",
    description = "Retrieves all results for a specific analysis within a table. Can be limited to only the most recent result for improved performance."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Analysis results successfully retrieved"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access analysis results"),
    @ApiResponse(responseCode = "404", description = "Project, table, or analysis not found")
  })
  public OpalAnalysisResultsDto getAnalysisResults(@PathParam("analysisName") String analysisName,
                                                   @QueryParam("lastResult") @DefaultValue("false") boolean lastResult) {
    return OpalAnalysisResultsDto.newBuilder()
        .addAllAnalysisResults(
          StreamSupport
            .stream(analysisResultService.getAnalysisResults(datasourceName, tableName, analysisName, lastResult).spliterator(), false)
            .map(analysisResult -> Dtos.asDto(analysisResult).build()).collect(Collectors.toList()))
        .build();
  }

  @GET
  @Path("/analysis/{analysisName}/result/{rid}")
  @Operation(
    summary = "Get analysis result",
    description = "Retrieves a specific analysis result by its unique identifier. Includes the complete result data, metadata, and execution information."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Analysis result successfully retrieved"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access analysis result"),
    @ApiResponse(responseCode = "404", description = "Project, table, analysis, or result not found")
  })
  public OpalAnalysisResultDto getAnalysisResult(@PathParam("analysisName") String analysisName, @PathParam("rid") String rid) {
    return Dtos.asDto(analysisResultService.getAnalysisResult(analysisName, rid)).build();
  }

  @DELETE
  @Path("/analysis/{analysisName}/result/{rid}")
  @Operation(
    summary = "Delete analysis result",
    description = "Removes a specific analysis result from an analysis. Returns updated list of remaining results for the analysis."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Analysis result successfully deleted, returns updated results list"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to delete analysis result"),
    @ApiResponse(responseCode = "404", description = "Project, table, analysis, or result not found")
  })
  public OpalAnalysisResultsDto deleteAnalysisResult(@PathParam("analysisName") String analysisName, @PathParam("rid") String rid) {
    analysisResultService.delete(analysisResultService.getAnalysisResult(analysisName, rid));
    return getAnalysisResults(analysisName, false);
  }

  @GET
  @Path("/analysis/{analysisName}/result/{rid}/_export")
  @Operation(
    summary = "Export analysis result",
    description = "Exports a specific analysis result as a ZIP archive containing all result files, data, and metadata. Useful for archiving or sharing analysis results."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Analysis result successfully exported as ZIP file"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to export analysis result"),
    @ApiResponse(responseCode = "404", description = "Project, table, analysis, or result not found"),
    @ApiResponse(responseCode = "500", description = "Error during export process")
  })
  public Response exportAnalysisResult(@PathParam("analysisName") String analysisName, @PathParam("rid") String rid) {
    StreamingOutput outputStream =
        stream -> analysisExportService.exportProjectAnalysisResult(datasourceName, tableName, analysisName, rid, new BufferedOutputStream(stream));

    return analysisZipDownloadResponse(outputStream);
  }

  @GET
  @Path("/analyses/_export")
  @Produces("application/zip")
  @Operation(
    summary = "Export all table analyses",
    description = "Exports all analyses and their results from a specific table as a ZIP archive. Can include all historical results or only successful results based on the 'all' parameter."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "All table analyses successfully exported as ZIP file"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to export table analyses"),
    @ApiResponse(responseCode = "404", description = "Project or table not found"),
    @ApiResponse(responseCode = "500", description = "Error during export process")
  })
  public Response exportTableAnalysis(@QueryParam("all") @DefaultValue("false") boolean all) {
    StreamingOutput outputStream =
      stream -> analysisExportService.exportProjectTableAnalyses(
        datasourceName,
        tableName,
        new BufferedOutputStream(stream),
        !all);

    return analysisZipDownloadResponse(outputStream);
  }

  @GET
  @Path("/analysis/{analysisName}/_export")
  @Produces("application/zip")
  @Operation(
    summary = "Export table analysis",
    description = "Exports a specific analysis and all its results from a table as a ZIP archive. Can include all historical results or only successful results based on the 'all' parameter."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Table analysis successfully exported as ZIP file"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to export analysis"),
    @ApiResponse(responseCode = "404", description = "Project, table, or analysis not found"),
    @ApiResponse(responseCode = "500", description = "Error during export process")
  })
  public Response exportTableAnalysis(@PathParam("analysisName") String analysisName,
                                      @QueryParam("all") @DefaultValue("false") boolean all) {
    getAnalysis(analysisName);

    StreamingOutput outputStream =
      stream -> analysisExportService.exportProjectAnalysis(datasourceName, tableName, analysisName, new BufferedOutputStream(stream), !all);

    return analysisZipDownloadResponse(outputStream);
  }

  @GET
  @Path("/analysis/{analysisName}/result/{resultId}/_report")
  @Produces("application/octet-stream")
  @Operation(
    summary = "Export analysis report",
    description = "Exports the generated report for a specific analysis result. The report format depends on the analysis type and can be PDF, HTML, or other formats. Returns the report as a downloadable file."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Analysis report successfully exported"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access analysis report"),
    @ApiResponse(responseCode = "404", description = "Project, table, analysis, result, or report not found"),
    @ApiResponse(responseCode = "500", description = "Error during report export")
  })
  public Response exportReport(@PathParam("analysisName") String analysisName, @PathParam("resultId") String resultId) {
    StreamingOutput outputStream = stream -> analysisExportService.exportProjectAnalysisResultReport(datasourceName, tableName, analysisName, resultId, new BufferedOutputStream(stream));

    String resultReportExtension = AnalysisExportServiceImpl.getResultReportExtension(datasourceName, tableName, analysisName, resultId);

    if (resultReportExtension != null) {
      String mimeType = new MimetypesFileTypeMap().getContentType(resultReportExtension);

      return Response.ok(outputStream, mimeType)
          .header("Content-Disposition", "inline; filename=\"report" + resultReportExtension + "\"").build();
    }

    return null;
  }

  private Response analysisZipDownloadResponse(StreamingOutput outputStream) {
    String fileName = String.format("%s-%s-analysis.zip", datasourceName, tableName);
    String mimeType = new MimetypesFileTypeMap().getContentType(fileName);
    return Response.ok(outputStream, mimeType)
        .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
  }

  private OpalAnalysis getAnalysis(String analysisName) throws NoSuchAnalysisException {
    return Optional.ofNullable(
      analysisService.getAnalysis(datasourceName, tableName, analysisName)
    ).orElseThrow(() -> new NoSuchAnalysisException(analysisName));
  }

  private OpalAnalysisResultDto getLastResult(String analysisName) {
    Iterable<OpalAnalysisResult> analysisResults = analysisResultService
        .getAnalysisResults(datasourceName, tableName, analysisName, true);

    ArrayList<OpalAnalysisResult> list = Lists.newArrayList(analysisResults);

    if (list.size() > 0) {
      return Dtos.asDto(list.get(0)).build();
    }

    return null;
  }
  
  void setDatasourceName(String value) {
    datasourceName = value;
  }

  void setTableName(String value) {
    tableName = value;
  }
}
