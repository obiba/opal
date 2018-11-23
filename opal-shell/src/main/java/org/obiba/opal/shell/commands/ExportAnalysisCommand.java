package org.obiba.opal.shell.commands;

import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.json.JSONObject;
import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.domain.OpalAnalysisResult;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.OpalAnalysisResultService;
import org.obiba.opal.core.service.OpalAnalysisService;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.shell.commands.options.ExportAnalysisCommandOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@CommandUsage(
  description = "Exports analysis resuults and associated file as a ZIP bundle.",
  syntax = "Syntax: analyse [--project PROJECT] [--tables TABLE-LIST]")
public class ExportAnalysisCommand extends AbstractOpalRuntimeDependentCommand<ExportAnalysisCommandOptions> {
  private static final Logger log = LoggerFactory.getLogger(ExportAnalysisCommand.class);

  private static final SimpleDateFormat TIMESTAMPT_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private ProjectService projectService;

  @Autowired
  private OpalAnalysisService opalAnalysisService;

  @Autowired
  private OpalAnalysisResultService opalAnalysisResultService;

  @Override
  public int execute() {
    String projectName = options.getProject();
    projectService.getProject(projectName); // validation

    List<OpalAnalysis> analyses = options.getTables().isEmpty()
      ? Lists.newArrayList(opalAnalysisService.getAnalysesByDatasource(projectName))
      : mergeTableAnalyses(projectName);

    if (analyses.isEmpty()) {
      log.info("There are no analysis for project: {} tables: {}", projectName, options.getTables());
      return 0;
    }

    Path exportFile = getOpalFsPath(projectName);

    try (FileOutputStream fileOutStream = new FileOutputStream(exportFile.toFile())) {
      ZipOutputStream out = new ZipOutputStream(fileOutStream);

      for (OpalAnalysis analyse : analyses) {
        String analysisId = analyse.getId();

        out.putNextEntry(new ZipEntry(Paths.get("analyses", analysisId, "analysis.json").toString()));
        out.write(new JSONObject(analyse).toString().getBytes());
        out.closeEntry();

        for (OpalAnalysisResult result : opalAnalysisResultService.getAnalysisResults(analysisId)) {
          String resultId = result.getId();
          String resultPath = Paths.get("analyses", analysisId, "results", resultId).toString();

          out.putNextEntry(new ZipEntry(Paths.get(resultPath, "result.json").toString()));
          out.write(new JSONObject(result).toString().getBytes());
          out.closeEntry();

          writeResultDataFiles(out, resultPath);
        }
      }

    } catch (IOException e) {
      return 1;
    }

    return 0;
  }

  private Path getOpalFsPath(String projectName) {
    String principal = SecurityUtils.getSubject().getPrincipal().toString();
    String exportFileName = String.format("%s-analysis-%s.zip", projectName, TIMESTAMPT_FORMAT.format(System.currentTimeMillis()));
    Path exportPath = Paths.get("home", principal, "export");
    File exportFolder = opalRuntime.getFileSystem().resolveLocalFile(exportPath.toString());
    exportFolder.mkdirs();
    return Paths.get(exportFolder.toString(), exportFileName);
  }

  private void writeResultDataFiles(ZipOutputStream out, String resultPath) throws IOException {
    Path resultPathFull = Paths.get(System.getProperty("OPAL_HOME"), "data", resultPath);

    try (Stream<Path> paths = Files.walk(resultPathFull)) {
      paths
        .filter(Files::isRegularFile)
        .forEach(filePath -> {
          try {
            out.putNextEntry(new ZipEntry(
              Paths.get(resultPath ,resultPathFull.toUri().relativize(filePath.toUri()).toString()).toString()
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

  private List<OpalAnalysis> mergeTableAnalyses(final String projectName) {
    return options.getTables()
      .stream()
      .map(table -> StreamSupport.stream(opalAnalysisService.getAnalysesByDatasourceAndTable(projectName, table).spliterator(), false)
        .collect(Collectors.toList())
      ).flatMap(Collection::stream)
      .collect(Collectors.toList());
  }
}
