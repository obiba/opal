package org.obiba.opal.spi.r.analysis;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.opal.spi.analysis.AbstractAnalysisService;
import org.obiba.opal.spi.analysis.AnalysisStatus;
import org.obiba.opal.spi.analysis.AnalysisTemplate;
import org.obiba.opal.spi.analysis.NoSuchAnalysisTemplateException;
import org.obiba.opal.spi.r.FileWriteROperation;
import org.obiba.opal.spi.r.ROperationTemplate;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
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
        ParsedAnalysisResult parsedResult = parseResult(run(analysis));
        analysisResultBuilder.message(parsedResult.getMessage());
        analysisResultBuilder.status(parsedResult.getStatus());
      }

      analysisResultBuilder.end();
      return analysisResultBuilder.build();
    }).collect(Collectors.toList());
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
    if (Files.isRegularFile(template.getRoutinePath())) {
      File routineFile = template.getRoutinePath().toFile();
      FileWriteROperation routineWriteOperation = new FileWriteROperation(routineFile.getName(), routineFile);
      session.execute(routineWriteOperation);
    }

    if (Files.isRegularFile(template.getReportPath())) {
      File reportFile = template.getReportPath().toFile();
      FileWriteROperation reportWriteOperation = new FileWriteROperation(reportFile.getName(), reportFile);
      session.execute(reportWriteOperation);
    }
  }

  protected List<AnalysisTemplate> initAnalysisTemplates() {
    Path templateDirectoryPath = Paths.get(getProperties().getProperty(INSTALL_DIR_PROPERTY), TEMPLATES_DIR)
      .toAbsolutePath();

    if (Files.isDirectory(templateDirectoryPath)) {
      try {
        return Files.list(templateDirectoryPath).filter(p -> Files.isDirectory(p)).map(p -> {
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

  protected abstract REXP run(RAnalysis analysis);

  protected ParsedAnalysisResult parseResult(REXP rexp) {
    try {
      RList rList = rexp.asList();
      return new ParsedAnalysisResult(
        AnalysisStatus.valueOf(((REXPString) rList.get("status")).asString()),
        ((REXPString)rList.get("message")).asString()
      );
    } catch (REXPMismatchException e) {
      log.error("Failed to parse analysis result {}", e);
    }

    return new ParsedAnalysisResult(AnalysisStatus.FAILURE, "");
  }

  private AnalysisTemplate getTemplate(String name) throws NoSuchAnalysisTemplateException {
    return getAnalysisTemplates().stream().filter(t -> name.equals(t.getName())).findFirst()
      .orElseThrow(() -> new NoSuchAnalysisTemplateException(name));
  }

  protected static class ParsedAnalysisResult {
    private String message;
    private final AnalysisStatus status;

    public ParsedAnalysisResult(AnalysisStatus status, String message) {
      this.message = message;
      this.status = status;
    }

    public String getMessage() {
      return message;
    }

    public AnalysisStatus getStatus() {
      return status;
    }
  }
}
