/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
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

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

  @Override
  @PostConstruct
  public void start() {
    configureMagma();
  }

  @Override
  public OpalConfiguration getOpalConfiguration() {
    OpalConfiguration configuration = new OpalConfiguration();
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);
      XPath xPath = XPathFactory.newInstance().newXPath();
      Node node = (Node) xPath.compile("//secretKey").evaluate(doc.getDocumentElement(), XPathConstants.NODE);
      configuration.setSecretKey(node.getTextContent());
    } catch(SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
      throw new RuntimeException(e);
    }
    return configuration;
  }

  @Override
  public void readOpalConfiguration() {
    throw new IllegalStateException("Should not be called during upgrade");
  }

  @Override
  public void modifyConfiguration(OpalConfigurationService.ConfigModificationTask task) {
    throw new IllegalStateException("Should not be called during upgrade");
  }

  public void setConfigFile(File configFile) {
    this.configFile = configFile;
  }
}
