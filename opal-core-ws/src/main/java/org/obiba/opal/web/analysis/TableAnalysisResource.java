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
  public Response getAnalysesOptions() {
    return Response.ok().build();
  }

  @GET
  @Path("/analyses")
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
  public Response deleteAnalysis(@PathParam("analysisName") String analysisName) {
    analysisService.delete(getAnalysis(analysisName));
    return Response.ok().build();
  }

  @GET
  @Path("/analysis/{analysisName}/results")
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
  public OpalAnalysisResultDto getAnalysisResult(@PathParam("analysisName") String analysisName, @PathParam("rid") String rid) {
    return Dtos.asDto(analysisResultService.getAnalysisResult(analysisName, rid)).build();
  }

  @DELETE
  @Path("/analysis/{analysisName}/result/{rid}")
  public OpalAnalysisResultsDto deleteAnalysisResult(@PathParam("analysisName") String analysisName, @PathParam("rid") String rid) {
    analysisResultService.delete(analysisResultService.getAnalysisResult(analysisName, rid));
    return getAnalysisResults(analysisName, false);
  }

  @GET
  @Path("/analysis/{analysisName}/result/{rid}/_export")
  public Response exportAnalysisResult(@PathParam("analysisName") String analysisName, @PathParam("rid") String rid) {
    StreamingOutput outputStream =
        stream -> analysisExportService.exportProjectAnalysisResult(datasourceName, tableName, analysisName, rid, new BufferedOutputStream(stream));

    return analysisZipDownloadResponse(outputStream);
  }

  @GET
  @Path("/analyses/_export")
  @Produces("application/zip")
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
