/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.reporting.service.r;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.spi.r.FileReadROperation;
import org.obiba.opal.spi.r.FileWriteROperation;
import org.obiba.opal.spi.r.RScriptROperation;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class RReportServiceImpl implements ReportService {

  private static final Logger log = LoggerFactory.getLogger(RReportServiceImpl.class);

  @Value("${org.obiba.opal.r.repos}")
  private String defaultRepos;

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private RServerManagerService rServerManagerService;

  @Override
  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput)
      throws ReportException {
    try {
      renderWithRServer(parameters, reportDesign, reportOutput);
      log.info("R report done");
    } catch (IOException | REXPMismatchException e) {
      log.error("Unable to render R report", e);
      throw new ReportException(e);
    }
  }

  @Override
  public boolean isRunning() {
    try {
      return rServerManagerService.getDefaultRServer().isRunning();
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void start() {
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

  /**
   * Turns parameters to R options command
   *
   * @param parameters
   * @return
   */
  private String buildOptions(Map<String, String> parameters) {
    StringBuilder script = new StringBuilder();
    script.append("options(");
    boolean appended = false;
    // append other parameters as R options
    if (parameters != null) {
      for (Map.Entry<String, String> param : parameters.entrySet()) {
        String value = param.getValue().trim();
        if (appended) {
          script.append(", ");
        }
        script.append(param.getKey().trim()).append("=").append(toOptionValue(value));
        appended = true;
      }
    }
    script.append(")");
    return script.toString();
  }

  private String toOptionValue(String value) {
    return Pattern.matches("^T$|^TRUE$|^F$|^FALSE$|^NULL$", value) ? value : "'" + value + "'";
  }

  //
  // Render on R server
  //

  private void renderWithRServer(Map<String, String> parameters, String reportDesign, String reportOutput)
      throws IOException, REXPMismatchException {
    RServerSession rSession = null;
    try {
      prepareRServer();
      File reportDesignFile = new File(reportDesign);
      rSession = opalRSessionManager.newSubjectRSession();
      rSession.setExecutionContext("Report");
      prepareRSession(rSession, parameters, reportDesignFile);
      runReport(rSession, reportDesignFile.getName());
      String suffix = reportOutput.substring(reportOutput.lastIndexOf('.'));
      readFileFromR(rSession, reportDesignFile.getName().replace(".Rmd", suffix), reportOutput);
    } finally {
      if (rSession != null) opalRSessionManager.removeSubjectRSession(rSession.getId());
    }
  }

  private void prepareRSession(RServerSession rSession, Map<String, String> parameters, File reportDesignFile) throws IOException {
    File parentFile = reportDesignFile.getParentFile();
    Files.find(Paths.get(parentFile.getPath()),
        Integer.MAX_VALUE,
        (filePath, fileAttr) -> fileAttr.isRegularFile())
        .forEach(path -> writeFileToR(rSession, parentFile, path));
    ensurePackage(rSession, "opalr");
    ensurePackage(rSession, "ggplot2");
    String options = buildOptions(parameters);
    if (!Strings.isNullOrEmpty(options)) {
      execute(rSession, options);
    }
  }

  /**
   * Prepare R server in another session, otherwise newly installed packages could fail loading.
   */
  private void prepareRServer() {
    RServerSession rSession = opalRSessionManager.newSubjectRSession();
    rSession.setExecutionContext("Report");
    ensurePackage(rSession, "opalr");
    ensurePackage(rSession, "ggplot2");
    opalRSessionManager.removeSubjectRSession(rSession.getId());
  }

  private void ensurePackage(RServerSession rSession, String packageName) {
    String repos = StringUtils.collectionToDelimitedString(getDefaultRepos(), ",", "'", "'");
    String cmd = String.format("if (!require(%s)) { install.packages('%s', repos=c(%s), dependencies=TRUE) }",
        packageName, packageName, repos);
    execute(rSession, cmd);
  }

  private RScriptROperation runReport(RServerSession rSession, String reportDesign) {
    StringBuilder script = new StringBuilder();
    script.append("opalr::opal.report('").append(reportDesign).append("', progress=TRUE)");
    return execute(rSession, script.toString());
  }

  private RScriptROperation execute(RServerSession rSession, String rscript) {
    log.debug(rscript);
    RScriptROperation rop = new RScriptROperation(rscript, false);
    rSession.execute(rop);
    return rop;
  }

  /**
   * Write file content to R side.
   *
   * @param rSession
   * @param parentFile
   * @param path
   * @throws IOException
   */
  private void writeFileToR(RServerSession rSession, File parentFile, Path path) {
    String destination = path.toString().replaceFirst(parentFile.getPath() + "/", "");
    FileWriteROperation rop = new FileWriteROperation(destination, path.toFile());
    rSession.execute(rop);
  }

  /**
   * Read file from R side.
   *
   * @param rSession
   * @param name
   * @return
   * @throws REXPMismatchException
   */
  private void readFileFromR(RServerSession rSession, String name, String reportOutput) {
    FileReadROperation rop = new FileReadROperation(name, new File(reportOutput));
    rSession.execute(rop);
  }

  private List<String> getDefaultRepos() {
    return Lists.newArrayList(defaultRepos.split(",")).stream().map(String::trim).collect(Collectors.toList());
  }
}
