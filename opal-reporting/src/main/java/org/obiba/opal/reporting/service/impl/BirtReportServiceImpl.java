/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.reporting.service.impl;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class BirtReportServiceImpl implements ReportService {

  private static final Logger log = LoggerFactory.getLogger(BirtReportServiceImpl.class);

  private static final String OPAL_HOME_SYSTEM_PROPERTY_NAME = "OPAL_HOME";

  private static final String BIRT_HOME_SYSTEM_PROPERTY_NAME = "BIRT_HOME";

  private IReportEngine engine;

  @SuppressWarnings("unchecked")
  @Override
  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput) throws ReportException {
    if(isRunning() == false) {
      throw new ReportException("Report engine not running. Please check startup logs for details.");
    }

    try {
      // Open the report design
      IReportRunnable design = engine.openReportDesign(reportDesign);

      // Create task to run and render the report,
      IRunAndRenderTask task = engine.createRunAndRenderTask(design);
      // Set parent classloader for engine
      task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, BirtReportServiceImpl.class.getClassLoader());

      // Set parameter values and validate
      if(parameters != null) {
        for(Entry<String, String> entry : parameters.entrySet()) {
          task.setParameterValue(entry.getKey(), entry.getValue());
        }
      }
      task.validateParameters();

      RenderOption options = getOptions(format);
      options.setOutputFileName(reportOutput);
      task.setRenderOption(options);

      // Run and render report
      task.run();
      task.close();
    } catch(EngineException e) {
      throw new ReportException("Unable to generate BIRT report from " + reportDesign, e);
    }
  }

  private RenderOption getOptions(String format) {
    if(format.equalsIgnoreCase("html")) {
      // Setup rendering to HTML
      HTMLRenderOption options = new HTMLRenderOption();
      options.setOutputFormat("html");
      // Setting this to true removes html and body tags
      options.setEmbeddable(false);
      return options;
    } else if(format.equalsIgnoreCase("pdf")) {
      PDFRenderOption options = new PDFRenderOption();
      options.setOutputFormat(format);
      return options;
    } else {
      throw new IllegalArgumentException("Unexpected report format: " + format);
    }
  }

  @Override
  public boolean isRunning() {
    return engine != null;
  }

  @Override
  public void start() {
    if(isRunning()) return;

    try {
      // make sure BIRT_HOME is set and valid
      File reportEngineHome = new File(System.getProperty(BIRT_HOME_SYSTEM_PROPERTY_NAME), "ReportEngine");

      if(reportEngineHome.exists() == false) {
        log.error("Could not find Birt engine distribution in directory '{}'.", System.getProperty(BIRT_HOME_SYSTEM_PROPERTY_NAME));
        return;
      }

      final EngineConfig config = new EngineConfig();
      config.setEngineHome(reportEngineHome.getAbsolutePath());
      config.setLogConfig(System.getProperty(OPAL_HOME_SYSTEM_PROPERTY_NAME) + File.separator + "logs", Level.ALL);

      Platform.startup(config);
      IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
      engine = factory.createReportEngine(config);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void stop() {
    try {
      engine.destroy();
      Platform.shutdown();
    } catch(Exception ex) {
      ex.printStackTrace();
    } finally {
      engine = null;
    }
  }

}
