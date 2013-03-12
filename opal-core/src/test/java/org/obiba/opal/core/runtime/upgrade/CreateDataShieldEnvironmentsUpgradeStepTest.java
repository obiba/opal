/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.upgrade;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.util.StreamUtil;
import org.obiba.runtime.Version;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.is;

/**
 *
 */
public class CreateDataShieldEnvironmentsUpgradeStepTest {

  @Test
  public void test_appliesToOpal1dot7dot0() {
    CreateDataShieldEnvironmentsUpgradeStep step = new CreateDataShieldEnvironmentsUpgradeStep();
    Assert.assertThat(step.getAppliesTo(), is(new Version(1, 7, 0)));
  }

  @Test
  public void test_acceptsNoDataShieldConfig() {
    assertTransformResult("no-datashield");
  }

  @Test
  public void test_removesNodeWhenEmpty() {
    assertTransformResult("empty-list");
  }

  @Test
  public void test_transformsListToEnvironment() {
    assertTransformResult("hasMethods");
  }

  private void assertTransformResult(String testName) {
    CreateDataShieldEnvironmentsUpgradeStep step = new CreateDataShieldEnvironmentsUpgradeStep();
    Document orig = loadTestConfig(testName + ".xml");
    step.doWithConfig(orig);
    try {
      Assert.assertThat(printDocument(orig), is(printDocument(loadTestConfig(testName + "-result.xml"))));
    } catch(AssertionError e) {
      System.out.println(printDocument(orig));
      System.out.println(printDocument(loadTestConfig(testName + "-result.xml")));
      throw e;
    }
  }

  /**
   * @param orig
   */
  private String printDocument(Document doc) {
    try {
      StringWriter sw = new StringWriter();
      TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(sw));
      return sw.toString();
    } catch(TransformerException e) {
      throw new RuntimeException(e);
    }
  }

  private Document loadTestConfig(String opalConfigFile) {
    String error = "Could not read Opal configuration file.";
    InputStream inputStream = null;
    try {
      inputStream = getClass().getResourceAsStream("/" + getClass().getSimpleName() + "/" + opalConfigFile);
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setIgnoringElementContentWhitespace(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document d = builder.parse(inputStream);
      d.normalizeDocument();
      return d;
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
