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
import java.nio.charset.Charset;
import java.util.Set;

import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewPersistenceStrategy;
import org.obiba.magma.xstream.DefaultXStreamFactory;

import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.XStream;

/**
 * This implementation of the {@link ViewPersistenceStrategy} serializes and de-serializes {@link View}s to XML files.
 * Each XML file is named after a {@link Datasource} and contains all the Views that are associated with that
 * Datasource.
 */
public class OpalViewPersistenceStrategy implements ViewPersistenceStrategy {

  final static String OPAL_HOME_SYSTEM_PROPERTY_NAME = "OPAL_HOME";

  private static final String CONF_DIRECTORY_NAME = "conf";

  private static final String VIEWS_DIRECTORY_NAME = "views";

  private static final Charset UTF8 = Charset.forName("UTF-8");

  private final String viewsDirectoryName;

  private final File viewsDirectory;

  public OpalViewPersistenceStrategy() {
    viewsDirectoryName = System.getProperty(OPAL_HOME_SYSTEM_PROPERTY_NAME) + File.separator + CONF_DIRECTORY_NAME + File.separator + VIEWS_DIRECTORY_NAME;
    viewsDirectory = new File(viewsDirectoryName);
  }

  @Override
  public void writeViews(String datasourceName, Set<View> views) {
    createViewsDirectory(); // Creates the views directory if it doesn't exist.
    deleteViews(datasourceName); // Delete the old views before writing new ones.
    if(views.isEmpty()) {
      // Do nothing. The file containing the views has already been deleted.
    } else {
      XStream xstream = new DefaultXStreamFactory().createXStream();
      OutputStreamWriter writer = null;
      try {
        if(!getDatasourceFile(datasourceName).createNewFile()) throw new RuntimeException("Failed to create the views file '" + getDatasourceFile(datasourceName).getAbsolutePath() + "'.");
        writer = new OutputStreamWriter(new FileOutputStream(getDatasourceFile(datasourceName)), UTF8);
        xstream.toXML(views, writer);
      } catch(FileNotFoundException e) {
        throw new RuntimeException("Could not find the views file '" + getDatasourceFile(datasourceName).getAbsolutePath() + "'. " + e);
      } catch(IOException e) {
        throw new RuntimeException("Failed to create the views file '" + getDatasourceFile(datasourceName).getAbsolutePath() + "'. " + e);
      } finally {
        StreamUtil.silentSafeClose(writer);
      }
    }
  }

  private void createViewsDirectory() {
    if(!viewsDirectory.isDirectory()) {
      if(!viewsDirectory.mkdirs()) throw new RuntimeException("The views directory '" + viewsDirectory.getAbsolutePath() + "' could not be created.");
    }
  }

  private void deleteViews(String datasourceName) {
    if(getDatasourceFile(datasourceName).exists()) {
      if(!getDatasourceFile(datasourceName).delete()) throw new RuntimeException("Failed to delete the views file '" + getDatasourceFile(datasourceName).getAbsolutePath() + "'.");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<View> readViews(String datasourceName) {
    if(!viewsDirectory.isDirectory()) throw new RuntimeException("The views directory '" + viewsDirectory.getAbsolutePath() + "' does not exist.");
    Set<View> result = ImmutableSet.of();
    XStream xstream = new DefaultXStreamFactory().createXStream();
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(new FileInputStream(getDatasourceFile(datasourceName)), UTF8);
      result = (Set<View>) xstream.fromXML(reader);
    } catch(FileNotFoundException e) {
      return ImmutableSet.of();
    } finally {
      StreamUtil.silentSafeClose(reader);
    }
    return result;
  }

  private String normalizeDatasourceName(String datasourceName) {
    return datasourceName;
  }

  private File getDatasourceFile(String datasourceName) {
    return new File(viewsDirectory, normalizeDatasourceName(datasourceName) + ".xml");
  }

}
