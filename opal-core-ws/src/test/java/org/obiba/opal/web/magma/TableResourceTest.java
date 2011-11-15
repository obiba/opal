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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.type.TextType;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Opal.LocaleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 *
 */
public class TableResourceTest extends AbstractMagmaResourceTest {

  private static final Logger log = LoggerFactory.getLogger(TableResourceTest.class);

  @BeforeClass
  public static void before() { 
    AbstractMagmaResourceTest.before();
    addDatasource(DATASOURCE2);
  }

  @Test
  public void testTablesGET() {
    TablesResource resource = new TablesResource(MagmaEngine.get().getDatasource(DATASOURCE2));

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

    replay(uriInfoMock);
    replay(segments.toArray());

    List<VariableDto> dtos = Lists.newArrayList(resource.getVariables().getVariables(uriInfoMock, null, 0, null));

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

    Assert.assertEquals("/datasource/" + DATASOURCE2 + "/table/Weight/variable/InstrumentRun.Contraindication.code", dtos.get(0).getLink());
    Assert.assertEquals("Weight", dtos.get(0).getParentLink().getRel());
    Assert.assertEquals("/datasource/" + DATASOURCE2 + "/table/Weight", dtos.get(0).getParentLink().getLink());

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

  @Test
  public void testAddOrUpdateVariables_UpdatingVariables() throws IOException {

    ValueTable valueTableMock = EasyMock.createMock(ValueTable.class);
    Datasource datasourceMock = EasyMock.createMock(Datasource.class);
    ValueTableWriter valueTableWriterMock = EasyMock.createMock(ValueTableWriter.class);
    VariableWriter variableWriterMock = EasyMock.createMock(VariableWriter.class);

    TableResource resource = new TableResource(valueTableMock);

    expect(valueTableMock.getDatasource()).andReturn(datasourceMock);
    expect(valueTableMock.getName()).andReturn("name");
    expect(valueTableMock.getEntityType()).andReturn("entityType");
    expect(valueTableMock.isView()).andReturn(false);
    expect(datasourceMock.createWriter("name", "entityType")).andReturn(valueTableWriterMock);
    expect(valueTableWriterMock.writeVariables()).andReturn(variableWriterMock);

    variableWriterMock.writeVariable(EasyMock.isA(Variable.class));
    EasyMock.expectLastCall().times(5);
    variableWriterMock.close();

    replay(valueTableMock, datasourceMock, valueTableWriterMock, variableWriterMock);

    List<VariableDto> variablesDto = Lists.newArrayList();
    for(int i = 0; i < 5; i++) {
      variablesDto.add(VariableDto.newBuilder().setName("name").setEntityType("entityType").setValueType("text").setIsRepeatable(false).build());
    }

    resource.getVariables().addOrUpdateVariables(variablesDto);

    verify(valueTableMock, datasourceMock, valueTableWriterMock, variableWriterMock);

  }

  @Test
  public void testAddOrUpdateVariables_InternalServerError() {
    TableResource resource = new TableResource(null);
    Response response = resource.getVariables().addOrUpdateVariables(null);
    Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }

  @Test
  public void testGetLocales_WhenNoDisplayLocaleSpecifiedReturnsLocaleDtosWithDisplayFieldUnset() {
    // Setup
    TableResource sut = new TableResource(null, ImmutableSet.of(new Locale("en"), new Locale("fr")));

    // Exercise
    Iterable<LocaleDto> localeDtos = sut.getLocalesResource().getLocales(null);

    // Verify
    assertNotNull(localeDtos);
    assertEquals(sut.getLocales().size(), ImmutableSet.copyOf(localeDtos).size());
    for(LocaleDto localeDto : localeDtos) {
      assertFalse(localeDto.hasDisplay());
    }
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleIsEnglishReturnsLocaleDtosWithDisplayFieldInEnglish() {
    // Setup
    TableResource sut = new TableResource(null, ImmutableSet.of(new Locale("en"), new Locale("fr")));

    // Exercise
    Iterable<LocaleDto> localeDtos = sut.getLocalesResource().getLocales("en");

    // Verify
    assertNotNull(localeDtos);
    assertEquals(sut.getLocales().size(), ImmutableSet.copyOf(localeDtos).size());
    for(LocaleDto localeDto : localeDtos) {
      assertTrue(localeDto.hasDisplay());
      if(localeDto.getName().equals("en")) {
        assertEquals("English", localeDto.getDisplay());
      } else if(localeDto.getName().equals("fr")) {
        assertEquals("French", localeDto.getDisplay());
      }
    }
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleIsFrenchReturnsLocaleDtosWithDisplayFieldInFrench() {
    // Setup
    TableResource sut = new TableResource(null, ImmutableSet.of(new Locale("en"), new Locale("fr")));

    // Exercise
    Iterable<LocaleDto> localeDtos = sut.getLocalesResource().getLocales("fr");

    // Verify
    assertNotNull(localeDtos);
    assertEquals(sut.getLocales().size(), ImmutableSet.copyOf(localeDtos).size());
    for(LocaleDto localeDto : localeDtos) {
      assertTrue(localeDto.hasDisplay());
      if(localeDto.getName().equals("en")) {
        assertEquals("anglais", localeDto.getDisplay());
      } else if(localeDto.getName().equals("fr")) {
        assertEquals("fran\u00e7ais", localeDto.getDisplay());
      }
    }
  }

  @Test
  public void testBuildTransientVariable_BuildsVariableAsSpecified() {
    // Setup
    ValueTable mockTable = createMock(ValueTable.class);
    expect(mockTable.getEntityType()).andReturn("Participant").atLeastOnce();
    TableResource sut = new TableResource(mockTable);
    String script = "$('someVar')";

    replay(mockTable);

    // Exercise
    VariableResource variableResource = sut.getTransient(TextType.get().getName(), false, script, ImmutableList.<String> of("CAT1", "CAT2"), script, ImmutableList.<String> of("CAT1", "CAT2"));

    // Verify behaviour
    verify(mockTable);

    Variable variable = variableResource.getVariableValueSource().getVariable();

    // Verify state
    assertNotNull(variable);
    assertEquals("Participant", variable.getEntityType());
    assertEquals(TextType.get(), variable.getValueType());
    assertEquals(false, variable.isRepeatable());
    assertEquals(script, variable.getAttributeStringValue("script"));
    assertTrue(hasCategory(variable, "CAT1"));
    assertTrue(hasCategory(variable, "CAT2"));

  }

  private boolean hasCategory(Variable variable, String category) {
    for(Category c : variable.getCategories()) {
      if(c.getName().equals(category)) {
        return true;
      }
    }
    return false;
  }
}
