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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.support.ExcelDatasourceFactory;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.magma.views.support.VariableOperationContext;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.search.IndexManagerConfiguration;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.web.magma.support.DatasourceFactoryDtoParser;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.magma.support.ExcelDatasourceFactoryDtoParser;
import org.obiba.opal.web.magma.view.JavaScriptViewDtoExtension;
import org.obiba.opal.web.magma.view.VariableListViewDtoExtension;
import org.obiba.opal.web.magma.view.ViewDtoExtension;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.JavaScriptViewDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.model.Opal.LocaleDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.project.ProjectTransientDatasourcesResource;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 *
 */
@SuppressWarnings({ "ReuseOfLocalVariable", "OverlyCoupledClass", "OverlyLongMethod", "PMD.NcssMethodCount" })
public class DatasourceResourceTest extends AbstractMagmaResourceTest {

//  private static final Logger log = LoggerFactory.getLogger(DatasourceResourceTest.class);

  @BeforeClass
  public static void before() {
    AbstractMagmaResourceTest.before();
    addAllDatasources();
  }

  @Test
  public void testDatasourcesGET() {
    DatasourcesResource resource = new DatasourcesResource();

    List<Magma.DatasourceDto> dtos = resource.getDatasources();
    assertThat(dtos).hasSize(2);
    assertThat(dtos.get(0).getName()).isEqualTo(DATASOURCE1);
    assertThat(dtos.get(1).getName()).isEqualTo(DATASOURCE2);
  }

  private DatasourceResource createDatasource(String name, ApplicationContext mockContext) {
    return createDatasource(name, mockContext, createMock(OpalConfigurationService.class));
  }

  private DatasourceResource createDatasource(String name, ApplicationContext mockContext,
      OpalConfigurationService opalConfigurationService) {
    ViewManager viewManagerMock = createMock(ViewManager.class);
    OpalGeneralConfigService serverServiceMock = createMock(OpalGeneralConfigService.class);
    OpalGeneralConfig configMock = createMock(OpalGeneralConfig.class);
    List<Locale> locales = new ArrayList<>();
    locales.add(Locale.ENGLISH);
    locales.add(Locale.FRENCH);

    expect(configMock.getLocales()).andReturn(locales).atLeastOnce();
    expect(serverServiceMock.getConfig()).andReturn(configMock).atLeastOnce();
    replay(serverServiceMock, configMock);

    IndexManagerConfigurationService indexManagerConfigService = new IndexManagerConfigurationService(
        opalConfigurationService);

    DatasourceResource resource = new DatasourceResource();
    resource.setApplicationContext(mockContext);
    resource.setServerService(serverServiceMock);
    resource.setViewManager(viewManagerMock);
    resource.setIndexManagerConfigService(indexManagerConfigService);
    resource.setViewDtos(newViewDtos());
    resource.setName(name);
    return resource;
  }

  private DatasourceResource createDatasource(String mockDatasourceName, final Datasource mockDatasource,
      OpalConfigurationService mockOpalRuntime, ViewManager mockViewManager, ApplicationContext mockContext) {

    OpalGeneralConfigService serverServiceMock = createMock(OpalGeneralConfigService.class);
    OpalGeneralConfig configMock = createMock(OpalGeneralConfig.class);
    expect(configMock.getLocales()).andReturn(Lists.newArrayList(Locale.ENGLISH, Locale.FRENCH)).atLeastOnce();
    expect(serverServiceMock.getConfig()).andReturn(configMock).atLeastOnce();

    IndexManagerConfigurationService indexManagerConfigService = new IndexManagerConfigurationService(mockOpalRuntime);
    OpalConfiguration opalConfig = new OpalConfiguration();
    opalConfig.addExtension(new IndexManagerConfiguration());

    expect(mockOpalRuntime.getOpalConfiguration()).andReturn(opalConfig).anyTimes();
    mockOpalRuntime.modifyConfiguration(EasyMock.<ConfigModificationTask>anyObject());
    expectLastCall();
    replay(mockOpalRuntime, serverServiceMock, configMock);

    DatasourceResource resource = new DatasourceResource() {
      @Override
      Datasource getDatasource() {
        return mockDatasource;
      }
    };
    resource.setApplicationContext(mockContext);
    resource.setServerService(serverServiceMock);
    resource.setViewManager(mockViewManager);
    resource.setIndexManagerConfigService(indexManagerConfigService);
    resource.setViewDtos(newViewDtos());
    resource.setName(mockDatasourceName);

    return resource;
  }

  @Test
  public void testRemoveDatasource_DatasourceNotFound() {
    OpalConfigurationService opalRuntimeMock = createMock(OpalConfigurationService.class);
    OpalGeneralConfigService serverServiceMock = createMock(OpalGeneralConfigService.class);
    ViewManager viewManagerMock = createMock(ViewManager.class);
    IndexManagerConfigurationService indexManagerConfigService = new IndexManagerConfigurationService(opalRuntimeMock);

    DatasourceResource resource = new DatasourceResource();
    resource.setServerService(serverServiceMock);
    resource.setViewManager(viewManagerMock);
    resource.setIndexManagerConfigService(indexManagerConfigService);
    resource.setViewDtos(newViewDtos());
    resource.setName("datasourceNotExist");
    Response response = resource.removeDatasource();

    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testTransientDatasourcesPOST() {
    Project projectMock = createMock(Project.class);
    ProjectService projectServiceMock = createMock(ProjectService.class);
    ProjectsKeyStoreService projectKeyStoreServiceMock = createMock(ProjectsKeyStoreService.class);
    ProjectTransientDatasourcesResource resource = new ProjectTransientDatasourcesResource();
    resource.setName("patate");
    resource.setProjectService(projectServiceMock);
    resource.setProjectsKeyStoreService(projectKeyStoreServiceMock);
    resource.setDatasourceFactoryRegistry(newDatasourceFactoryRegistry());

    OpalKeyStore keyStore = createMock(OpalKeyStore.class);
    expect(keyStore.listKeyPairs()).andReturn(Collections.<String>emptySet()).atLeastOnce();

    expect(projectServiceMock.getProject("patate")).andReturn(projectMock).atLeastOnce();
    expect(projectKeyStoreServiceMock.getKeyStore(projectMock)).andReturn(keyStore).atLeastOnce();

    Magma.DatasourceFactoryDto factoryDto = Magma.DatasourceFactoryDto.newBuilder()
        .setExtension(ExcelDatasourceFactoryDto.params,
            Magma.ExcelDatasourceFactoryDto.newBuilder().setFile(getDatasourcePath(DATASOURCE1)).setReadOnly(true)
                .build()).build();

    replay(projectMock, projectServiceMock, projectKeyStoreServiceMock, keyStore);
    Response response = resource.createDatasource(factoryDto);
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    Object entity = response.getEntity();
    assertThat(entity).isNotNull();

    Magma.DatasourceDto dto = (Magma.DatasourceDto) entity;
    assertThat(MagmaEngine.get().hasTransientDatasource(dto.getName())).isTrue();
    assertThat(response.getMetadata().get("Location")).isNotNull();
    assertThat(response.getMetadata().get("Location").toString()).isEqualTo("[" + "/datasource/" + dto.getName() + "]");

    verify(projectMock, projectServiceMock, projectKeyStoreServiceMock, keyStore);
  }

  @Test
  public void testTransientDatasourceInstanceGET() {
    ExcelDatasourceFactory factory = new ExcelDatasourceFactory();
    factory.setFile(new File(getDatasourcePath(DATASOURCE1)));
    factory.setReadOnly(true);

    String uid = MagmaEngine.get().addTransientDatasource(factory);

    DatasourceResource resource = createDatasource(uid, null);

    Magma.DatasourceDto dto = resource.get(null);

    assertThat(dto).isNotNull();
    assertThat(dto.getName()).isEqualTo(uid);
  }

  @Test
  public void testTransientDatasourceDELETE() {
    ExcelDatasourceFactory factory = new ExcelDatasourceFactory();
    factory.setFile(new File(getDatasourcePath(DATASOURCE1)));
    factory.setReadOnly(true);

    String uid = MagmaEngine.get().addTransientDatasource(factory);

    DatasourceResource resource = createDatasource(uid, null);

    Response response = resource.removeDatasource();

    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
    assertThat((MagmaEngine.get().hasTransientDatasource(uid))).isFalse();

    response = resource.removeDatasource();
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void testDatasourceGET() {
    DatasourceResource resource = createDatasource(DATASOURCE1, null);

    Magma.DatasourceDto dto = resource.get(null);

    assertThat(dto).isNotNull();
    assertThat(dto.getName()).isEqualTo(DATASOURCE1);
    List<String> tableNames = dto.getTableList();
    assertThat(tableNames).hasSize(2);
    assertThat(tableNames.get(0)).isEqualTo("CIPreliminaryQuestionnaire");
    assertThat(tableNames.get(1)).isEqualTo("StandingHeight");
  }

  @Test
  public void testCreateTable_CreatedTableAndItsVariables() throws IOException {
    Datasource datasourceMock = createMock(Datasource.class);
    ValueTableWriter valueTableWriterMock = createMock(ValueTableWriter.class);
    VariableWriter variableWriterMock = createMock(VariableWriter.class);

    datasourceMock.initialise();
    expect(datasourceMock.createWriter("table", "entityType")).andReturn(valueTableWriterMock);
    expect(valueTableWriterMock.writeVariables()).andReturn(variableWriterMock);
    expect(datasourceMock.getName()).andReturn("testDatasource").atLeastOnce();
    expect(datasourceMock.hasValueTable("table")).andReturn(false);
    datasourceMock.dispose();
    expectLastCall().atLeastOnce();
    variableWriterMock.writeVariable(isA(Variable.class));
    variableWriterMock.close();

    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean(DatasourceTablesResource.class)).andReturn(new DatasourceTablesResourceImpl())
        .atLeastOnce();

    replay(datasourceMock, variableWriterMock, valueTableWriterMock, mockContext);

    MagmaEngine.get().addDatasource(datasourceMock);

    DatasourceResource datasourceResource = createDatasource(datasourceMock.getName(), mockContext);
    Response response = datasourceResource.getTables().createTable(createTableDto());

    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    assertThat(response.getMetadata().getFirst("Location").toString())
        .isEqualTo("/datasource/testDatasource/table/table");

    MagmaEngine.get().removeDatasource(datasourceMock);

    verify(datasourceMock, variableWriterMock, valueTableWriterMock);

  }

  @Test
  public void testCreateTable_TableAlreadyExist() throws IOException {
    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean(DatasourceTablesResource.class)).andReturn(new DatasourceTablesResourceImpl())
        .atLeastOnce();

    Datasource datasourceMock = createMock(Datasource.class);
    datasourceMock.initialise();
    expect(datasourceMock.getName()).andReturn("testDatasource").atLeastOnce();
    expect(datasourceMock.hasValueTable("table")).andReturn(true);
    datasourceMock.dispose();
    expectLastCall().atLeastOnce();

    replay(mockContext, datasourceMock);

    MagmaEngine.get().addDatasource(datasourceMock);

    DatasourceResource datasourceResource = createDatasource(datasourceMock.getName(), mockContext);
    Response response = datasourceResource.getTables().createTable(createTableDto());

    assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    assertThat(((ClientErrorDto) response.getEntity()).getStatus()).isEqualTo("TableAlreadyExists");

    MagmaEngine.get().removeDatasource(datasourceMock);

    verify(datasourceMock);
  }

  @SuppressWarnings("OverlyLongMethod")
  @Test
  public void testCreateView_AddsViewByDelegatingToViewManager() throws URISyntaxException {
    // Setup
    String mockDatasourceName = "mockDatasource";
    String viewName = "viewToAdd";
    String fqViewName = mockDatasourceName + "." + "fromTable";

    ValueTable mockFromTable = createMock(ValueTable.class);
    View mockView = new View(viewName, mockFromTable);

    Datasource mockDatasource = createMock(Datasource.class);
    mockDatasource.initialise();
    expect(mockDatasource.getName()).andReturn(mockDatasourceName).atLeastOnce();
    expect(mockDatasource.hasValueTable(viewName)).andReturn(false).atLeastOnce();
    expect(mockDatasource.getValueTable("fromTable")).andReturn(mockFromTable).atLeastOnce();
    expect(mockDatasource.getValueTable("viewToAdd")).andReturn(mockView).once();
    expect(mockFromTable.getDatasource()).andReturn(mockDatasource).atLeastOnce();
    mockDatasource.dispose();

    JavaScriptViewDto jsViewDto = JavaScriptViewDto.newBuilder().build();
    ViewDto viewDto = ViewDto.newBuilder().setName(viewName).addFrom(fqViewName)
        .setExtension(JavaScriptViewDto.view, jsViewDto).build();

    ViewManager mockViewManager = createMock(ViewManager.class);
    mockViewManager.addView(EasyMock.same(mockDatasourceName), eqView(mockView), eqComment(null), eqOpContext(null));
    expectLastCall().once();

    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getBaseUri()).andReturn(new URI("http://localhost:8080/ws")).atLeastOnce();

    OpalConfigurationService mockOpalRuntime = createMock(OpalConfigurationService.class);
    DatasourceResource sut = createDatasource(mockDatasourceName, mockDatasource, mockOpalRuntime, mockViewManager,
        null);

    replay(mockDatasource, mockFromTable, mockViewManager, uriInfoMock);

    // Exercise
    MagmaEngine.get().addDatasource(mockDatasource);
    Response response = sut.createView(viewDto, uriInfoMock, null);
    MagmaEngine.get().removeDatasource(mockDatasource);

    // Verify state
    assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

    // Verify behaviour
    verify(mockViewManager, uriInfoMock);

  }

  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  @Test
  public void testUpdateView_UpdatesViewIfViewExistsByDelegatingToViewManager() {
    // Setup
    String mockDatasourceName = "mockDatasource";
    String viewName = "viewToUpdate";
    String fqViewName = mockDatasourceName + "." + "fromTable";

    ValueTable mockFromTable = createMock(ValueTable.class);

    Datasource mockDatasource = createMock(Datasource.class);
    mockDatasource.initialise();
    expect(mockDatasource.getName()).andReturn(mockDatasourceName).atLeastOnce();
    expect(mockDatasource.hasValueTable(viewName)).andReturn(true).atLeastOnce();
    expect(mockDatasource.getValueTable("fromTable")).andReturn(mockFromTable).atLeastOnce();
    mockDatasource.dispose();

    expect(mockFromTable.getDatasource()).andReturn(mockDatasource).atLeastOnce();

    JavaScriptViewDto jsViewDto = JavaScriptViewDto.newBuilder().build();
    ViewDto viewDto = ViewDto.newBuilder().setName(viewName).addFrom(fqViewName)
        .setExtension(JavaScriptViewDto.view, jsViewDto).build();

    ViewManager mockViewManager = createMock(ViewManager.class);
    View view = new View(viewName, mockFromTable);
    expect(mockViewManager.getView(mockDatasourceName, viewName)).andReturn(view).atLeastOnce();
    mockViewManager.addView(EasyMock.same(mockDatasourceName), eqView(view), eqComment(null), eqOpContext(null));
    expectLastCall().once();

    OpalConfigurationService mockOpalRuntime = createMock(OpalConfigurationService.class);

    ViewResourceImpl viewResource = new ViewResourceImpl();
    viewResource.setViewManager(mockViewManager);
    viewResource.setViewDtos(newViewDtos());

    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean(ViewResource.class)).andReturn(viewResource).atLeastOnce();

    DatasourceResource datasourceResource = createDatasource(mockDatasourceName, mockDatasource, mockOpalRuntime,
        mockViewManager, mockContext);

    replay(mockDatasource, mockFromTable, mockViewManager, mockContext);

    // Exercise
    MagmaEngine.get().addDatasource(mockDatasource);
    Response response = datasourceResource.getView(viewName).updateView(viewDto, null);
    MagmaEngine.get().removeDatasource(mockDatasource);

    // Verify state
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

    // Verify behaviour
    verify(mockViewManager, mockContext);
  }

  @Test
  public void testRemoveView_RemovesViewByDelegatingToViewManager() {
    // Setup
    String mockDatasourceName = "mockDatasource";
    String viewName = "viewToRemove";

    ValueTable mockFromTable = createMock(ValueTable.class);

    Datasource mockDatasource = createMock(Datasource.class);
    expect(mockDatasource.getName()).andReturn(mockDatasourceName).atLeastOnce();

    expect(mockFromTable.getDatasource()).andReturn(mockDatasource).atLeastOnce();

    ViewManager mockViewManager = createMock(ViewManager.class);
    View view = new View(viewName, mockFromTable);
    expect(mockViewManager.getView(mockDatasourceName, viewName)).andReturn(view).atLeastOnce();
    mockViewManager.removeView(mockDatasourceName, viewName);
    expectLastCall().once();

    ViewResourceImpl viewResource = new ViewResourceImpl();
    viewResource.setViewManager(mockViewManager);
    viewResource.setViewDtos(newViewDtos());

    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean(ViewResource.class)).andReturn(viewResource).atLeastOnce();

    DatasourceResource datasourceResource = createDatasource(mockDatasourceName, mockDatasource,
        createMock(OpalConfigurationService.class), mockViewManager, mockContext);

    replay(mockDatasource, mockFromTable, mockViewManager, mockContext);

    // Exercise
    Response response = datasourceResource.getView(viewName).removeView();

    // Verify behaviour
    verify(mockViewManager, mockContext);

    // Verify state
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
  }

  @Test
  public void testGetView_GetsViewFromViewManager() {
    // Setup
    String mockDatasourceName = "mockDatasource";
    String viewName = "viewToGet";

    Datasource mockDatasource = createMock(Datasource.class);
    expect(mockDatasource.getName()).andReturn(mockDatasourceName).atLeastOnce();

    View view = new View(viewName, createMock(ValueTable.class));
    ViewManager mockViewManager = createMock(ViewManager.class);
    expect(mockViewManager.getView(mockDatasourceName, viewName)).andReturn(view).atLeastOnce();

    ApplicationContext mockContext = createMock(ApplicationContext.class);
    expect(mockContext.getBean(ViewResource.class)).andReturn(new ViewResourceImpl()).atLeastOnce();

    DatasourceResource sut = createDatasource(mockDatasourceName, mockDatasource,
        createMock(OpalConfigurationService.class), mockViewManager, mockContext);

    replay(mockDatasource, mockViewManager, mockContext);

    // Exercise
    ViewResource viewResource = sut.getView(viewName);

    // Verify behaviour
    verify(mockViewManager, mockContext);

    // Verify state
    assertThat(viewName).isEqualTo(((AbstractValueTableResource) viewResource).getValueTable().getName());
  }

  @Test(expected = NoSuchValueTableException.class)
  public void testGetView_ThrowsNoSuchValueTableExceptionWhenViewDoesNotExist() {
    // Setup
    String mockDatasourceName = "mockDatasource";
    String viewName = "viewToGet";

    Datasource mockDatasource = createMock(Datasource.class);
    expect(mockDatasource.getName()).andReturn(mockDatasourceName).atLeastOnce();

    ViewManager mockViewManager = createMock(ViewManager.class);
    expect(mockViewManager.getView(mockDatasourceName, viewName)).andThrow(new NoSuchValueTableException(viewName));

    DatasourceResource sut = createDatasource(mockDatasourceName, mockDatasource,
        createMock(OpalConfigurationService.class), mockViewManager, null);

    replay(mockDatasource, mockViewManager);

    // Exercise
    sut.getView(viewName);
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleSpecifiedReturnsLocaleDtosWithDisplayFieldSet() {
    // Setup
    DatasourceResource sut = createDatasource("theDatasource", null);

    // Exercise
    String displayLocaleName = "en";
    Iterable<LocaleDto> localeDtos = sut.getLocales(displayLocaleName);

    // Verify
    assertThat(localeDtos).isNotNull();
    ImmutableList<LocaleDto> localeDtoList = ImmutableList.copyOf(localeDtos);
    assertThat(localeDtoList).hasSize(2);
    assertThatLocaleDto(localeDtoList, "en", displayLocaleName);
    assertThatLocaleDto(localeDtoList, "fr", displayLocaleName);
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleNotSpecifiedReturnsLocaleDtosWithDisplayFieldNotSet() {
    // Setup
    DatasourceResource sut = createDatasource("theDatasource", null);

    // Exercise
    Iterable<LocaleDto> localeDtos = sut.getLocales(null);

    // Verify
    assertThat(localeDtos).isNotNull();
    ImmutableList<LocaleDto> localeDtoList = ImmutableList.copyOf(localeDtos);
    assertThat(localeDtoList).hasSize(2);
    assertThatLocaleDto(localeDtoList, "en", null);
    assertThatLocaleDto(localeDtoList, "fr", null);
  }

  private DatasourceFactoryRegistry newDatasourceFactoryRegistry() {
    return new DatasourceFactoryRegistry(
        ImmutableSet.<DatasourceFactoryDtoParser>of(new ExcelDatasourceFactoryDtoParser() {
          @Override
          protected File resolveLocalFile(String path) {
            return new File(path);
          }
        }));
  }

  private ViewDtos newViewDtos() {
    ViewDtos viewDtos = new ViewDtos();
    viewDtos.setExtensions(
        ImmutableSet.<ViewDtoExtension>of(new JavaScriptViewDtoExtension(), new VariableListViewDtoExtension()));
    return viewDtos;
  }

  private TableDto createTableDto() {
    TableDto.Builder builder = TableDto.newBuilder().setName("table").setEntityType("entityType");
    builder.addVariables(VariableDto.newBuilder().setName("name").setEntityType("entityType").setValueType("text")
        .setIsRepeatable(true));
    return builder.build();
  }

  private void assertThatLocaleDto(Iterable<LocaleDto> localeDto, String localeName, String displayLocaleName) {
    boolean found = false;
    for(LocaleDto dto : localeDto) {
      if(dto.getName().equals(localeName)) {
        found = true;
        assertThat(dto.hasDisplay()).isEqualTo(displayLocaleName != null);
        if(dto.hasDisplay()) {
          assertThat(new Locale(localeName).getDisplayName(new Locale(displayLocaleName))).isEqualTo(dto.getDisplay());
        }

        break;
      }
    }
    assertThat(found).isTrue();
  }

  //
  // Inner Classes
  //

  static class ViewMatcher implements IArgumentMatcher {

    private final View expected;

    ViewMatcher(View expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      return actual instanceof View && ((ValueTable) actual).getName().equals(expected.getName());
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqView(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with name \"");
      buffer.append(expected.getName());
      buffer.append("\")");
    }

  }

  @Nullable
  private static View eqView(View in) {
    EasyMock.reportMatcher(new ViewMatcher(in));
    return null;
  }

  @Nullable
  private static String eqComment(String in) {
    EasyMock.reportMatcher(new IArgumentMatcher() {
      @Override
      public boolean matches(Object o) {
        return true;
      }

      @Override
      public void appendTo(StringBuffer stringBuffer) {
      }
    });

    return null;
  }

  @Nullable
  private static VariableOperationContext eqOpContext(VariableOperationContext in) {
    EasyMock.reportMatcher(new IArgumentMatcher() {
      @Override
      public boolean matches(Object o) {
        return true;
      }

      @Override
      public void appendTo(StringBuffer stringBuffer) {
      }
    });

    return null;
  }
}
