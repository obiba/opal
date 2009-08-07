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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.opal.datasource.DatasourceService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class OnyxDatasetReaderTest {
  //
  // Constants
  //

  private static final String TEST_DIR = "OnyxDatasetReaderTest";

  //
  // Instance Variables
  //

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  private IOnyxDataInputStrategy dataInputStrategy;

  private DatasourceService datasourceService;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    variablePathNamingStrategy = new DefaultVariablePathNamingStrategy();
    dataInputStrategy = createMock(IOnyxDataInputStrategy.class);
    datasourceService = createMock(DatasourceService.class);
  }

  //
  // Test Methods
  //

  @Test
  public void testRead() throws Exception {
    String catalogueName = "catalogue";
    String variableFile = "variables.xml";
    Resource variableFileResource = new ClassPathResource(TEST_DIR + "/" + variableFile);
    String exportFile = "1111111.xml";
    Resource exportFileResource = new ClassPathResource(TEST_DIR + "/" + exportFile);

    OnyxDatasetReader reader = new OnyxDatasetReader();
    reader.setCatalogueName(catalogueName);
    reader.setVariablePathNamingStrategy(variablePathNamingStrategy);
    reader.setDataInputStrategy(dataInputStrategy);
    reader.setResource(exportFileResource);
    reader.setDatasourceService(datasourceService);

    dataInputStrategy.prepare((OnyxDataInputContext) org.easymock.EasyMock.anyObject());
    expect(dataInputStrategy.getEntry(variableFile)).andReturn(variableFileResource.getInputStream());

    List<String> entryList = new ArrayList<String>();
    entryList.add(exportFile);
    expect(dataInputStrategy.listEntries()).andReturn(entryList);
    expect(dataInputStrategy.getEntry(exportFile)).andReturn(exportFileResource.getInputStream());

    expect(datasourceService.loadCatalogue(catalogueName)).andReturn(null);
    expect(datasourceService.fetchEntity(exportFile.replace(".xml", ""))).andReturn(null);

    replay(dataInputStrategy);
    replay(datasourceService);

    reader.open(null);
    reader.read();
  }
}
