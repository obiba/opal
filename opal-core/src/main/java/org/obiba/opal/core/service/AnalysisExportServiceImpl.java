package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sun.istack.NotNull;
import org.json.JSONObject;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class AnalysisExportServiceImpl implements AnalysisExportService {

  private static final Logger log = LoggerFactory.getLogger(AnalysisExportServiceImpl.class);

  @Autowired
  private OpalAnalysisService opalAnalysisService;

  @Autowired
  private OpalAnalysisResultService opalAnalysisResultService;


  @Override
  public void exportProjectAnalyses(@NotNull String projectName,
                                    OutputStream outputStream,
                                    boolean lastResult) throws IOException {
    Assert.isTrue(!Strings.isNullOrEmpty(projectName), "Project name cannot be empty or null.");
    Assert.notNull(outputStream, "outputStream cannot be null.");

    createZip(outputStream, lastResult, Lists.newArrayList(opalAnalysisService.getAnalysesByDatasource(projectName)));
  }

  @Override
  public void exportProjectTableAnalyses(@NotNull String projectName,
                                         @NotNull String tableName,
                                         OutputStream outputStream,
                                         boolean lastResult) throws IOException {
    Assert.isTrue(!Strings.isNullOrEmpty(projectName), "Project name cannot be empty or null.");
    Assert.isTrue(!Strings.isNullOrEmpty(tableName), "Table name cannot be empty or null.");
    Assert.notNull(outputStream, "outputStream cannot be null.");

    createZip(outputStream,
      lastResult,
      Lists.newArrayList(opalAnalysisService.getAnalysesByDatasourceAndTable(projectName, tableName))
    );
  }

  @Override
  public void exportProjectAnalysis(@NotNull String projectName,
                                    @NotNull String tableName,
                                    @NotNull String analysisId,
                                    @NotNull OutputStream outputStream,
                                    boolean lastResult) throws IOException {

    Assert.isTrue(!Strings.isNullOrEmpty(analysisId), "Analysis ID cannot be empty or null.");
    Assert.notNull(outputStream, "outputStream cannot be null.");

    createZip(outputStream, lastResult, Lists.newArrayList(opalAnalysisService.getAnalysis(projectName, tableName, analysisId)));
  }

  @Override
  public void exportProjectAnalysisResult(@NotNull String projectName,
      @NotNull String tableName, String analysisId, String resultId,
      OutputStream outputStream) throws IOException {

    Assert.isTrue(!Strings.isNullOrEmpty(analysisId), "Analysis ID cannot be empty or null.");
    Assert.isTrue(!Strings.isNullOrEmpty(resultId), "Result ID cannot be empty or null.");

    Assert.notNull(outputStream, "outputStream cannot be null.");

    OpalAnalysisResult analysisResult = opalAnalysisResultService.getAnalysisResult(analysisId, resultId);

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
      zipOutputStream.putNextEntry(new ZipEntry(Paths.get("analyses", analysisId, "analysis.json").toString()));
      zipOutputStream.write(new JSONObject(opalAnalysisService.getAnalysis(projectName, tableName, analysisId)).toString().getBytes());
      zipOutputStream.closeEntry();

      createZip(zipOutputStream, analysisId, analysisResult);
    }

  }

  private void createZip(@NotNull OutputStream outputStream, boolean lastResult, List<OpalAnalysis> analyses) throws IOException {
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

      for (OpalAnalysis analyse : analyses) {
        String id = analyse.getId();

        zipOutputStream.putNextEntry(new ZipEntry(Paths.get("analyses", id, "analysis.json").toString()));
        zipOutputStream.write(new JSONObject(analyse).toString().getBytes());
        zipOutputStream.closeEntry();

        for (OpalAnalysisResult result : opalAnalysisResultService.getAnalysisResults(id, lastResult)) {
          createZip(zipOutputStream, id, result);
        }
      }
    }
  }

  private void createZip(@NotNull ZipOutputStream zipOutputStream, @NotNull String analysisId, @NotNull OpalAnalysisResult result) throws IOException {
    String resultId = result.getId();
    String resultPath = Paths.get("analyses", analysisId, "results", resultId).toString();

    zipOutputStream.putNextEntry(new ZipEntry(Paths.get(resultPath, "result.json").toString()));
    zipOutputStream.write(new JSONObject(result).toString().getBytes());
    zipOutputStream.closeEntry();

    writeResultDataFiles(zipOutputStream, resultPath);
  }

  private void writeResultDataFiles(ZipOutputStream out, String resultPath) throws IOException {
    Path resultPathFull = Paths.get(System.getProperty("OPAL_HOME"), "data", resultPath);

    try (Stream<Path> paths = Files.walk(resultPathFull)) {
      paths
        .filter(Files::isRegularFile)
        .forEach(filePath -> {
          try {
            out.putNextEntry(new ZipEntry(
              Paths.get(resultPath, resultPathFull.toUri().relativize(filePath.toUri()).toString()).toString()
            ));
            writeFileToBuffer(out, filePath);
            out.closeEntry();
          } catch (IOException e) {
            log.error("Failed to write result data file {}", filePath);
          }
        });
    }
  }

  private void writeFileToBuffer(ZipOutputStream out, Path filePath) throws IOException {
    FileInputStream fileInputStream = new FileInputStream(new File(filePath.toString()));
    int len;
    byte[] buffer = new byte[1024];
    while ((len = fileInputStream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
  }

}
