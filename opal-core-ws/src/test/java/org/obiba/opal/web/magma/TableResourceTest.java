/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

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
import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
public class TableResourceTest extends AbstractMagmaResourceTest {

  private static final String PARTICIPANT = "Participant";

//  private static final Logger log = LoggerFactory.getLogger(TableResourceTest.class);

  @BeforeClass
  public static void before() {
    AbstractMagmaResourceTest.before();
    addDatasource(DATASOURCE2);
  }

  @Test
  public void testTablesGET() {
    DatasourceTablesResource resource = new DatasourceTablesResourceImpl();
    resource.setDatasource(MagmaEngine.get().getDatasource(DATASOURCE2));

    List<Magma.TableDto> dtos = resource.getTables(true, null);
    // alphabetical order
    assertEquals(2, dtos.size());
    assertEquals("Impedance418", dtos.get(0).getName());
    assertEquals("Weight", dtos.get(1).getName());

    checkWeightTableDto(dtos.get(1));
  }

  private TableResource createResource(ValueTable table, ApplicationContext applicationContext) {
    return createResource(table, Collections.<Locale>emptySet(), applicationContext);
  }

  private TableResource createResource(ValueTable table, Set<Locale> locales, ApplicationContext applicationContext) {
    TableResource tableResource = new TableResourceImpl();
    ((AbstractValueTableResource) tableResource).setApplicationContext(applicationContext);
    tableResource.setLocales(locales);
    tableResource.setValueTable(table);
    return tableResource;
  }

  @Test
  public void testTableGET() {
    Datasource datasource = MagmaEngine.get().getDatasource(DATASOURCE2);
    TableResource resource = createResource(datasource.getValueTable("Weight"), null);

    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getPath(false)).andReturn("/datasource/" + DATASOURCE2 + "/table/Weight");

    replay(uriInfoMock);
    checkWeightTableDto((Magma.TableDto) resource.get(null, uriInfoMock, true).getEntity());
    verify(uriInfoMock);
  }

  @Test
  public void testTableGETVariables() {
    Datasource datasource = MagmaEngine.get().getDatasource(DATASOURCE2);
    createResource(datasource.getValueTable("Weight"), null);

    List<PathSegment> segments = new ArrayList<>();
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

    VariablesResource variablesResource = new VariablesResourceImpl();
    variablesResource.setValueTable(datasource.getValueTable("Weight"));
    Response r = variablesResource.getVariables(null, uriInfoMock, null, 0, null);
    @SuppressWarnings("unchecked")
    List<VariableDto> dtos = ImmutableList.copyOf((Iterable<? extends VariableDto>) r.getEntity());

    verify(uriInfoMock);
    verify(segments.toArray());

    // alphabetical order
    assertEquals(9, dtos.size());
    assertEquals("InstrumentRun.Contraindication.code", dtos.get(0).getName());
    assertEquals("InstrumentRun.Contraindication.type", dtos.get(1).getName());
    assertEquals("InstrumentRun.instrumentBarcode", dtos.get(2).getName());
    assertEquals("InstrumentRun.otherContraindication", dtos.get(3).getName());
    assertEquals("InstrumentRun.timeEnd", dtos.get(4).getName());
    assertEquals("InstrumentRun.timeStart", dtos.get(5).getName());
    assertEquals("InstrumentRun.user", dtos.get(6).getName());
    assertEquals("RES_WEIGHT", dtos.get(7).getName());
    assertEquals("RES_WEIGHT.captureMethod", dtos.get(8).getName());

    assertEquals("/datasource/" + DATASOURCE2 + "/table/Weight/variable/InstrumentRun.Contraindication.code",
        dtos.get(0).getLink());
    assertEquals("Weight", dtos.get(0).getParentLink().getRel());
    assertEquals("/datasource/" + DATASOURCE2 + "/table/Weight", dtos.get(0).getParentLink().getLink());

    assertEquals(3, dtos.get(8).getCategoriesCount());
    assertEquals("MANUAL", dtos.get(8).getCategories(0).getName());
    assertEquals("AUTOMATIC", dtos.get(8).getCategories(1).getName());
    assertEquals("COMPUTED", dtos.get(8).getCategories(2).getName());

    assertEquals(5, dtos.get(7).getAttributesCount());
    assertEquals(1, dtos.get(8).getAttributesCount());
    assertEquals("stage", dtos.get(8).getAttributes(0).getName());
  }

  private void checkWeightTableDto(Magma.TableDto dto) {
    assertNotNull(dto);
    assertEquals("Weight", dto.getName());
    assertEquals(PARTICIPANT, dto.getEntityType());
    assertEquals(9, dto.getVariableCount());
    assertEquals(0, dto.getValueSetCount());
    assertEquals(DATASOURCE2, dto.getDatasourceName());
    assertEquals("/datasource/" + DATASOURCE2 + "/table/Weight", dto.getLink());
  }

  @Test
  public void testAddOrUpdateVariables_UpdatingVariables() throws IOException {

    ValueTable valueTableMock = createMock(ValueTable.class);
    Datasource datasourceMock = createMock(Datasource.class);
    ValueTableWriter valueTableWriterMock = createMock(ValueTableWriter.class);
    VariableWriter variableWriterMock = createMock(VariableWriter.class);

    expect(valueTableMock.getDatasource()).andReturn(datasourceMock);
    expect(valueTableMock.getName()).andReturn("name");
    expect(valueTableMock.getEntityType()).andReturn("entityType");
    expect(valueTableMock.isView()).andReturn(false);
    expect(datasourceMock.createWriter("name", "entityType")).andReturn(valueTableWriterMock);
    expect(valueTableWriterMock.writeVariables()).andReturn(variableWriterMock);

    variableWriterMock.writeVariable(EasyMock.isA(Variable.class));
    EasyMock.expectLastCall().times(5);
    variableWriterMock.close();
    valueTableWriterMock.close();

    replay(valueTableMock, datasourceMock, valueTableWriterMock, variableWriterMock);

    List<VariableDto> variablesDto = Lists.newArrayList();
    for(int i = 0; i < 5; i++) {
      variablesDto.add(VariableDto.newBuilder().setName("name").setEntityType("entityType").setValueType("text")
          .setIsRepeatable(false).build());
    }

    VariablesResource variablesResource = new VariablesResourceImpl();
    variablesResource.setValueTable(valueTableMock);
    variablesResource.addOrUpdateVariables(variablesDto, null);

    verify(valueTableMock, datasourceMock, valueTableWriterMock, variableWriterMock);

  }

  @Test
  public void testAddOrUpdateVariables_InternalServerError() {
    Response response = new VariablesResourceImpl().addOrUpdateVariables(null, null);
    assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }

  @Test
  public void testGetLocales_WhenNoDisplayLocaleSpecifiedReturnsLocaleDtosWithDisplayFieldUnset() {
    // Setup
    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean(LocalesResource.class)).andReturn(new LocalesResource()).atLeastOnce();
    replay(mockContext);
    TableResource tableResource = createResource(null, ImmutableSet.of(new Locale("en"), new Locale("fr")),
        mockContext);

    // Exercise
    Iterable<LocaleDto> localeDtos = tableResource.getLocalesResource().getLocales(null);

    // Verify
    verify(mockContext);
    assertNotNull(localeDtos);
    assertEquals(tableResource.getLocales().size(), ImmutableSet.copyOf(localeDtos).size());
    for(LocaleDto localeDto : localeDtos) {
      assertFalse(localeDto.hasDisplay());
    }
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleIsEnglishReturnsLocaleDtosWithDisplayFieldInEnglish() {
    // Setup
    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean(LocalesResource.class)).andReturn(new LocalesResource()).atLeastOnce();
    replay(mockContext);

    TableResource tableResource = createResource(null, ImmutableSet.of(new Locale("en"), new Locale("fr")),
        mockContext);

    // Exercise
    Iterable<LocaleDto> localeDtos = tableResource.getLocalesResource().getLocales("en");

    // Verify
    verify(mockContext);
    assertNotNull(localeDtos);
    assertEquals(tableResource.getLocales().size(), ImmutableSet.copyOf(localeDtos).size());
    for(LocaleDto localeDto : localeDtos) {
      assertTrue(localeDto.hasDisplay());
      if("en".equals(localeDto.getName())) {
        assertEquals("English", localeDto.getDisplay());
      } else if("fr".equals(localeDto.getName())) {
        assertEquals("French", localeDto.getDisplay());
      }
    }
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleIsFrenchReturnsLocaleDtosWithDisplayFieldInFrench() {
    // Setup
    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean(LocalesResource.class)).andReturn(new LocalesResource()).atLeastOnce();
    replay(mockContext);

    TableResource tableResource = createResource(null, ImmutableSet.of(new Locale("en"), new Locale("fr")),
        mockContext);

    // Exercise
    Iterable<LocaleDto> localeDtos = tableResource.getLocalesResource().getLocales("fr");

    // Verify
    verify(mockContext);
    assertNotNull(localeDtos);
    assertEquals(tableResource.getLocales().size(), ImmutableSet.copyOf(localeDtos).size());
    for(LocaleDto localeDto : localeDtos) {
      assertTrue(localeDto.hasDisplay());
      if("en".equals(localeDto.getName())) {
        assertEquals("anglais", localeDto.getDisplay());
      } else if("fr".equals(localeDto.getName())) {
        assertEquals("fran\u00e7ais", localeDto.getDisplay());
      }
    }
  }

  @Test
  public void testBuildTransientVariable_BuildsVariableAsSpecified() {
    // Setup
    ValueTable mockTable = createMock(ValueTable.class);
    expect(mockTable.getEntityType()).andReturn(PARTICIPANT).atLeastOnce();

    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean(VariableResource.class)).andReturn(new VariableResourceImpl()).atLeastOnce();

    replay(mockTable, mockContext);

    TableResource tableResource = createResource(mockTable, mockContext);
    String script = "$('someVar')";

    // Exercise
    VariableResource variableResource = tableResource
        .getTransientVariable(TextType.get().getName(), false, script, ImmutableList.<String>of("CAT1", "CAT2"), script,
            ImmutableList.<String>of("CAT1", "CAT2"));

    // Verify behaviour
    verify(mockTable, mockContext);

    Variable variable = variableResource.getVariableValueSource().getVariable();

    // Verify state
    assertNotNull(variable);
    assertEquals(PARTICIPANT, variable.getEntityType());
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
