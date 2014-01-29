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
import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  @Override
  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput)
      throws ReportException {

    try {
      String reportOutputName = new File(reportOutput).getName();
      String reportOutputDirName = reportOutputName.substring(0, reportOutputName.lastIndexOf('.'));
      File reportOutputDir = new File(getWorkingDirectory(), reportOutputDirName);
      if(reportOutputDir.exists()) {
        FileUtil.delete(reportOutputDir);
      }
      if (!reportOutputDir.mkdirs()) {
        log.warn("Failed to create directory: {}", reportOutputDir.getAbsolutePath());
      }

      String script = buildReportScript(parameters, reportDesign, reportOutputDir);
      List<String> args = Lists.newArrayList(exec, "--vanilla", "-e", script);
      launchRReportProcess(args, reportOutputDir, reportDesign, reportOutput);

    } catch(InterruptedException e) {
      log.error("Render R report interrupted", e);
      throw new ReportException(e);
    } catch(IOException e) {
      log.error("Unable to render R report", e);
      throw new ReportException(e);
    }
  }

  @Override
  public boolean isRunning() {
    return isEnabled();
  }

  @Override
  public void start() {
    log.info(isEnabled()
        ? "R report service started."
        : "R report service unavailable (R executable or opal R package is missing)");
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
    return !Strings.isNullOrEmpty(exec) && new File(exec).exists() && isOpalPackageInstalled();
  }

  private boolean isOpalPackageInstalled() {
    return true;
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

  private File getLibDirectory() {
    File dir = new File(opalHomeFile, "data" + File.separator + "R" + File.separator + "library");
    if(!dir.exists()) {
      if (!dir.mkdirs()) {
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

  private void launchRReportProcess(List<String> args, File reportOutputDir, String reportDesign, String reportOutput)
      throws IOException, InterruptedException, ReportException {
    Process rProcess = buildRProcess(args, reportOutputDir).start();
    int rProcessStatus = rProcess.waitFor();
    if(rProcessStatus == 0) {
      log.info("R report done");
      String reportName = new File(reportDesign).getName();
      FileUtil.moveFile(new File(reportOutputDir, reportName.replace(".Rmd", ".html")), new File(reportOutput));
      FileUtil.delete(reportOutputDir);
    } else {
      log.error("R report failed with status: {}", rProcessStatus);
      throw new ReportException("R report failed with status: " + rProcessStatus);
    }
  }

  private ProcessBuilder buildRProcess(List<String> args, File workingDirectory) {
    log.info("Starting R report: {}", StringUtil.collectionToString(args, " "));
    ProcessBuilder pb = new ProcessBuilder(args);
    pb.directory(workingDirectory);
    pb.redirectErrorStream(true);
    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(getRreportLog()));
    return pb;
  }

  private String buildReportScript(Map<String, String> parameters,String reportDesign, File reportOutputDir) {
    StringBuilder script = new StringBuilder();

    script.append(".libPaths(append('").append(getLibDirectory().getAbsolutePath()).append("', .libPaths()));require(opal);");

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
      script.append(");");
    }
    script.append("opal.report('").append(reportDesign).append("'").append(", '")
        .append(reportOutputDir.getAbsolutePath()).append("', progress=TRUE);");

    return script.toString();
  }
}
