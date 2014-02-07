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

import org.obiba.core.util.FileUtil;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class RReportServiceImpl implements ReportService {

  private static final Logger log = LoggerFactory.getLogger(RReportServiceImpl.class);

  @Value("${OPAL_HOME}")
  private File opalHomeFile;

  @Autowired
  private OpalRSessionManager opalRSessionManager;

  @Autowired
  private OpalRService opalRService;

  @Override
  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput)
      throws ReportException {
    try {
      // prepare the working directory
      String reportOutputName = new File(reportOutput).getName();

      // R server is running locally
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

  //
  // Render on R server
  //

  private String renderWithRServer(Map<String, String> parameters, String reportDesign)
      throws IOException, REXPMismatchException {
    String report = "";
    OpalRSession rSession = null;
    try {
      File reportDesignFile = new File(reportDesign);
      rSession = opalRSessionManager.newSubjectCurrentRSession();
      prepareRSession(rSession, parameters, reportDesignFile);
      runReport(rSession, reportDesignFile.getName());
      report = readFile(rSession, reportDesignFile.getName().replace(".Rmd", ".html"));
      // TODO remote clean working directory
    } finally {
      if(rSession != null) rSession.close();
    }
    return report;
  }

  private void prepareRSession(OpalRSession rSession, Map<String, String> parameters, File reportDesignFile)
      throws IOException {
    // copy all Rmd files to the work directory of the R server
    for (File file : reportDesignFile.getParentFile().listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".Rmd");
      }
    })) {
      writeFile(rSession, file);
    }
    execute(rSession, "require(opal)");
    String options = buildOptions(parameters);
    if(!Strings.isNullOrEmpty(options)) {
      execute(rSession, options);
    }
  }

  private RScriptROperation runReport(OpalRSession rSession, String reportDesign) {
    StringBuilder script = new StringBuilder();
    script.append("opal.report('").append(reportDesign).append("', progress=TRUE)");
    return execute(rSession, script.toString());
  }

  private RScriptROperation execute(OpalRSession rSession, String rscript) {
    log.info(rscript);
    RScriptROperation rop = new RScriptROperation(rscript, false);
    rSession.execute(rop);
    return rop;
  }

  /**
   * Write file content to R side.
   * @param rSession
   * @param file
   * @throws IOException
   */
  private void writeFile(OpalRSession rSession, File file) throws IOException {
    StringBuffer script = new StringBuffer("writeLines(");
    String content = readFileInString(file.getAbsolutePath());
    script.append("'").append(content.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")).append("', '").append(file.getName())
        .append("')");
    execute(rSession, script.toString());
  }

  /**
   * Read file from R side.
   * @param rSession
   * @param name
   * @return
   * @throws REXPMismatchException
   */
  private String readFile(OpalRSession rSession, String name) throws REXPMismatchException {
    String script = "readChar('" + name + "', file.info('" + name + "')$size)";
    RScriptROperation rop = execute(rSession, script);
    return rop.getResult().asString();
  }

  /**
   * Read local file content in a string.
   * @param path
   * @return
   * @throws IOException
   */
  private String readFileInString(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
  }
}
