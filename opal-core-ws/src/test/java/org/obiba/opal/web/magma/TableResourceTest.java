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
import javax.ws.rs.core.UriInfo;

import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.Category;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.type.TextType;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Opal.LocaleDto;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
public class TableResourceTest extends AbstractMagmaResourceTest {

  private static final String PARTICIPANT = "Participant";

//  private static final Logger log = LoggerFactory.getLogger(TableResourceTest.class);

  @BeforeClass
  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
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
    assertThat(dtos).hasSize(2);
    assertThat(dtos.get(0).getName()).isEqualTo("Impedance418");
    assertThat(dtos.get(1).getName()).isEqualTo("Weight");

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

    UriInfo uriInfoMock = mock(UriInfo.class);
    when(uriInfoMock.getPath(false)).thenReturn("/datasource/" + DATASOURCE2 + "/table/Weight");

    checkWeightTableDto((Magma.TableDto) resource.get(null, uriInfoMock, true).getEntity());
  }

  @Test
  public void testTableGETVariables() {
    Datasource datasource = MagmaEngine.get().getDatasource(DATASOURCE2);
    createResource(datasource.getValueTable("Weight"), null);

    List<PathSegment> segments = new ArrayList<>();
    segments.add(mock(PathSegment.class));
    segments.add(mock(PathSegment.class));
    segments.add(mock(PathSegment.class));
    segments.add(mock(PathSegment.class));
    segments.add(mock(PathSegment.class));

    when(segments.get(0).getPath()).thenReturn("datasource");
    when(segments.get(1).getPath()).thenReturn(DATASOURCE2);
    when(segments.get(2).getPath()).thenReturn("table");
    when(segments.get(3).getPath()).thenReturn("Weight");

    UriInfo uriInfoMock = mock(UriInfo.class);
    when(uriInfoMock.getPathSegments()).thenReturn(segments);

    VariablesResource variablesResource = new VariablesResourceImpl();
    variablesResource.setValueTable(datasource.getValueTable("Weight"));
    Response r = variablesResource.getVariables(null, uriInfoMock, null, 0, null);
    @SuppressWarnings("unchecked")
    List<VariableDto> dtos = ImmutableList.copyOf((Iterable<? extends VariableDto>) r.getEntity());

    for(int i = 0; i < 4; i++) {
      verify(segments.get(i)).getPath();
    }

    // alphabetical order
    assertThat(dtos).hasSize(9);
    assertThat("InstrumentRun.Contraindication.code").isEqualTo(dtos.get(0).getName());
    assertThat("InstrumentRun.Contraindication.type").isEqualTo(dtos.get(1).getName());
    assertThat("InstrumentRun.instrumentBarcode").isEqualTo(dtos.get(2).getName());
    assertThat("InstrumentRun.otherContraindication").isEqualTo(dtos.get(3).getName());
    assertThat("InstrumentRun.timeEnd").isEqualTo(dtos.get(4).getName());
    assertThat("InstrumentRun.timeStart").isEqualTo(dtos.get(5).getName());
    assertThat("InstrumentRun.user").isEqualTo(dtos.get(6).getName());
    assertThat("RES_WEIGHT").isEqualTo(dtos.get(7).getName());
    assertThat("RES_WEIGHT.captureMethod").isEqualTo(dtos.get(8).getName());

    assertThat("/datasource/" + DATASOURCE2 + "/table/Weight/variable/InstrumentRun.Contraindication.code")
        .isEqualTo(dtos.get(0).getLink());
    assertThat("Weight").isEqualTo(dtos.get(0).getParentLink().getRel());
    assertThat("/datasource/" + DATASOURCE2 + "/table/Weight").isEqualTo(dtos.get(0).getParentLink().getLink());

    assertThat(3).isEqualTo(dtos.get(8).getCategoriesCount());
    assertThat("MANUAL").isEqualTo(dtos.get(8).getCategories(0).getName());
    assertThat("AUTOMATIC").isEqualTo(dtos.get(8).getCategories(1).getName());
    assertThat("COMPUTED").isEqualTo(dtos.get(8).getCategories(2).getName());

    assertThat(5).isEqualTo(dtos.get(7).getAttributesCount());
    assertThat(1).isEqualTo(dtos.get(8).getAttributesCount());
    assertThat("stage").isEqualTo(dtos.get(8).getAttributes(0).getName());
  }

  private void checkWeightTableDto(Magma.TableDto dto) {
    assertThat(dto).isNotNull();
    assertThat("Weight").isEqualTo(dto.getName());
    assertThat(PARTICIPANT).isEqualTo(dto.getEntityType());
    assertThat(9).isEqualTo(dto.getVariableCount());
    assertThat(0).isEqualTo(dto.getValueSetCount());
    assertThat(DATASOURCE2).isEqualTo(dto.getDatasourceName());
    assertThat("/datasource/" + DATASOURCE2 + "/table/Weight").isEqualTo(dto.getLink());
  }

  @Test
  public void testAddOrUpdateVariables_UpdatingVariables() throws IOException {

    ValueTable valueTable = mock(ValueTable.class);
    Datasource datasource = mock(Datasource.class);
    ValueTableWriter valueTableWriter = mock(ValueTableWriter.class);
    VariableWriter variableWriter = mock(VariableWriter.class);

    when(valueTable.getDatasource()).thenReturn(datasource);
    when(valueTable.getName()).thenReturn("name");
    when(valueTable.getEntityType()).thenReturn("entityType");
    when(valueTable.isView()).thenReturn(false);
    when(datasource.createWriter("name", "entityType")).thenReturn(valueTableWriter);
    when(valueTableWriter.writeVariables()).thenReturn(variableWriter);

    List<VariableDto> variablesDto = Lists.newArrayList();
    for(int i = 0; i < 5; i++) {
      variablesDto.add(VariableDto.newBuilder().setName("name").setEntityType("entityType").setValueType("text")
          .setIsRepeatable(false).build());
    }

    VariablesResource variablesResource = new VariablesResourceImpl();
    variablesResource.setValueTable(valueTable);
    variablesResource.addOrUpdateVariables(variablesDto, null);

    verify(variableWriter, times(5)).writeVariable(any(Variable.class));
  }

  @Test
  public void testGetLocales_WhenNoDisplayLocaleSpecifiedReturnsLocaleDtosWithDisplayFieldUnset() {
    // Setup
    ApplicationContext mockContext = mock(ApplicationContext.class);
    when(mockContext.getBean(LocalesResource.class)).thenReturn(new LocalesResource());
    TableResource tableResource = createResource(null, ImmutableSet.of(new Locale("en"), new Locale("fr")),
        mockContext);

    // Exercise
    Iterable<LocaleDto> localeDtos = tableResource.getLocalesResource().getLocales(null);

    // Verify
    verify(mockContext).getBean(LocalesResource.class);
    assertThat(localeDtos).isNotNull();
    assertThat(tableResource.getLocales().size()).isEqualTo(ImmutableSet.copyOf(localeDtos).size());
    for(LocaleDto localeDto : localeDtos) {
      assertThat(localeDto.hasDisplay()).isFalse();
    }
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleIsEnglishReturnsLocaleDtosWithDisplayFieldInEnglish() {
    // Setup
    ApplicationContext mockContext = mock(ApplicationContext.class);
    when(mockContext.getBean(LocalesResource.class)).thenReturn(new LocalesResource());

    TableResource tableResource = createResource(null, ImmutableSet.of(new Locale("en"), new Locale("fr")),
        mockContext);

    // Exercise
    Iterable<LocaleDto> localeDtos = tableResource.getLocalesResource().getLocales("en");

    // Verify
    verify(mockContext).getBean(LocalesResource.class);

    assertThat(localeDtos).isNotNull();
    assertThat(tableResource.getLocales().size()).isEqualTo(ImmutableSet.copyOf(localeDtos).size());
    for(LocaleDto localeDto : localeDtos) {
      assertThat(localeDto.hasDisplay()).isTrue();
      if("en".equals(localeDto.getName())) {
        assertThat(localeDto.getDisplay()).isEqualTo("English");
      } else if("fr".equals(localeDto.getName())) {
        assertThat(localeDto.getDisplay()).isEqualTo("French");
      }
    }
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleIsFrenchReturnsLocaleDtosWithDisplayFieldInFrench() {
    // Setup
    ApplicationContext mockContext = mock(ApplicationContext.class);
    when(mockContext.getBean(LocalesResource.class)).thenReturn(new LocalesResource());

    TableResource tableResource = createResource(null, ImmutableSet.of(new Locale("en"), new Locale("fr")),
        mockContext);

    // Exercise
    Iterable<LocaleDto> localeDtos = tableResource.getLocalesResource().getLocales("fr");

    // Verify
    verify(mockContext).getBean(LocalesResource.class);
    assertThat(localeDtos).isNotNull();
    assertThat(tableResource.getLocales().size()).isEqualTo(ImmutableSet.copyOf(localeDtos).size());
    for(LocaleDto localeDto : localeDtos) {
      assertThat(localeDto.hasDisplay()).isTrue();
      if("en".equals(localeDto.getName())) {
        assertThat("anglais").isEqualTo(localeDto.getDisplay());
      } else if("fr".equals(localeDto.getName())) {
        assertThat("fran\u00e7ais").isEqualTo(localeDto.getDisplay());
      }
    }
  }

  @Test
  public void testBuildTransientVariable_BuildsVariableAsSpecified() {
    // Setup
    ValueTable valueTable = mock(ValueTable.class);
    when(valueTable.getEntityType()).thenReturn(PARTICIPANT);

    ApplicationContext applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getBean("variableResource", VariableResource.class)).thenReturn(new VariableResourceImpl());

    VariableValueSource variableValueSource = mock(VariableValueSource.class);
    when(variableValueSource.getVariable()).thenReturn(mock(Variable.class));
    when(valueTable.getVariableValueSource(anyString())).thenReturn(variableValueSource);

    TableResource tableResource = createResource(valueTable, applicationContext);
    String script = "$('someVar')";

    // Exercise
    ImmutableList<String> categories = ImmutableList.of("CAT1", "CAT2");
    VariableResource variableResource = tableResource
        .getTransientVariable(null, TextType.get().getName(), false, script, categories, script, categories);

    // Verify behaviour
    verify(valueTable).getEntityType();
    verify(applicationContext).getBean("variableResource", VariableResource.class);

    Variable variable = variableResource.getVariableValueSource().getVariable();

    // Verify state
    assertThat(variable).isNotNull();
    assertThat(variable.getEntityType()).isEqualTo(PARTICIPANT);
    assertThat(TextType.get()).isEqualTo((TextType) variable.getValueType());
    assertThat(variable.isRepeatable()).isFalse();
    assertThat(variable.getAttributeStringValue("script")).isEqualTo(script);
    assertThat(hasCategory(variable, "CAT1")).isTrue();
    assertThat(hasCategory(variable, "CAT2")).isTrue();
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
