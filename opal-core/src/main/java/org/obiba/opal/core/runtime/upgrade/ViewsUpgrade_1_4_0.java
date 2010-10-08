/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.upgrade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.DatasourceTransformer;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewPersistenceStrategy;
import org.obiba.magma.views.support.ViewAwareDatasourceTransformer;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalViewPersistenceStrategy;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;

import com.google.common.annotations.VisibleForTesting;
import com.thoughtworks.xstream.XStream;

import de.schlichtherle.io.FileInputStream;

/**
 * Copies {@link View}s from within {@link DatasourceFactory} elements of the opal-config.xml file and writes them to
 * separate XML files. Each {@link Datasource} that has {@code View}s will have it's own XML file containing all the
 * {@code View}s associated with that {@code Datasource}.
 */
public class ViewsUpgrade_1_4_0 extends AbstractUpgradeStep {

  private static final Charset CHARSET = Charset.availableCharsets().get("UTF-8");

  private File configFile;

  private OpalConfiguration opalConfiguration;

  private ViewPersistenceStrategy viewPersistenceStrategy = new OpalViewPersistenceStrategy();

  @Override
  public void execute(Version currentVersion) {
    readOpalConfiguration();

    for(DatasourceFactory datasourceFactory : opalConfiguration.getMagmaEngineFactory().factories()) {
      @SuppressWarnings("deprecation")
      DatasourceTransformer datasourceTransformer = datasourceFactory.getDatasourceTransformer();
      if(datasourceTransformer instanceof ViewAwareDatasourceTransformer) {
        ViewAwareDatasourceTransformer viewAwareDatasourceTransformer = (ViewAwareDatasourceTransformer) datasourceTransformer;
        viewPersistenceStrategy.writeViews(datasourceFactory.getName(), viewAwareDatasourceTransformer.getViews());
      }
    }
  }

  @VisibleForTesting
  void readOpalConfiguration() {
    new MagmaEngine().extend(new MagmaJsExtension()).extend(new MagmaXStreamExtension());

    Reader reader = null;
    try {
      reader = new InputStreamReader(new FileInputStream(configFile), CHARSET);
      XStream xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
      opalConfiguration = (OpalConfiguration) xstream.fromXML(reader);
    } catch(FileNotFoundException e) {
      throw new RuntimeException("Could not read Opal configuration file.", e);
    } finally {
      StreamUtil.silentSafeClose(reader);
      MagmaEngine.get().shutdown();
    }
  }

  public void setConfigFile(File configFile) {
    this.configFile = configFile;
  }

  @VisibleForTesting
  OpalConfiguration getOpalConfiguration() {
    return opalConfiguration;
  }
}
