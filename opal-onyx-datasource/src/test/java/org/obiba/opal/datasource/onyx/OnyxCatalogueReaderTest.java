/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.opal.core.domain.metadata.Catalogue;
import org.obiba.opal.core.domain.metadata.DataItem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class OnyxCatalogueReaderTest {
  //
  // Constants
  //

  private static final String TEST_DIR = "OnyxCatalogueReaderTest";

  //
  // Instance Variables
  //

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  private IOnyxDataInputStrategy dataInputStrategy;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    variablePathNamingStrategy = new DefaultVariablePathNamingStrategy();
    dataInputStrategy = createMock(IOnyxDataInputStrategy.class);
  }

  //
  // Test Methods
  //

  @Test
  public void testRead() throws Exception {
    // Create an OnyxCatalogueReader.
    String catalogueName = "catalogue";
    String variableFile = "variables.xml";
    Resource variableFileResource = new ClassPathResource(TEST_DIR + "/" + variableFile);
    OnyxCatalogueReader reader = createReader(catalogueName, variableFileResource);

    // Record expectations.
    dataInputStrategy.prepare((OnyxDataInputContext) org.easymock.EasyMock.anyObject());
    expect(dataInputStrategy.getEntry(variableFile)).andReturn(variableFileResource.getInputStream());

    replay(dataInputStrategy);

    // Open the OnyxCatalogueReader and read the Catalogue.
    reader.open(null);
    Catalogue catalogue = reader.read();

    // Verify expectations.
    verify(dataInputStrategy);

    // Verify resulting Catalogue.
    assertNotNull(catalogue);
    List<DataItem> dataItems = catalogue.getDataItems();
    assertNotNull(dataItems);
    assertEquals(dataItems.size(), 10150);
  }

  //
  // Helper Methods
  //

  private OnyxCatalogueReader createReader(String catalogueName, Resource variableFileResource) {
    OnyxCatalogueReader reader = new OnyxCatalogueReader();
    reader.setCatalogueName(catalogueName);
    reader.setPathNamingStrategy(variablePathNamingStrategy);
    reader.setDataInputStrategy(dataInputStrategy);
    reader.setResource(variableFileResource);

    return reader;
  }
}