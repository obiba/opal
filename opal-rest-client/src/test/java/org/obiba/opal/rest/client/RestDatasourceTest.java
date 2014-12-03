/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.rest.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BinaryType;
import org.obiba.opal.rest.client.magma.OpalJavaClient;
import org.obiba.opal.rest.client.magma.RestDatasource;
import org.obiba.opal.rest.client.magma.UriBuilder;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.client.magma.TimestampsDto;

import com.google.protobuf.Message;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class RestDatasourceTest {

//  private static final Logger log = LoggerFactory.getLogger(RestDatasourceTest.class);
  OpalJavaClient mockOpalClient;
  Magma.DatasourceDto datasourceDto;
  Magma.TableDto tableDto;

  @BeforeClass
  public static void before() {
    new MagmaEngine();
  }

  @AfterClass
  public static void after() {
    MagmaEngine.get().shutdown();
  }

  @Before
  public void setup() throws URISyntaxException, IOException {
    mockOpalClient = mock(OpalJavaClient.class);
    datasourceDto = Magma.DatasourceDto.newBuilder()
        .setName("test")
        .setType("participant")
        .addAllTable(Arrays.asList(new String[] {"tab1"}))
        .setTimestamps(
            Magma.TimestampsDto.newBuilder().setLastUpdate("2014-01-01T00:00:00Z").setCreated("2014-01-01T00:00:00Z").build())
        .build();
    tableDto = Magma.TableDto.newBuilder()
        .setName("tab1")
        .setEntityType("table")
        .setDatasourceName("test")
        .build();

    when(mockOpalClient.newUri()).thenReturn(
        new UriBuilder(new URI("https://localhost/")));

    when(mockOpalClient.newUri(any(URI.class))).thenReturn(
        new UriBuilder(new URI("https://localhost/")));
  }

  @Test
  @Ignore
  public void testValueSetWriter() throws URISyntaxException, IOException {
    Datasource ds = new RestDatasource("rest", "http://127.0.0.1:8080/ws", "test", "administrator", "password");

    // ensure test table exists
    try(ValueTableWriter tableWriter = ds.createWriter("RestDatasourceTest", "Participant");
        VariableWriter variableWriter = tableWriter.writeVariables();
        ValueSetWriter vsw = tableWriter.writeValueSet(new VariableEntityBean("Participant", "1234"))) {
      Variable var = Variable.Builder.newVariable("VarBin", BinaryType.get(), "Participant").build();
      variableWriter.writeVariable(var);
      vsw.writeValue(var, BinaryType.get().valueOf(new byte[102400000]));
    }
  }

  @Test
  public void testRefresh() throws URISyntaxException, IOException {
    when(mockOpalClient
        .getResource(any(Class.class), any(URI.class), any(Magma.DatasourceDto.Builder.class)))
        .thenReturn(datasourceDto, tableDto);
    Datasource ds = new RestDatasource("rest", mockOpalClient, "test");

    Set<ValueTable> valueTables = ds.getValueTables();

    assertEquals(1, valueTables.size());
    verify(mockOpalClient, times(2)).getResource(any(Class.class), any(URI.class), any(Magma.DatasourceDto.Builder.class));
  }

  @Test
  public void testRefreshNotNeeded() throws URISyntaxException, IOException {
    when(mockOpalClient
        .getResource(any(Class.class), any(URI.class), any(Magma.DatasourceDto.Builder.class)))
        .thenReturn(datasourceDto, tableDto, datasourceDto);
    Datasource ds = new RestDatasource("rest", mockOpalClient, "test");
    ds.getValueTables(); //populate cached tables

    Set<ValueTable> valueTables2 = ds.getValueTables();

    assertEquals(1, valueTables2.size());
    verify(mockOpalClient, times(3)).getResource(any(Class.class), any(URI.class), any(Magma.DatasourceDto.Builder.class));
  }

  @Test
  public void testRefreshRemoveTable() throws URISyntaxException, IOException {
    Magma.DatasourceDto datasourceDtoEmpty = Magma.DatasourceDto.newBuilder()
        .setName("test")
        .setType("participant")
        .addAllTable(Arrays.asList(new String[] { }))
        .setTimestamps(
            Magma.TimestampsDto.newBuilder().setLastUpdate("2014-01-01T00:00:01Z").setCreated("2014-01-01T00:00:00Z")
                .build())
        .build();

    when(mockOpalClient
        .getResource(any(Class.class), any(URI.class), any(Magma.DatasourceDto.Builder.class)))
        .thenReturn(datasourceDto, tableDto, datasourceDtoEmpty);
    Datasource ds = new RestDatasource("rest", mockOpalClient, "test");
    ds.getValueTables(); //populate cached tables

    Set<ValueTable> valueTables2 = ds.getValueTables();

    assertEquals(0, valueTables2.size());
    verify(mockOpalClient, times(3)).getResource(any(Class.class), any(URI.class), any(Magma.DatasourceDto.Builder.class));
  }
}
