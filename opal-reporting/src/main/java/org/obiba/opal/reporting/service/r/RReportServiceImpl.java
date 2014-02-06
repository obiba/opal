/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.reporting.service.r;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.obiba.core.util.FileUtil;
import org.obiba.core.util.StringUtil;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component
public class RReportServiceImpl implements ReportService {

  private static final Logger log = LoggerFactory.getLogger(RReportServiceImpl.class);

  @Value("${OPAL_HOME}")
  private File opalHomeFile;

  @Value("${org.obiba.opal.R.exec}")
  private String exec;

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private OpalRService opalRService;

  @Override
  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput)
      throws ReportException {
    File reportWorkingDir = null;

    try {
      // prepare the working directory
      String reportOutputName = new File(reportOutput).getName();
      String reportOutputDirName = reportOutputName.substring(0, reportOutputName.lastIndexOf('.'));
      reportWorkingDir = new File(getWorkingDirectory(), reportOutputDirName);
      if(reportWorkingDir.exists()) {
        FileUtil.delete(reportWorkingDir);
      }
      if(!reportWorkingDir.mkdirs()) {
        log.warn("Failed to create directory: {}", reportWorkingDir.getAbsolutePath());
      }
      if(opalRService.isEnabled()) {
        // R server is running locally
        renderWithRServer(parameters, reportDesign, reportWorkingDir.getAbsolutePath());
      } else {
        // R server is not local, then run a R process for the report
        renderWithRProcess(parameters, reportDesign, reportWorkingDir.getAbsolutePath());
      }
      log.info("R report done");
    } catch(IOException e) {
      log.error("Unable to render R report", e);
      throw new ReportException(e);
    } finally {
      cleanFiles(reportDesign, reportOutput, reportWorkingDir);
    }
  }

  @Override
  public boolean isRunning() {
    return isEnabled();
  }

  @Override
  public void start() {
    log.info(isEnabled() ? "R report service started." : "R report service unavailable (R executable is missing)");
  }

  @Override
  public void stop() {
  }

  @Override
  public String getName() {
    return "report";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  public boolean isEnabled() {
    return !Strings.isNullOrEmpty(exec) && new File(exec).exists();
  }

  /**
   * Turns parameters to R options command
   *
   * @param parameters
   * @return
   */
  private String buildOptions(Map<String, String> parameters) {
    StringBuilder script = new StringBuilder();
    if(parameters.size() > 0) {
      script.append("options(");
      boolean appended = false;
      for(Map.Entry<String, String> param : parameters.entrySet()) {
        String value = param.getValue().trim();
        if(!Pattern.matches("^T$|^TRUE$|^F$|^FALSE$", value)) {
          value = "'" + value + "'";
        }
        if(appended) {
          script.append(", ");
        }
        script.append(param.getKey().trim()).append("=").append(value);
        appended = true;
      }
      script.append(")");
    }
    return script.toString();
  }

  /**
   * Move the produced report to the output directory and delete the working directory.
   *
   * @param reportDesign
   * @param reportOutput
   * @param reportWorkingDir
   * @throws ReportException
   */
  private void cleanFiles(String reportDesign, String reportOutput, File reportWorkingDir) throws ReportException {
    if(reportWorkingDir != null && reportWorkingDir.exists()) {
      try {
        String reportName = new File(reportDesign).getName();
        File report = new File(reportWorkingDir, reportName.replace(".Rmd", ".html"));
        if(report.exists()) {
          FileUtil.moveFile(report, new File(reportOutput));
        }
        FileUtil.delete(reportWorkingDir);
      } catch(IOException e) {
        throw new ReportException(e);
      }
    }
  }

  private File getWorkingDirectory() {
    File dir = new File(opalHomeFile, "work" + File.separator + "R" + "report");
    if(!dir.exists()) {
      if(!dir.mkdirs()) {
        log.error("Unable to create: {}", dir.getAbsolutePath());
      }
    }
    return dir;
  }

  //
  // Render with R process
  //

  private void renderWithRProcess(Map<String, String> parameters, String reportDesign, String reportOutputDir)
      throws ReportException {
    try {
      String script = buildReportScript(parameters, reportDesign, reportOutputDir);
      List<String> args = Lists.newArrayList(exec, "--vanilla", "-e", script);
      launchRReportProcess(args, reportOutputDir);
    } catch(InterruptedException | IOException e) {
      log.error("Render R report interrupted", e);
      throw new ReportException(e);
    }
  }

  private File getLibDirectory() {
    File dir = new File(opalHomeFile, "data" + File.separator + "R" + File.separator + "library");
    if(!dir.exists()) {
      if(!dir.mkdirs()) {
        log.error("Unable to create: {}", dir.getAbsolutePath());
      }
    }
    return dir;
  }

  private File getRreportLog() {
    File logFile = new File(opalHomeFile, "logs" + File.separator + "Rreport.log");
    if(!logFile.getParentFile().exists()) {
      if(!logFile.getParentFile().mkdirs()) {
        log.error("Unable to create: {}", logFile.getParentFile().getAbsolutePath());
      }
    }
    return logFile;
  }

  private void launchRReportProcess(List<String> args, String reportOutputDir)
      throws IOException, InterruptedException, ReportException {
    Process rProcess = buildRProcess(args, reportOutputDir).start();
    int rProcessStatus = rProcess.waitFor();
    if(rProcessStatus != 0) {
      log.error("R report failed with status: {}", rProcessStatus);
      throw new ReportException("R report failed with status: " + rProcessStatus);
    }
  }

  private ProcessBuilder buildRProcess(List<String> args, String workingDirectory) {
    log.debug("Starting R report: {}", StringUtil.collectionToString(args, " "));
    ProcessBuilder pb = new ProcessBuilder(args);
    pb.directory(new File(workingDirectory));
    pb.redirectErrorStream(true);
    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(getRreportLog()));
    return pb;
  }

  private String buildReportScript(Map<String, String> parameters, String reportDesign, String reportWorkingDir) {
    StringBuilder script = new StringBuilder();

    script.append(".libPaths(append('").append(getLibDirectory().getAbsolutePath())
        .append("', .libPaths()));require(opal);");

    String options = buildOptions(parameters);
    if(!Strings.isNullOrEmpty(options)) {
      script.append(options).append(";");
    }

    script.append("opal.report('").append(reportDesign).append("'").append(", '").append(reportWorkingDir)
        .append("', progress=TRUE);");

    return script.toString();
  }

  //
  // Render on R server
  //

  private boolean renderWithRServer(Map<String, String> parameters, String reportDesign, String reportWorkingDir) {
    OpalRSession rSession = null;
    try {
      rSession = opalRSessionManager.newSubjectCurrentRSession();
      prepareRSession(rSession, parameters);
      runReport(rSession, reportDesign, reportWorkingDir);
    } finally {
      if(rSession != null) rSession.close();
    }
    return true;
  }

  private void prepareRSession(OpalRSession rSession, Map<String, String> parameters) {
    execute(rSession, "require(opal)");
    String options = buildOptions(parameters);
    if(!Strings.isNullOrEmpty(options)) {
      execute(rSession, options);
    }
  }

  private RScriptROperation runReport(OpalRSession rSession, String reportDesign, String reportWorkingDir) {
    StringBuilder script = new StringBuilder();
    script.append("opal.report('").append(reportDesign).append("'").append(", '").append(reportWorkingDir)
        .append("', progress=TRUE)");
    return execute(rSession, script.toString());
  }

  private RScriptROperation execute(OpalRSession rSession, String rscript) {
    log.info(rscript);
    RScriptROperation rop = new RScriptROperation(rscript, false);
    rSession.execute(rop);
    return rop;
  }
}
