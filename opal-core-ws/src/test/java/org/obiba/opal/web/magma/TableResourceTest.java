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
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import junit.framework.Assert;

import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 *
 */
public class TableResourceTest extends AbstractMagmaResourceTest {

  private static final Logger log = LoggerFactory.getLogger(TableResourceTest.class);

  private static final String BASE_URI = "http://localhost:8888/ws";

  @BeforeClass
  public static void before() {
    AbstractMagmaResourceTest.before();
    addDatasource(DATASOURCE2);
  }

  @Test
  public void testTablesGET() {
    TablesResource resource = new TablesResource(DATASOURCE2);

    List<Magma.TableDto> dtos = resource.getTables();
    // alphabetical order
    Assert.assertEquals(2, dtos.size());
    Assert.assertEquals("Impedance418", dtos.get(0).getName());
    Assert.assertEquals("Weight", dtos.get(1).getName());

    checkWeightTableDto(dtos.get(1));
  }

  @Test
  public void testTableGET() {
    Datasource datasource = MagmaEngine.get().getDatasource(DATASOURCE2);
    TableResource resource = new TableResource(datasource.getValueTable("Weight"));

    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getPath()).andReturn("/datasource/" + DATASOURCE2 + "/table/Weight");

    replay(uriInfoMock);
    checkWeightTableDto(resource.get(uriInfoMock));
    verify(uriInfoMock);
  }

  @Test
  public void testTableGETVariables() {
    Datasource datasource = MagmaEngine.get().getDatasource(DATASOURCE2);
    TableResource resource = new TableResource(datasource.getValueTable("Weight"));

    List<PathSegment> segments = new ArrayList<PathSegment>();
    segments.add(createMock(PathSegment.class));
    segments.add(createMock(PathSegment.class));
    segments.add(createMock(PathSegment.class));
    segments.add(createMock(PathSegment.class));
    segments.add(createMock(PathSegment.class));

    expect(segments.get(0).getPath()).andReturn("datasource").atLeastOnce();
    expect(segments.get(1).getPath()).andReturn(DATASOURCE2).atLeastOnce();
    expect(segments.get(2).getPath()).andReturn("table").atLeastOnce();
    expect(segments.get(3).getPath()).andReturn("Weight").atLeastOnce();

    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getPathSegments()).andReturn(segments);
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilderImpl.fromUri(BASE_URI));
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilderImpl.fromUri(BASE_URI));

    replay(uriInfoMock);
    replay(segments.toArray());

    List<VariableDto> dtos = Lists.newArrayList(resource.getVariables(uriInfoMock));

    verify(uriInfoMock);
    verify(segments.toArray());

    // alphabetical order
    Assert.assertEquals(9, dtos.size());
    Assert.assertEquals("InstrumentRun.Contraindication.code", dtos.get(0).getName());
    Assert.assertEquals("InstrumentRun.Contraindication.type", dtos.get(1).getName());
    Assert.assertEquals("InstrumentRun.instrumentBarcode", dtos.get(2).getName());
    Assert.assertEquals("InstrumentRun.otherContraindication", dtos.get(3).getName());
    Assert.assertEquals("InstrumentRun.timeEnd", dtos.get(4).getName());
    Assert.assertEquals("InstrumentRun.timeStart", dtos.get(5).getName());
    Assert.assertEquals("InstrumentRun.user", dtos.get(6).getName());
    Assert.assertEquals("RES_WEIGHT", dtos.get(7).getName());
    Assert.assertEquals("RES_WEIGHT.captureMethod", dtos.get(8).getName());

    Assert.assertEquals(BASE_URI + "/datasource/" + DATASOURCE2 + "/table/Weight/variable/InstrumentRun.Contraindication.code", dtos.get(0).getLink());
    Assert.assertEquals("Weight", dtos.get(0).getParentLink().getRel());
    Assert.assertEquals(BASE_URI + "/datasource/" + DATASOURCE2 + "/table/Weight", dtos.get(0).getParentLink().getLink());

    Assert.assertEquals(3, dtos.get(8).getCategoriesCount());
    Assert.assertEquals("MANUAL", dtos.get(8).getCategories(0).getName());
    Assert.assertEquals("AUTOMATIC", dtos.get(8).getCategories(1).getName());
    Assert.assertEquals("COMPUTED", dtos.get(8).getCategories(2).getName());

    Assert.assertEquals(5, dtos.get(7).getAttributesCount());
    Assert.assertEquals(1, dtos.get(8).getAttributesCount());
    Assert.assertEquals("stage", dtos.get(8).getAttributes(0).getName());
  }

  private void checkWeightTableDto(Magma.TableDto dto) {
    Assert.assertNotNull(dto);
    Assert.assertEquals("Weight", dto.getName());
    Assert.assertEquals("Participant", dto.getEntityType());
    Assert.assertEquals(9, dto.getVariableCount());
    Assert.assertEquals(0, dto.getValueSetCount());
    Assert.assertEquals(DATASOURCE2, dto.getDatasourceName());
    Assert.assertEquals("/datasource/" + DATASOURCE2 + "/table/Weight", dto.getLink());
  }
}
