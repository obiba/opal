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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.datasource.excel.support.ExcelDatasourceFactory;
import org.obiba.opal.web.magma.support.DatasourceFactoryDtoParser;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.magma.support.ExcelDatasourceFactoryDtoParser;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Ws;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
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
    DatasourcesResource resource = new DatasourcesResource("opal-keys.keys", newDatasourceFactoryRegistry());

    List<Magma.DatasourceDto> dtos = resource.getDatasources();
    Assert.assertEquals(2, dtos.size());
    Assert.assertEquals(DATASOURCE1, dtos.get(0).getName());
    Assert.assertEquals(DATASOURCE2, dtos.get(1).getName());
  }

  @Test
  public void testDatasourcesPOST() {
    DatasourcesResource resource = new DatasourcesResource("opal-keys.keys", newDatasourceFactoryRegistry());

    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilderImpl.fromUri(BASE_URI));

    Magma.DatasourceFactoryDto factoryDto = Magma.DatasourceFactoryDto.newBuilder().setExtension(ExcelDatasourceFactoryDto.params, Magma.ExcelDatasourceFactoryDto.newBuilder().setFile(getDatasourcePath(DATASOURCE1)).setReadOnly(true).build()).build();

    replay(uriInfoMock);
    Response response = resource.createDatasource(uriInfoMock, factoryDto);
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    Object entity = response.getEntity();
    Assert.assertNotNull(entity);
    try {
      Magma.DatasourceDto dto = (Magma.DatasourceDto) entity;
      Assert.assertTrue(MagmaEngine.get().hasTransientDatasource(dto.getName()));
      Assert.assertNotNull(response.getMetadata().get("Location"));
      Assert.assertEquals("[" + BASE_URI + "/datasource/" + dto.getName() + "]", response.getMetadata().get("Location").toString());
    } catch(Exception e) {
      Assert.assertFalse(true);
    }

    verify(uriInfoMock);
  }

  @Test
  public void testDatasourcesPOSTUserDefinedBogus() {
    DatasourcesResource resource = new DatasourcesResource("opal-keys.keys", newDatasourceFactoryRegistry());

    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilderImpl.fromUri(BASE_URI));

    File file = new File(DATASOURCES_FOLDER, "user-defined-bogus.xls");
    Magma.DatasourceFactoryDto factoryDto = Magma.DatasourceFactoryDto.newBuilder().setExtension(ExcelDatasourceFactoryDto.params, Magma.ExcelDatasourceFactoryDto.newBuilder().setFile(file.getAbsolutePath()).setReadOnly(true).build()).build();

    replay(uriInfoMock);
    Response response = resource.createDatasource(uriInfoMock, factoryDto);
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    ClientErrorDto error = (ClientErrorDto) response.getEntity();
    // System.out.println(JsonFormat.printToString(error));
    Assert.assertEquals("DatasourceCreationFailed", error.getStatus());
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), error.getCode());
    Assert.assertEquals(15, error.getExtensionCount(Ws.DatasourceParsingErrorDto.errors));
    Ws.DatasourceParsingErrorDto parsingError = error.getExtension(Ws.DatasourceParsingErrorDto.errors, 0);
    Assert.assertEquals("VariableNameRequired", parsingError.getKey());
    Assert.assertEquals("[Variables, 10, Table2]", parsingError.getArgumentsList().toString());
  }

  @Test
  public void testTransientDatasourceInstanceGET() {
    ExcelDatasourceFactory factory = new ExcelDatasourceFactory();
    factory.setFile(new File(getDatasourcePath(DATASOURCE1)));
    factory.setReadOnly(true);

    String uid = MagmaEngine.get().addTransientDatasource(factory);

    DatasourceResource resource = new DatasourceResource(uid);

    Magma.DatasourceDto dto = resource.get();

    Assert.assertNotNull(dto);
    Assert.assertEquals(uid, dto.getName());
  }

  @Test
  public void testTransientDatasourceDELETE() {
    ExcelDatasourceFactory factory = new ExcelDatasourceFactory();
    factory.setFile(new File(getDatasourcePath(DATASOURCE1)));
    factory.setReadOnly(true);

    String uid = MagmaEngine.get().addTransientDatasource(factory);

    DatasourceResource resource = new DatasourceResource(uid);

    Response response = resource.removeDatasource();

    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
    Assert.assertFalse(MagmaEngine.get().hasTransientDatasource(uid));

    response = resource.removeDatasource();
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
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
    expect(datasourceMock.hasValueTable("table")).andReturn(false);
    datasourceMock.dispose();
    expectLastCall().atLeastOnce();
    variableWriterMock.writeVariable(isA(Variable.class));
    variableWriterMock.close();

    replay(datasourceMock, variableWriterMock, valueTableWriterMock);

    MagmaEngine.get().addDatasource(datasourceMock);

    DatasourceResource datasourceResource = new DatasourceResource(datasourceMock.getName());
    Response response = datasourceResource.createTable(createTableDto());

    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
    Assert.assertEquals("/datasource/testDatasource/table/table", response.getMetadata().getFirst("Location").toString());

    MagmaEngine.get().removeDatasource(datasourceMock);

    verify(datasourceMock, variableWriterMock, valueTableWriterMock);

  }

  @Test
  public void testCreateTable_TableAlreadyExist() throws IOException {
    Datasource datasourceMock = EasyMock.createMock(Datasource.class);

    datasourceMock.initialise();
    expect(datasourceMock.getName()).andReturn("testDatasource").atLeastOnce();
    expect(datasourceMock.hasValueTable("table")).andReturn(true);
    datasourceMock.dispose();
    expectLastCall().atLeastOnce();

    replay(datasourceMock);

    MagmaEngine.get().addDatasource(datasourceMock);

    DatasourceResource datasourceResource = new DatasourceResource(datasourceMock.getName());
    Response response = datasourceResource.createTable(createTableDto());

    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    Assert.assertEquals("TableAlreadyExists", ((ClientErrorDto) response.getEntity()).getStatus());

    MagmaEngine.get().removeDatasource(datasourceMock);

    verify(datasourceMock);
  }

  @Test
  public void testCreateTable_InternalServerError() {
    DatasourceResource datasourceResource = new DatasourceResource("");
    Response response = datasourceResource.createTable(null);

    Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }

  private DatasourceFactoryRegistry newDatasourceFactoryRegistry() {
    return new DatasourceFactoryRegistry() {
      @Override
      public DatasourceFactory parse(DatasourceFactoryDto dto) {
        DatasourceFactoryDtoParser parser = new ExcelDatasourceFactoryDtoParser() {
          @Override
          protected File resolveLocalFile(String path) {
            return new File(path);
          }
        };
        return parser.parse(dto);
      }
    };
  }

  private TableDto createTableDto() {
    TableDto.Builder builder = TableDto.newBuilder().setName("table").setEntityType("entityType");
    builder.addVariables(VariableDto.newBuilder().setName("name").setEntityType("entityType").setValueType("text").setIsRepeatable(true));
    return builder.build();
  }
}
