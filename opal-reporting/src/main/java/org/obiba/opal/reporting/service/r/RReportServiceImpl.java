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
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class RReportServiceImpl implements ReportService {

  private static final Logger log = LoggerFactory.getLogger(RReportServiceImpl.class);

  private static final String REPORT_STYLE_OPTION = "opal.report.style";

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private OpalRService opalRService;

  @Override
  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput)
      throws ReportException {
    try {
      String report = renderWithRServer(parameters, reportDesign);
      Files.write(Paths.get(reportOutput), report.getBytes());
      log.info("R report done");
    } catch(IOException | REXPMismatchException e) {
      log.error("Unable to render R report", e);
      throw new ReportException(e);
    }
  }

  @Override
  public boolean isRunning() {
    return opalRService.isRunning();
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
    if(parameters != null) {
      for(Map.Entry<String, String> param : parameters.entrySet()) {
        String value = param.getValue().trim();
        if(appended) {
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

  private String renderWithRServer(Map<String, String> parameters, String reportDesign)
      throws IOException, REXPMismatchException {
    String report = "";
    OpalRSession rSession = null;
    try {
      File reportDesignFile = new File(reportDesign);
      rSession = opalRSessionManager.newSubjectRSession();
      String originalWorkDir = getRWorkDir(rSession);
      prepareRSession(rSession, parameters, reportDesignFile);
      runReport(rSession, reportDesignFile.getName());
      report = readFileFromR(rSession, reportDesignFile.getName().replace(".Rmd", ".html"));
      cleanRWorkDir(rSession, originalWorkDir);
    } finally {
      if(rSession != null) opalRSessionManager.removeSubjectRSession(rSession.getId());
    }
    return report;
  }

  private void prepareRSession(OpalRSession rSession, Map<String, String> parameters, File reportDesignFile)
      throws IOException {
    // copy all Rmd files to the work directory of the R server
    for(File file : reportDesignFile.getParentFile().listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".Rmd");
      }
    })) {
      writeFileToR(rSession, file);
    }
    execute(rSession, "require(opal)");
    String options = buildOptions(parameters);
    if(!Strings.isNullOrEmpty(options)) {
      execute(rSession, options);
    }
  }

  private RScriptROperation runReport(OpalRSession rSession, String reportDesign) {
    StringBuilder script = new StringBuilder();
    script.append("opal.report('").append(reportDesign).append("', boot_style=getOption('").append(REPORT_STYLE_OPTION)
        .append("'),  progress=TRUE)");
    return execute(rSession, script.toString());
  }

  private RScriptROperation execute(OpalRSession rSession, String rscript) {
    log.debug(rscript);
    RScriptROperation rop = new RScriptROperation(rscript, false);
    rSession.execute(rop);
    return rop;
  }

  /**
   * Write file content to R side.
   *
   * @param rSession
   * @param file
   * @throws IOException
   */
  private void writeFileToR(OpalRSession rSession, File file) throws IOException {
    StringBuffer script = new StringBuffer("writeLines(");
    String content = readFileInString(file.getAbsolutePath());
    script.append("'").append(content.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")).append("', '")
        .append(file.getName()).append("')");
    execute(rSession, script.toString());
  }

  /**
   * Read file from R side.
   *
   * @param rSession
   * @param name
   * @return
   * @throws REXPMismatchException
   */
  private String readFileFromR(OpalRSession rSession, String name) throws REXPMismatchException {
    String script = "readChar('" + name + "', file.info('" + name + "')$size)";
    RScriptROperation rop = execute(rSession, script);
    return rop.getResult().asString();
  }

  private String getRWorkDir(OpalRSession rSession) throws REXPMismatchException {
    String script = "getwd()";
    RScriptROperation rop = execute(rSession, script);
    return rop.getResult().asString();
  }

  private void cleanRWorkDir(OpalRSession rSession, String workDir) {
    String script = "unlink('" + workDir + "', recursive=TRUE)";
    execute(rSession, script);
  }

  /**
   * Read local file content in a string.
   *
   * @param path
   * @return
   * @throws IOException
   */
  private String readFileInString(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
  }
}
