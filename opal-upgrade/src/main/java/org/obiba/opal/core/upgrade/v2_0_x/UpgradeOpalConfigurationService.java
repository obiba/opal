/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_0_x;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.obiba.opal.core.cfg.DefaultOpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class UpgradeOpalConfigurationService extends DefaultOpalConfigurationService {

  private File configFile;

  private static final XPath XPATH = XPathFactory.newInstance().newXPath();

  @Override
  public OpalConfiguration getOpalConfiguration() {
    readOpalConfiguration();
    return opalConfiguration;
  }

  @Override
  public void readOpalConfiguration() {
    if(opalConfiguration == null) opalConfiguration = new OpalConfiguration();
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
      opalConfiguration.setSecretKey(getNodeValue("//secretKey", doc));
      opalConfiguration.setDatabasePassword(getNodeValue("//databasePassword", doc));
    } catch(SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  private String getNodeValue(String expression, Document doc) throws XPathExpressionException {
    Node node = (Node) XPATH.compile(expression).evaluate(doc.getDocumentElement(), XPathConstants.NODE);
    return node == null ? null : node.getTextContent();
  }

  @Override
  public void modifyConfiguration(OpalConfigurationService.ConfigModificationTask task) {
    task.doWithConfig(opalConfiguration);
    persistDatabasePassword();
  }

  private void persistDatabasePassword() {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
      Node node = doc.createElement("databasePassword");
      node.setTextContent(opalConfiguration.getDatabasePassword());
      doc.getFirstChild().appendChild(node);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(new DOMSource(doc), new StreamResult(configFile));
    } catch(SAXException | ParserConfigurationException | IOException | TransformerException e) {
      throw new RuntimeException(e);
    }
  }

  public void setConfigFile(File configFile) {
    this.configFile = configFile;
  }
}
