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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.obiba.core.util.FileUtil;
import org.obiba.core.util.StreamUtil;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.schlichtherle.io.FileInputStream;

public abstract class AbstractConfigurationAlteringStep {

  @SuppressWarnings("FieldMayBeFinal")
  @Value("${OPAL_HOME}/conf/opal-config.xml")
  private String opalConfigFile;

  private final Transformer transformer;

  /**
   * Create an install step that does it's XML transformation in {@code doWithConfig} method.
   */
  protected AbstractConfigurationAlteringStep() {
    TransformerFactory tFactory = TransformerFactory.newInstance();
    try {
      transformer = tFactory.newTransformer();
    } catch(TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Create an install step that transforms the opal config using a transformer (probably XSLT).
   *
   * @param transformSource
   */
  @SuppressWarnings("UnusedDeclaration")
  protected AbstractConfigurationAlteringStep(Source transformSource) {
    TransformerFactory tFactory = TransformerFactory.newInstance();
    try {
      transformer = tFactory.newTransformer(transformSource);
    } catch(TransformerConfigurationException e) {
      throw new RuntimeException("Cannot create XML transformer from source", e);
    }
  }

  protected void writeOpalConfig(Document opalConfig) {
    OutputStream outputStream = null;
    try {
      Source source = new DOMSource(opalConfig);

      File tmpFile = File.createTempFile("cfg", ".tmp");
      Result dest = new StreamResult(outputStream = new FileOutputStream(tmpFile));

      transformer.transform(source, dest);

      FileUtil.moveFile(tmpFile, new File(opalConfigFile));
    } catch(IOException e) {
      throw new RuntimeException(e);
    } catch(TransformerException e) {
      throw new RuntimeException(e);
    } finally {
      StreamUtil.silentSafeClose(outputStream);
    }
  }

  protected Document getOpalConfigurationAsDocument() {
    String error = "Could not read Opal configuration file.";
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(opalConfigFile);
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

}
