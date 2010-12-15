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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.reporting.service.birt.common.BirtEngine;
import org.obiba.opal.reporting.service.birt.common.BirtEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class BirtReportServiceImpl implements ReportService {

  private static final Logger log = LoggerFactory.getLogger(BirtReportServiceImpl.class);

  private static final String BIRT_HOME_SYSTEM_PROPERTY_NAME = "BIRT_HOME";

  private BirtEngine engine;

  @Override
  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput) throws ReportException {
    try {
      if(isRunning()) {
        engine.render(format, parameters, reportDesign, reportOutput);
      }
    } catch(BirtEngineException e) {
      throw new ReportException(e.getMessage(), e);
    }
  }

  @Override
  public boolean isRunning() {
    return engine != null && engine.isRunning();
  }

  @Override
  public void start() {
    if(isRunning()) return;
    log.info("Starting BIRT Report Engine.");

    if(System.getProperty(BIRT_HOME_SYSTEM_PROPERTY_NAME) == null) {
      log.error("System property '" + BIRT_HOME_SYSTEM_PROPERTY_NAME + "' is not defined. Cannot start reporting engine.");
      return;
    }

    // make sure BIRT_HOME is set and valid
    File reportEngineHome = new File(System.getProperty(BIRT_HOME_SYSTEM_PROPERTY_NAME), "ReportEngine").getAbsoluteFile();

    if(reportEngineHome.exists() == false) {
      log.error("Could not find Birt engine distribution in directory '{}'.", System.getProperty(BIRT_HOME_SYSTEM_PROPERTY_NAME));
      return;
    }

    this.engine = instantiateEngine(reportEngineHome);
    this.engine.start();
    log.info("Sucessfully started BIRT Report Engine.");
  }

  @Override
  public void stop() {
    try {
      if(engine != null) {
        log.info("Shuting down BIRT Report Engine.");
        engine.stop();
        log.info("Sucessfully shutdown BIRT Report Engine.");
      }
    } catch(Throwable t) {
      log.warn("Error stoping BIRT", t);
    } finally {
      engine = null;
    }
  }

  private BirtEngine instantiateEngine(File birtHome) {
    try {
      File libDir = new File(birtHome, "lib");
      ClassLoader classLoader = createClassLoader(libDir);
      Class<?> c = classLoader.loadClass("org.obiba.opal.reporting.service.birt.bootstrap.EmbeddedBirtEngine");
      return (BirtEngine) c.newInstance();
    } catch(ClassNotFoundException e) {
      log.debug("Cannot find embedded service class", e);
      throw new RuntimeException(e);
    } catch(InstantiationException e) {
      log.debug("Cannot instantiate embedded service class", e);
      throw new RuntimeException(e);
    } catch(IllegalAccessException e) {
      log.debug("Cannot instantiate embedded service class", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a ClassLoader that will load classes from the BIRT dependencies and classes in the {@code
   * org.obiba.opal.reporting.service.birt.common} package before loading a class from the Opal classpath.
   * <p>
   * Whenever BIRT and Opal have a common dependency (Rhino for example), this ClassLoader will "prefer" BIRT's version
   * over Opal's. This allows to "isolate" BIRT and its dependencies from Opal and still allow sharing exceptions.
   * 
   * @param libDir BIRT's /lib directory
   * @return a ClassLoader that will load classes from BIRT dependencies before looking for classes in the Opal
   * classpath.
   */
  private ClassLoader createClassLoader(File libDir) {
    File[] jars = libDir.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File arg0, String name) {
        return name.endsWith("jar");
      }
    });
    URL[] urls = new URL[jars.length];
    for(int i = 0; i < jars.length; i++) {
      try {
        urls[i] = jars[i].toURI().toURL();
      } catch(MalformedURLException e) {
      }
    }
    return createClassLoader(urls);
  }

  private ClassLoader createClassLoader(URL[] urls) {
    // This ClassLoader will load classes from the BIRT classpath before lookin in the Opal classpath except for classes
    // in the "common" package
    return new ChildFirstClassLoader(urls) {
      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Anything in the common package should use the already loaded class so we can cast instances
        return name.startsWith("org.obiba.opal.reporting.service.birt.common") ? getParent().loadClass(name) : super.findClass(name);
      }
    };
  }

}
