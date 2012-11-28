/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.obiba.core.util.FileUtil;
import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewPersistenceStrategy;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.XStream;

/**
 * This implementation of the {@link ViewPersistenceStrategy} serializes and de-serializes {@link View}s to XML files.
 * Each XML file is named after a {@link Datasource} and contains all the Views that are associated with that
 * Datasource.
 */
public class OpalViewPersistenceStrategy implements ViewPersistenceStrategy {

  private static final Logger log = LoggerFactory.getLogger(OpalViewPersistenceStrategy.class);

  public final static String OPAL_HOME_SYSTEM_PROPERTY_NAME = "OPAL_HOME";

  public static final String CONF_DIRECTORY_NAME = "conf";

  public static final String VIEWS_DIRECTORY_NAME = "views";

  private final File viewsDirectory;

  private final ReadWriteLock rwl = new ReentrantReadWriteLock(true);

  private final Lock r = rwl.readLock();

  private final Lock w = rwl.writeLock();

  public OpalViewPersistenceStrategy() {
    String viewsDirectoryName = System.getProperty(OPAL_HOME_SYSTEM_PROPERTY_NAME) + File.separator //
        + CONF_DIRECTORY_NAME + File.separator + VIEWS_DIRECTORY_NAME;
    viewsDirectory = new File(viewsDirectoryName);
  }

  @Override
  public void writeViews(String datasourceName, Set<View> views) {
    w.lock();
    try {
      doWriteViews(datasourceName, views);
    } finally {
      w.unlock();
    }
  }

  @Override
  public Set<View> readViews(String datasourceName) {
    r.lock();
    try {
      return doReadViews(datasourceName);
    } finally {
      r.unlock();
    }
  }

  private void doWriteViews(String datasourceName, Collection<View> views) {
    createViewsDirectory(); // Creates the views directory if it doesn't exist.
    if(views.isEmpty()) {
      if(getDatasourceViewsFile(datasourceName).exists()) {
        if(getDatasourceViewsFile(datasourceName).delete() == false) {
          // Ignore, but this may be a problem.
        }
      }
      // Do nothing. The file containing the views has already been deleted.
    } else {
      XStream xstream = getXStream();
      OutputStreamWriter writer = null;
      try {
        // OPAL-1285 tmp file prefix must be at least 3 chars
        File tmpFile = File.createTempFile("opal-" + datasourceName, ".xml");
        writer = new OutputStreamWriter(new FileOutputStream(tmpFile), Charsets.UTF_8);
        xstream.toXML(views, writer);

        FileUtil.copyFile(tmpFile, getDatasourceViewsFile(datasourceName));
        if(!tmpFile.delete()) {
          // ignore;
        }

      } catch(FileNotFoundException e) {
        throw new RuntimeException(
            "Could not find the views file '" + getDatasourceViewsFile(datasourceName).getAbsolutePath() + "'. " + e);
      } catch(IOException e) {
        throw new RuntimeException(
            "Failed to create the views file '" + getDatasourceViewsFile(datasourceName).getAbsolutePath() + "'. " + e);
      } finally {
        StreamUtil.silentSafeClose(writer);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Set<View> doReadViews(String datasourceName) {
    Set<View> result = ImmutableSet.of();
    if(!viewsDirectory.isDirectory()) {
      log.info("The views directory '" + viewsDirectory.getAbsolutePath() + "' does not exist.");
      return result;
    }
    XStream xstream = getXStream();
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(new FileInputStream(getDatasourceViewsFile(datasourceName)), Charsets.UTF_8);
      result = (Set<View>) xstream.fromXML(reader);
    } catch(FileNotFoundException e) {
      return ImmutableSet.of();
    } finally {
      StreamUtil.silentSafeClose(reader);
    }
    return result;
  }

  protected XStream getXStream() {
    return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
  }

  private void createViewsDirectory() {
    if(!viewsDirectory.isDirectory()) {
      if(!viewsDirectory.mkdirs()) {
        throw new RuntimeException(
            "The views directory '" + viewsDirectory.getAbsolutePath() + "' could not be created.");
      }
    }
  }

  private String normalizeDatasourceName(@SuppressWarnings("TypeMayBeWeakened") String datasourceName) {
    Pattern escaper = Pattern.compile("([^a-zA-Z0-9-_. ])");
    return escaper.matcher(datasourceName).replaceAll("");
  }

  private File getDatasourceViewsFile(String datasourceName) {
    return new File(viewsDirectory, normalizeDatasourceName(datasourceName) + ".xml");
  }

}
