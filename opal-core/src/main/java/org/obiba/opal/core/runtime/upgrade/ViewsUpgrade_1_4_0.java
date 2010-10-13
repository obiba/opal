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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.annotations.VisibleForTesting;
import com.thoughtworks.xstream.XStream;

import de.schlichtherle.io.FileInputStream;

/**
 * Copies {@link View}s from within {@link DatasourceFactory} elements of the opal-config.xml file and writes them to
 * separate XML files. Each {@link Datasource} that has {@code View}s will have it's own XML file containing all the
 * {@code View}s associated with that {@code Datasource}.
 */
public class ViewsUpgrade_1_4_0 extends AbstractUpgradeStep {

  private static final String VIEWS_ELEMENT_NAME = "views";

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
        deleteViewsFromOpalConfigurationFile();
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

  private void deleteViewsFromOpalConfigurationFile() {
    String error = "Could not delete views from Opal configuration file.";
    OutputStream outputStream = null;
    try {
      Document doc = getOpalConfigurationAsDocument();
      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer tFormer = tFactory.newTransformer();
      NodeList viewsElements = doc.getElementsByTagName(VIEWS_ELEMENT_NAME);
      deleteNodes(viewsElements);
      Source source = new DOMSource(doc);
      Result dest = new StreamResult(outputStream = new FileOutputStream(configFile));
      tFormer.transform(source, dest);
    } catch(IOException e) {
      throw new RuntimeException(error, e);
    } catch(TransformerException e) {
      throw new RuntimeException(error, e);
    } finally {
      StreamUtil.silentSafeClose(outputStream);
    }
  }

  private Document getOpalConfigurationAsDocument() {
    String error = "Could not read Opal configuration file.";
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(configFile);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(inputStream);
    } catch(ParserConfigurationException e) {
      throw new RuntimeException(error, e);
    } catch(SAXException e) {
      throw new RuntimeException(error, e);
    } catch(IOException e) {
      throw new RuntimeException(error, e);
    } finally {
      StreamUtil.silentSafeClose(inputStream);
    }
  }

  private void deleteNodes(NodeList nodes) {
    for(int i = nodes.getLength() - 1; i >= 0; i--) {
      Node node = nodes.item(i);
      node.getParentNode().removeChild(node);
    }
  }

}
