/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.reporting.service.birt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.obiba.opal.reporting.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
// @Component
public class BasicBirtReportServiceImpl implements ReportService {

  private static final Logger log = LoggerFactory.getLogger(BasicBirtReportServiceImpl.class);

  final static String OPAL_HOME_SYSTEM_PROPERTY_NAME = "OPAL_HOME";

  final static String BIRT_HOME_SYSTEM_PROPERTY_NAME = "BIRT_HOME";

  private String birtExec;

  private boolean running = false;

  @Override
  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput) {
    Runtime r = Runtime.getRuntime();

    try {
      Process p = r.exec(getBirtCommandLine(format, parameters, reportDesign, reportOutput));

      OutputPurger outputErrorPurger = new OutputPurger(p.getErrorStream());
      OutputPurger outputPurger = new OutputPurger(p.getInputStream());
      outputErrorPurger.start();
      outputPurger.start();

      // Check for birt failure
      try {
        if(p.waitFor() != 0) {
          throw new RuntimeException("BIRT executable reported an error.  Exit value = " + p.exitValue());
        }
      } catch(InterruptedException e) {
        throw new RuntimeException(e);
      }
    } catch(IOException e) {
      throw new RuntimeException("BIRT could not be executed.", e);
    }
  }

  private String getBirtCommandLine(String format, Map<String, String> parameters, String reportDesign, String reportOutput) {
    StringBuffer cmd = new StringBuffer(birtExec);

    if(format != null) {
      cmd.append(" -f ").append(format);
    }

    if(parameters != null && parameters.size() > 0) {
      cmd.append(" -p");
      for(Entry<String, String> entry : parameters.entrySet()) {
        cmd.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
      }
    }

    cmd.append(" -o ").append(reportOutput);

    cmd.append(" ").append(reportDesign);

    return cmd.toString();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    birtExec = System.getProperty(BIRT_HOME_SYSTEM_PROPERTY_NAME) + File.separator + "ReportEngine" + File.separator + "genReport";
    if(System.getProperty("os.name").contains("Windows")) {
      birtExec = "cmd.exe /c " + birtExec;
      birtExec += ".bat";
    } else {
      birtExec += ".sh";
    }

    log.info("birtExec=" + birtExec);
    running = true;
  }

  @Override
  public void stop() {
    running = false;
  }

  static class OutputPurger extends Thread {

    InputStream inputStream;

    OutputPurger(InputStream pInputStream) {
      inputStream = pInputStream;
    }

    public void run() {
      try {
        InputStreamReader isReader = new InputStreamReader(inputStream);
        BufferedReader bufReader = new BufferedReader(isReader);

        while(true) {
          String line = bufReader.readLine();
          if(line == null) {
            break;
          }
        }

      } catch(IOException ex) {
      }
    }
  }
}
