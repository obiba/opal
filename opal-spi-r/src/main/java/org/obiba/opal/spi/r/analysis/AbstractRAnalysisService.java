/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.analysis;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.opal.spi.analysis.*;
import org.obiba.opal.spi.r.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class AbstractRAnalysisService extends AbstractAnalysisService<RAnalysis, RAnalysisResult> implements RAnalysisService {

  private static final Logger log = LoggerFactory.getLogger(AbstractRAnalysisService.class);

  private static final String TEMPLATES_DIR = "templates";
  private static final String SCHEMA_FORM_FILE_NAME = "form.json";
  private static final String ROUTINE_FILE_NAME = "routine.R";
  private static final String REPORT_FILE_NAME = "report.Rmd";

  protected Properties properties;

  protected boolean running;

  protected List<AnalysisTemplate> analysisTemplates;


  @Override
  public List<AnalysisTemplate> getAnalysisTemplates() {
    return analysisTemplates;
  }

  @Override
  public List<RAnalysisResult> analyse(List<RAnalysis> analyses) throws NoSuchAnalysisTemplateException {
    return analyses.stream().map(analysis -> {
      RAnalysisResult.Builder analysisResultBuilder = RAnalysisResult.create(analysis);
      AnalysisTemplateImpl template = (AnalysisTemplateImpl) getTemplate(analysis.getTemplateName());

      analysisResultBuilder.start();
      if (!Files.isRegularFile(template.getRoutinePath()) && !Files.isRegularFile(template.getReportPath())) {
        analysisResultBuilder.status(AnalysisStatus.IGNORED.name());
        analysisResultBuilder.message("No analysis to run.");
      } else {
        prepare(analysis.getSession(), template);
        RServerResult rexp = run(analysis);
        createAnalysisResult(analysisResultBuilder, parseResult(rexp));

        Path fileName = template.getReportPath().getFileName();
        Path reportPath = generateReportPath(analysis, analysisResultBuilder.getResultId(), fileName);
        analysisResultBuilder.report(reportPath.toString());
        downloadFilesFromRSession(analysis.getSession(), generateAbsoluteReportPath(reportPath.getParent()));
      }
      analysisResultBuilder.end();

      return analysisResultBuilder.build();
    }).collect(Collectors.toList());
  }

  private void createAnalysisResult(RAnalysisResult.Builder analysisResultBuilder, JSONObject parsedResult) {
    analysisResultBuilder.message(parsedResult.optString("message", ""));
    analysisResultBuilder.status(
        AnalysisStatus.valueOf(parsedResult.optString("status", "ERROR").toUpperCase())
    );

    JSONArray items = parsedResult.getJSONArray("items");
    List<AnalysisResultItem> resultItems = Lists.newArrayList();

    for (int i = 0, length = items.length(); i < length; i++) {
      JSONObject itemJson = items.getJSONObject(i);
      resultItems.add(
          new RAnalysisResult.RAnalysisResultItem(
              AnalysisStatus.valueOf(itemJson.optString("status", "ERROR").toUpperCase()),
              itemJson.optString("message", "")
          )
      );
    }
    analysisResultBuilder.items(resultItems);
  }


  private Path generateReportPath(RAnalysis analysis, String resultId, Path fileName) {
    AnalysisReportType reportType = AnalysisReportType.safeValueOf(analysis.getParameters().optString("reportType"));
    String reportFileName = fileName.toString().replaceAll("Rmd", reportType.toString().toLowerCase());
    return Paths.get(analysis.getDatasource(), analysis.getTable(), analysis.getName(), "results", resultId, reportFileName);
  }

  private Path generateAbsoluteReportPath(Path reportFileName) {
    return Paths.get(Analysis.ANALYSES_HOME.toString(), reportFileName.toString());
  }

  @Override
  public Properties getProperties() {
    return properties;
  }

  @Override
  public void configure(Properties properties) {
    this.properties = properties;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    running = true;
    analysisTemplates = initAnalysisTemplates();
  }

  @Override
  public void stop() {
    running = false;
  }

  protected void prepare(ROperationTemplate session, AnalysisTemplate template) {
    uploadFileToRSession(session, template.getRoutinePath());
    uploadFileToRSession(session, template.getReportPath());
  }

  private void downloadFilesFromRSession(ROperationTemplate session, Path path) {
    String rscript = "list.files(path = '.', recursive = TRUE)";
    RScriptROperation rop = new RScriptROperation(rscript, false);
    session.execute(rop);
    if (!rop.hasResult()) return;
    String[] files = rop.getResult().asStrings();
    Lists.newArrayList(files)
        .stream()
        .filter(file -> !REPORT_FILE_NAME.equals(file) && !ROUTINE_FILE_NAME.equals(file))
        .forEach(file -> downloadFileToFromRSession(session, Paths.get(path.toString(), file)));
  }

  private void uploadFileToRSession(ROperationTemplate session, Path path) {
    if (Files.isRegularFile(path)) {
      File routineFile = path.toFile();
      FileWriteROperation routineWriteOperation = new FileWriteROperation(routineFile.getName(), routineFile);
      session.execute(routineWriteOperation);
    }
  }

  private void downloadFileToFromRSession(ROperationTemplate session, Path path) {
    File reportDir = path.getParent().toFile();
    if (!reportDir.exists()) {
      if (!reportDir.mkdirs()) {
        log.error("Failed to create report folder {}", path);
        return;
      }
    }

    FileReadROperation fileReadROperation = new FileReadROperation(path.getFileName().toString(), path.toFile());
    session.execute(fileReadROperation);
  }

  protected List<AnalysisTemplate> initAnalysisTemplates() {
    Path templateDirectoryPath = Paths.get(getProperties().getProperty(INSTALL_DIR_PROPERTY), TEMPLATES_DIR)
        .toAbsolutePath();

    if (Files.isDirectory(templateDirectoryPath)) {
      try {
        return Files.list(templateDirectoryPath).filter(Files::isDirectory).map(p -> {
          AnalysisTemplateImpl analysisTemplate = new AnalysisTemplateImpl(p.getFileName().toString());

          Path schemaFormPath = Paths.get(p.toString(), SCHEMA_FORM_FILE_NAME);
          analysisTemplate.setRoutinePath(Paths.get(p.toString(), ROUTINE_FILE_NAME));
          analysisTemplate.setReportPath(Paths.get(p.toString(), REPORT_FILE_NAME));

          if (Files.isRegularFile(schemaFormPath)) {
            try {
              String schemaForm = Files.lines(schemaFormPath).reduce("", String::concat).trim();
              analysisTemplate
                  .setSchemaForm(new JSONObject(Strings.isNullOrEmpty(schemaForm) ? "{}" : schemaForm));

              analysisTemplate.setTitle(analysisTemplate.getJSONSchemaForm().optString("title"));
              analysisTemplate.setDescription(analysisTemplate.getJSONSchemaForm().optString("description"));
            } catch (IOException e) {
              log.error("Error reading file at path {}", schemaFormPath);
            }
          }

          return analysisTemplate;
        }).collect(Collectors.toList());
      } catch (IOException e) {
        log.error("No templates directory.");
      }
    }

    return Lists.newArrayList();
  }

  protected abstract RServerResult run(RAnalysis analysis);

  JSONObject parseResult(RServerResult result) {
    try {
      return new JSONObject(result.asStrings()[0]);
    } catch (Exception e) {
      log.error("Failed to parse analysis result", e);
    }

    return new JSONObject().put("status", AnalysisStatus.ERROR.toString());
  }

  private AnalysisTemplate getTemplate(String name) throws NoSuchAnalysisTemplateException {
    return getAnalysisTemplates().stream().filter(t -> name.equals(t.getName())).findFirst()
        .orElseThrow(() -> new NoSuchAnalysisTemplateException(name));
  }

}
