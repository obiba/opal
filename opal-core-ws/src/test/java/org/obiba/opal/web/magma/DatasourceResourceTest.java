/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DatasourceResourceTest extends AbstractMagmaResourceTest {

  private static final Logger log = LoggerFactory.getLogger(DatasourceResourceTest.class);

  @BeforeClass
  public static void before() {
    AbstractMagmaResourceTest.before();
    addAllDatasources();
  }

  @Test
  public void testDatasourcesGET() {
    DatasourcesResource resource = new DatasourcesResource("opal-keys.keys");

    List<Magma.DatasourceDto> dtos = resource.getDatasources();
    Assert.assertEquals(2, dtos.size());
    Assert.assertEquals(DATASOURCE1, dtos.get(0).getName());
    Assert.assertEquals(DATASOURCE2, dtos.get(1).getName());
  }

  @Test
  public void testDatasourceGET() {
    DatasourceResource resource = new DatasourceResource(DATASOURCE1);

    Magma.DatasourceDto dto = resource.get();

    Assert.assertNotNull(dto);
    Assert.assertEquals(DATASOURCE1, dto.getName());
    List<String> tableNames = dto.getTableList();
    Assert.assertEquals(2, tableNames.size());
    Assert.assertEquals("CIPreliminaryQuestionnaire", tableNames.get(0));
    Assert.assertEquals("StandingHeight", tableNames.get(1));
  }

  @Test
  public void testCreateTable_CreatedTableAndItsVariables() throws IOException {
    Datasource datasourceMock = EasyMock.createMock(Datasource.class);
    ValueTableWriter valueTableWriterMock = EasyMock.createMock(ValueTableWriter.class);
    VariableWriter variableWriterMock = EasyMock.createMock(VariableWriter.class);

    datasourceMock.initialise();
    expect(datasourceMock.createWriter("table", "entityType")).andReturn(valueTableWriterMock);
    expect(valueTableWriterMock.writeVariables()).andReturn(variableWriterMock);
    expect(datasourceMock.getName()).andReturn("testDatasource").atLeastOnce();
    datasourceMock.dispose();
    expectLastCall().atLeastOnce();
    variableWriterMock.writeVariable(isA(Variable.class));
    variableWriterMock.close();

    replay(datasourceMock, variableWriterMock, valueTableWriterMock);

    MagmaEngine.get().addDatasource(datasourceMock);

    DatasourceResource datasourceResource = new DatasourceResource(datasourceMock.getName());
    datasourceResource.createTable(createTableDto());

    datasourceMock.dispose();

    verify(datasourceMock, variableWriterMock, valueTableWriterMock);

  }

  private TableDto createTableDto() {
    TableDto.Builder builder = TableDto.newBuilder().setName("table").setEntityType("entityType");
    builder.addVariables(VariableDto.newBuilder().setName("name").setEntityType("entityType").setValueType("text").setIsRepeatable(true));
    return builder.build();
  }
}
