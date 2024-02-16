/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.spi.analysis.Analysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import jakarta.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
  public void exportProjectAnalyses(@Nonnull String projectName,
                                    OutputStream outputStream,
                                    boolean lastResult) throws IOException {
    Assert.isTrue(!Strings.isNullOrEmpty(projectName), "Project name cannot be empty or null.");
    Assert.notNull(outputStream, "outputStream cannot be null.");

    createZip(outputStream, lastResult, Lists.newArrayList(opalAnalysisService.getAnalysesByDatasource(projectName)));
  }

  @Override
  public void exportProjectTableAnalyses(@Nonnull String projectName,
                                         @Nonnull String tableName,
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
  public void exportProjectAnalysis(@Nonnull String projectName,
                                    @Nonnull String tableName,
                                    @Nonnull String analysisName,
                                    @Nonnull OutputStream outputStream,
                                    boolean lastResult) throws IOException {

    Assert.isTrue(!Strings.isNullOrEmpty(analysisName), "Analysis ID cannot be empty or null.");
    Assert.notNull(outputStream, "outputStream cannot be null.");

    createZip(outputStream, lastResult, Lists.newArrayList(opalAnalysisService.getAnalysis(projectName, tableName, analysisName)));
  }

  @Override
  public void exportProjectAnalysisResult(@Nonnull String projectName,
                                          @Nonnull String tableName,
                                          @Nonnull String analysisName,
                                          @Nonnull String resultId,
                                          @Nonnull OutputStream outputStream) throws IOException {

    Assert.isTrue(!Strings.isNullOrEmpty(analysisName), "Analysis ID cannot be empty or null.");
    Assert.isTrue(!Strings.isNullOrEmpty(resultId), "Result ID cannot be empty or null.");

    Assert.notNull(outputStream, "outputStream cannot be null.");

    OpalAnalysisResult analysisResult = opalAnalysisResultService.getAnalysisResult(analysisName, resultId);

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
      zipOutputStream.putNextEntry(
        new ZipEntry(Paths.get("analyses", projectName, tableName, analysisName, "analysis.json").toString())
      );

      zipOutputStream.write(
        new ObjectMapper().writeValueAsBytes(opalAnalysisService.getAnalysis(projectName, tableName, analysisName))
      );

      zipOutputStream.closeEntry();
      createZip(zipOutputStream, analysisName, analysisResult);
    }

  }

  @Override
  public void exportProjectAnalysisResultReport(@Nonnull String projectName,
                                                @Nonnull String tableName,
                                                @Nonnull String analysisName,
                                                @Nonnull String resultId,
                                                @Nonnull OutputStream outputStream) throws IOException {
    Path resultsPath = getResultsPath(projectName, tableName, analysisName, resultId);

      List<Path> tentativeReports = getTentativeReports(resultsPath);

      if (tentativeReports.size() == 0) {
        throw new FileNotFoundException("No Report Files in \"" + resultsPath.toString() + "\"");
      } else if (tentativeReports.size() == 1) {
        Path path = tentativeReports.get(0);
        writeFileToBuffer(outputStream, path);
        outputStream.close();
      } else {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

          for (Path path : tentativeReports) {
            zipOutputStream.putNextEntry(new ZipEntry(path.getFileName().toString()));
            writeFileToBuffer(zipOutputStream, path);
            zipOutputStream.closeEntry();
          }
        }

      }

  }

  private void createZip(@Nonnull OutputStream outputStream, boolean lastResult, List<OpalAnalysis> analyses) throws IOException {
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

      for (OpalAnalysis analyse : analyses) {
        String id = analyse.getName();

        zipOutputStream.putNextEntry(new ZipEntry(Paths.get("analyses", analyse.getDatasource(), analyse.getTable(), id, "analysis.json").toString()));
        zipOutputStream.write(new ObjectMapper().writeValueAsBytes(analyse));
        zipOutputStream.closeEntry();

        for (OpalAnalysisResult result : opalAnalysisResultService.getAnalysisResults(analyse.getDatasource(), analyse.getTable(), id, lastResult)) {
          createZip(zipOutputStream, id, result);
        }
      }
    }
  }

  private void createZip(@Nonnull ZipOutputStream zipOutputStream, @Nonnull String analysisName, @Nonnull OpalAnalysisResult result) throws IOException {
    String resultId = result.getId();
    String resultPath = Paths.get("analyses", result.getDatasource(), result.getTable(), analysisName, "results", resultId).toString();

    zipOutputStream.putNextEntry(new ZipEntry(Paths.get(resultPath, "result.json").toString()));
    zipOutputStream.write(new ObjectMapper().writeValueAsBytes(result));
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

  private void writeFileToBuffer(OutputStream out, Path filePath) throws IOException {
    FileInputStream fileInputStream = new FileInputStream(new File(filePath.toString()));
    int len;
    byte[] buffer = new byte[1024];
    while ((len = fileInputStream.read(buffer)) > 0) {
      out.write(buffer, 0, len);
    }
  }

  private static List<Path> getTentativeReports(Path resultDirectoryPath) {
    try (Stream<Path> pathStream = Files.walk(resultDirectoryPath)) {
      return pathStream
          .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".html") || path.getFileName().toString().toLowerCase().endsWith(".pdf"))
          .collect(Collectors.toList());
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  public static Path getResultsPath(String projectName, String tableName, String analysisName, String resultId) {
    return Paths
        .get(Analysis.ANALYSES_HOME.toAbsolutePath().toString(), projectName, tableName, analysisName, "results", resultId);
  }

  public static String getResultReportExtension(String projectName, String tableName, String analysisName, String resultId) {
    Path resultsPath = getResultsPath(projectName, tableName, analysisName, resultId);

    List<Path> tentativeReports = getTentativeReports(resultsPath);

    if (tentativeReports.size() == 1) {
      return tentativeReports.get(0).getFileName().toString().endsWith(".html") ? ".html" : ".pdf";
    } else if (tentativeReports.size() > 1) {
      return ".zip";
    } else {
      return null;
    }
  }

}
