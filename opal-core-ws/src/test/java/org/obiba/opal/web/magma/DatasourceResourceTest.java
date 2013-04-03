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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableUpdateListener;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.support.ExcelDatasourceFactory;
import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchConfigurationService;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.magma.support.DatasourceFactoryDtoParser;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.magma.support.ExcelDatasourceFactoryDtoParser;
import org.obiba.opal.web.magma.support.SpssDatasourceFactoryDtoParser;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import junit.framework.Assert;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.obiba.core.util.FileUtil.getFileFromResource;

/**
 *
 */
public class DatasourceResourceTest extends AbstractMagmaResourceTest {

//  private static final Logger log = LoggerFactory.getLogger(DatasourceResourceTest.class);

  @BeforeClass
  public static void before() {
    AbstractMagmaResourceTest.before();
    addAllDatasources();
  }

  @Test
  public void testDatasourcesGET() {
    OpalConfigurationService opalRuntimeMock = createMock(OpalConfigurationService.class);
    DatasourcesResource resource = new DatasourcesResource(newDatasourceFactoryRegistry(), opalRuntimeMock);

    List<Magma.DatasourceDto> dtos = resource.getDatasources();
    Assert.assertEquals(2, dtos.size());
    Assert.assertEquals(DATASOURCE1, dtos.get(0).getName());
    Assert.assertEquals(DATASOURCE2, dtos.get(1).getName());
  }

  @Test
  public void testCreateDatasource_DatasourceCreatedSuccessfully() {
    Response response = createNewDatasource("newDatasourceCreated");
    Assert.assertTrue(MagmaEngine.get().hasDatasource("newDatasourceCreated"));
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
    MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource("newDatasourceCreated"));
  }

  @Test
  public void testCreateDatasource_SpssDatasourceCreatedSuccessfully() {
    Response response = createNewSpssDatasource("newSpssDatasourceCreated");
    Assert.assertTrue(MagmaEngine.get().hasDatasource("newSpssDatasourceCreated"));
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
    MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource("newSpssDatasourceCreated"));
  }

  private Response createNewDatasource(String name) {
    OpalConfigurationService opalRuntimeMock = createMock(OpalConfigurationService.class);
    UriInfo uriInfoMock = createMock(UriInfo.class);

    opalRuntimeMock.modifyConfiguration((ConfigModificationTask) EasyMock.anyObject());
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilderImpl.fromPath("/"));

    replay(uriInfoMock, opalRuntimeMock);

    DatasourcesResource resource = new DatasourcesResource(newDatasourceFactoryRegistry(), opalRuntimeMock);
    Magma.DatasourceFactoryDto factoryDto = Magma.DatasourceFactoryDto.newBuilder().setName(name)
        .setExtension(ExcelDatasourceFactoryDto.params,
            Magma.ExcelDatasourceFactoryDto.newBuilder().setFile(getDatasourcePath(DATASOURCE1)).setReadOnly(true)
                .build()).build();

    Response response = resource.createDatasource(uriInfoMock, factoryDto);

    verify(uriInfoMock, opalRuntimeMock);
    return response;
  }

  private Response createNewSpssDatasource(String name) {
    OpalConfigurationService opalRuntimeMock = createMock(OpalConfigurationService.class);
    UriInfo uriInfoMock = createMock(UriInfo.class);

    opalRuntimeMock.modifyConfiguration((ConfigModificationTask) EasyMock.anyObject());
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilderImpl.fromPath("/"));

    replay(uriInfoMock, opalRuntimeMock);

    DatasourcesResource resource = new DatasourcesResource(newSpssDatasourceFactoryRegistry(), opalRuntimeMock);
    Magma.DatasourceFactoryDto factoryDto = Magma.DatasourceFactoryDto.newBuilder().setName(name)
        .setExtension(Magma.SpssDatasourceFactoryDto.params, Magma.SpssDatasourceFactoryDto.newBuilder()
            .setFile(FileUtil.getFileFromResource("spss/DatabaseTest.sav").getAbsolutePath()).build()).build();

    Response response = resource.createDatasource(uriInfoMock, factoryDto);

    verify(uriInfoMock, opalRuntimeMock);
    return response;
  }

  @Test
  public void testCreateDatasource_DuplicateDatasourceName() {
    createNewDatasource("newDatasourceDuplicate");

    OpalConfigurationService opalRuntimeMock = createMock(OpalConfigurationService.class);
    UriInfo uriInfoMock = createMock(UriInfo.class);

    DatasourcesResource resource = new DatasourcesResource(newDatasourceFactoryRegistry(), opalRuntimeMock);
    Magma.DatasourceFactoryDto factoryDto = Magma.DatasourceFactoryDto.newBuilder().setName("newDatasourceDuplicate")
        .setExtension(ExcelDatasourceFactoryDto.params,
            Magma.ExcelDatasourceFactoryDto.newBuilder().setFile(getDatasourcePath(DATASOURCE1)).setReadOnly(true)
                .build()).build();
    Response response = resource.createDatasource(uriInfoMock, factoryDto);

    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    Assert.assertEquals(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "DuplicateDatasourceName").build(),
        response.getEntity());
    MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource("newDatasourceDuplicate"));
  }

  @Test
  public void testRemoveDatasource_RemoveNonTransientDatasource() {

    createNewDatasource("datasourceToRemove");

    OpalConfigurationService opalRuntimeMock = createMock(OpalConfigurationService.class);
    OpalConfiguration opalConfig = new OpalConfiguration();

    MagmaEngineFactory factory = new MagmaEngineFactory();
    DatasourceFactory excelFactory = new ExcelDatasourceFactory();
    excelFactory.setName("datasourceToRemove");
    factory.withFactory(excelFactory);
    opalConfig.setMagmaEngineFactory(factory);

    expect(opalRuntimeMock.getOpalConfiguration()).andReturn(opalConfig);
    opalRuntimeMock.modifyConfiguration((ConfigModificationTask) EasyMock.anyObject());

    replay(opalRuntimeMock);

    DatasourceResource resource = createDatasource("datasourceToRemove", opalRuntimeMock);
    Response response = resource.removeDatasource();

    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }

  private DatasourceResource createDatasource(String name) {
    return createDatasource(name, createMock(OpalConfigurationService.class));
  }

  private DatasourceResource createDatasource(String name, OpalConfigurationService opalConfigurationService) {
    ViewManager viewManagerMock = createMock(ViewManager.class);
    ImportService importService = createMock(ImportService.class);
    OpalSearchService opalSearchService = new OpalSearchService(
        new ElasticSearchConfigurationService(createMock(OpalConfigurationService.class)));
    StatsIndexManager statsIndexManager = createMock(StatsIndexManager.class);
    ElasticSearchProvider esProvider = createMock(ElasticSearchProvider.class);

    DatasourceResource resource = new DatasourceResource(opalConfigurationService, importService, viewManagerMock,
        opalSearchService, statsIndexManager, esProvider, newViewDtos(),
        Collections.<ValueTableUpdateListener>emptySet());
    resource.setName(name);
    return resource;
  }

  private DatasourceResource createDatasource(String mockDatasourceName, final Datasource mockDatasource,
      OpalConfigurationService mockOpalRuntime, ViewManager mockViewManager) {
    ImportService importService = createMock(ImportService.class);
    OpalSearchService opalSearchService = new OpalSearchService(
        new ElasticSearchConfigurationService(createMock(OpalConfigurationService.class)));
    StatsIndexManager statsIndexManager = createMock(StatsIndexManager.class);
    ElasticSearchProvider esProvider = createMock(ElasticSearchProvider.class);

    DatasourceResource sut = new DatasourceResource(mockOpalRuntime, importService, mockViewManager, opalSearchService,
        statsIndexManager, esProvider, newViewDtos(), Collections.<ValueTableUpdateListener>emptySet()) {

      @Override
      Datasource getDatasource() {
        return mockDatasource;
      }
    };
    sut.setName(mockDatasourceName);

    return sut;
  }

  @Test
  public void testRemoveDatasource_DatasourceNotFound() {
    OpalConfigurationService opalRuntimeMock = createMock(OpalConfigurationService.class);
    ViewManager viewManagerMock = createMock(ViewManager.class);
    ImportService importService = createMock(ImportService.class);
    OpalSearchService opalSearchService = new OpalSearchService(
        new ElasticSearchConfigurationService(createMock(OpalConfigurationService.class)));
    StatsIndexManager statsIndexManager = createMock(StatsIndexManager.class);
    ElasticSearchProvider esProvider = createMock(ElasticSearchProvider.class);

    DatasourceResource resource = new DatasourceResource(opalRuntimeMock, importService, viewManagerMock,
        opalSearchService, statsIndexManager, esProvider, newViewDtos(),
        Collections.<ValueTableUpdateListener>emptySet());
    resource.setName("datasourceNotExist");
    Response response = resource.removeDatasource();

    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void testDatasourcesPOST() {
    OpalConfigurationService opalRuntimeMock = createMock(OpalConfigurationService.class);

    DatasourcesResource resource = new DatasourcesResource(newDatasourceFactoryRegistry(), opalRuntimeMock);

    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilderImpl.fromPath("/"));

    Magma.DatasourceFactoryDto factoryDto = Magma.DatasourceFactoryDto.newBuilder().setName("patate")
        .setExtension(ExcelDatasourceFactoryDto.params,
            Magma.ExcelDatasourceFactoryDto.newBuilder().setFile(getDatasourcePath(DATASOURCE1)).setReadOnly(true)
                .build()).build();

    opalRuntimeMock.modifyConfiguration((ConfigModificationTask) EasyMock.anyObject());

    replay(uriInfoMock, opalRuntimeMock);
    Response response = resource.createDatasource(uriInfoMock, factoryDto);
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    Object entity = response.getEntity();
    Assert.assertNotNull(entity);
    try {
      Magma.DatasourceDto dto = (Magma.DatasourceDto) entity;
      Assert.assertTrue(MagmaEngine.get().hasDatasource(dto.getName()));
      Assert.assertNotNull(response.getMetadata().get("Location"));
      Assert
          .assertEquals("[" + "/datasource/" + dto.getName() + "]", response.getMetadata().get("Location").toString());
    } catch(Exception e) {
      Assert.assertFalse(true);
    }

    verify(uriInfoMock, opalRuntimeMock);
    MagmaEngine.get().removeDatasource(MagmaEngine.get().getDatasource("patate"));
  }

  @Test
  public void testTransientDatasourcesPOST() {
    TransientDatasourcesResource resource = new TransientDatasourcesResource(newDatasourceFactoryRegistry());

    UriInfo uriInfoMock = createMock(UriInfo.class);

    Magma.DatasourceFactoryDto factoryDto = Magma.DatasourceFactoryDto.newBuilder()
        .setExtension(ExcelDatasourceFactoryDto.params,
            Magma.ExcelDatasourceFactoryDto.newBuilder().setFile(getDatasourcePath(DATASOURCE1)).setReadOnly(true)
                .build()).build();

    replay(uriInfoMock);
    Response response = resource.createDatasource(uriInfoMock, factoryDto);
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    Object entity = response.getEntity();
    Assert.assertNotNull(entity);
    try {
      Magma.DatasourceDto dto = (Magma.DatasourceDto) entity;
      Assert.assertTrue(MagmaEngine.get().hasTransientDatasource(dto.getName()));
      Assert.assertNotNull(response.getMetadata().get("Location"));
      Assert
          .assertEquals("[" + "/datasource/" + dto.getName() + "]", response.getMetadata().get("Location").toString());
    } catch(Exception e) {
      Assert.assertFalse(true);
    }

    verify(uriInfoMock);
  }

  @Test
  public void testDatasourcesPOSTUserDefinedBogus() {
    OpalConfigurationService opalRuntimeMock = createMock(OpalConfigurationService.class);
    DatasourcesResource resource = new DatasourcesResource(newDatasourceFactoryRegistry(), opalRuntimeMock);

    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilderImpl.fromPath("/"));

    File file = new File(getFileFromResource(DATASOURCES_FOLDER), "user-defined-bogus.xls");
    Magma.DatasourceFactoryDto factoryDto = Magma.DatasourceFactoryDto.newBuilder()
        .setExtension(ExcelDatasourceFactoryDto.params,
            Magma.ExcelDatasourceFactoryDto.newBuilder().setFile(file.getAbsolutePath()).setReadOnly(true).build())
        .build();

    replay(uriInfoMock);
    Response response = resource.createDatasource(uriInfoMock, factoryDto);
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    ClientErrorDto error = (ClientErrorDto) response.getEntity();
    // System.out.println(JsonFormat.printToString(error));
    Assert.assertEquals("DatasourceCreationFailed", error.getStatus());
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), error.getCode());
    Assert.assertEquals(15, error.getExtensionCount(Magma.DatasourceParsingErrorDto.errors));
    Magma.DatasourceParsingErrorDto parsingError = error.getExtension(Magma.DatasourceParsingErrorDto.errors, 0);
    Assert.assertEquals("DuplicateCategoryName", parsingError.getKey());
    Assert.assertEquals("[Categories, 4, Table1, Var1, C2]", parsingError.getArgumentsList().toString());
  }

  @Test
  public void testTransientDatasourceInstanceGET() {
    ExcelDatasourceFactory factory = new ExcelDatasourceFactory();
    factory.setFile(new File(getDatasourcePath(DATASOURCE1)));
    factory.setReadOnly(true);

    String uid = MagmaEngine.get().addTransientDatasource(factory);

    DatasourceResource resource = createDatasource(uid);

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

    DatasourceResource resource = createDatasource(uid);

    Response response = resource.removeDatasource();

    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
    Assert.assertFalse(MagmaEngine.get().hasTransientDatasource(uid));

    response = resource.removeDatasource();
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void testDatasourceGET() {
    DatasourceResource resource = createDatasource(DATASOURCE1);

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

    replay(datasourceMock, variableWriterMock, valueTableWriterMock);

    MagmaEngine.get().addDatasource(datasourceMock);

    DatasourceResource datasourceResource = createDatasource(datasourceMock.getName());
    Response response = datasourceResource.getTables().createTable(createTableDto());

    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
    Assert
        .assertEquals("/datasource/testDatasource/table/table", response.getMetadata().getFirst("Location").toString());

    MagmaEngine.get().removeDatasource(datasourceMock);

    verify(datasourceMock, variableWriterMock, valueTableWriterMock);

  }

  @Test
  public void testCreateTable_TableAlreadyExist() throws IOException {
    Datasource datasourceMock = createMock(Datasource.class);

    datasourceMock.initialise();
    expect(datasourceMock.getName()).andReturn("testDatasource").atLeastOnce();
    expect(datasourceMock.hasValueTable("table")).andReturn(true);
    datasourceMock.dispose();
    expectLastCall().atLeastOnce();

    replay(datasourceMock);

    MagmaEngine.get().addDatasource(datasourceMock);

    DatasourceResource datasourceResource = createDatasource(datasourceMock.getName());
    Response response = datasourceResource.getTables().createTable(createTableDto());

    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    Assert.assertEquals("TableAlreadyExists", ((ClientErrorDto) response.getEntity()).getStatus());

    MagmaEngine.get().removeDatasource(datasourceMock);

    verify(datasourceMock);
  }

  @Test
  public void testCreateView_AddsViewByDelegatingToViewManager() throws URISyntaxException {
    // Setup
    String mockDatasourceName = "mockDatasource";
    String viewName = "viewToAdd";
    String fqViewName = mockDatasourceName + "." + "fromTable";

    ValueTable mockFromTable = createMock(ValueTable.class);

    Datasource mockDatasource = createMock(Datasource.class);
    mockDatasource.initialise();
    expect(mockDatasource.getName()).andReturn(mockDatasourceName).atLeastOnce();
    expect(mockDatasource.hasValueTable(viewName)).andReturn(false).atLeastOnce();
    expect(mockDatasource.getValueTable("fromTable")).andReturn(mockFromTable).atLeastOnce();
    mockDatasource.dispose();

    JavaScriptViewDto jsViewDto = JavaScriptViewDto.newBuilder().build();
    ViewDto viewDto = ViewDto.newBuilder().setName(viewName).addFrom(fqViewName)
        .setExtension(JavaScriptViewDto.view, jsViewDto).build();

    ViewManager mockViewManager = createMock(ViewManager.class);
    mockViewManager.addView(EasyMock.same(mockDatasourceName), eqView(new View(viewName, mockFromTable)));
    expectLastCall().once();

    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getBaseUri()).andReturn(new URI("http://localhost:8080/ws")).atLeastOnce();

    OpalConfigurationService mockOpalRuntime = createMock(OpalConfigurationService.class);
    DatasourceResource sut = createDatasource(mockDatasourceName, mockDatasource, mockOpalRuntime, mockViewManager);

    replay(mockDatasource, mockFromTable, mockViewManager, uriInfoMock);

    // Exercise
    MagmaEngine.get().addDatasource(mockDatasource);
    Response response = sut.createView(viewDto, uriInfoMock);
    MagmaEngine.get().removeDatasource(mockDatasource);

    // Verify state
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    // Verify behaviour
    verify(mockViewManager, uriInfoMock);

  }

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
    mockViewManager.addView(EasyMock.same(mockDatasourceName), eqView(view));
    expectLastCall().once();

    OpalConfigurationService mockOpalRuntime = createMock(OpalConfigurationService.class);
    DatasourceResource sut = createDatasource(mockDatasourceName, mockDatasource, mockOpalRuntime, mockViewManager);
    sut.setLocalesProperty("en, fr");

    replay(mockDatasource, mockFromTable, mockViewManager);

    // Exercise
    MagmaEngine.get().addDatasource(mockDatasource);
    Response response = sut.getView(viewName).updateView(viewDto);
    MagmaEngine.get().removeDatasource(mockDatasource);

    // Verify state
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    // Verify behaviour
    verify(mockViewManager);
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

    DatasourceResource sut = createDatasource(mockDatasourceName, mockDatasource,
        createMock(OpalConfigurationService.class), mockViewManager);
    sut.setLocalesProperty("en, fr");

    replay(mockDatasource, mockFromTable, mockViewManager);

    // Exercise
    Response response = sut.getView(viewName).removeView();

    // Verify behaviour
    verify(mockViewManager);

    // Verify state
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
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

    DatasourceResource sut = createDatasource(mockDatasourceName, mockDatasource,
        createMock(OpalConfigurationService.class), mockViewManager);
    sut.setLocalesProperty("en");

    replay(mockDatasource, mockViewManager);

    // Exercise
    ViewResource viewResource = sut.getView(viewName);

    // Verify behaviour
    verify(mockViewManager);

    // Verify state
    assertEquals(viewName, viewResource.getValueTable().getName());
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
        createMock(OpalConfigurationService.class), mockViewManager);
    sut.setLocalesProperty("en");

    replay(mockDatasource, mockViewManager);

    // Exercise
    sut.getView(viewName);
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleSpecifiedReturnsLocaleDtosWithDisplayFieldSet() {
    // Setup
    DatasourceResource sut = createDatasource("theDatasource");
    sut.setLocalesProperty("en, fr");

    // Exercise
    String displayLocaleName = "en";
    Iterable<LocaleDto> localeDtos = sut.getLocales(displayLocaleName);

    // Verify
    assertNotNull(localeDtos);
    ImmutableList<LocaleDto> localeDtoList = ImmutableList.copyOf(localeDtos);
    assertEquals(2, localeDtoList.size());
    assertEqualsLocaleDto(localeDtoList.get(0), "en", displayLocaleName);
    assertEqualsLocaleDto(localeDtoList.get(1), "fr", displayLocaleName);
  }

  @Test
  public void testGetLocales_WhenDisplayLocaleNotSpecifiedReturnsLocaleDtosWithDisplayFieldNotSet() {
    // Setup
    DatasourceResource sut = createDatasource("theDatasource");
    sut.setLocalesProperty("en, fr");

    // Exercise
    Iterable<LocaleDto> localeDtos = sut.getLocales(null);

    // Verify
    assertNotNull(localeDtos);
    ImmutableList<LocaleDto> localeDtoList = ImmutableList.copyOf(localeDtos);
    assertEquals(2, localeDtoList.size());
    assertEqualsLocaleDto(localeDtoList.get(0), "en", null);
    assertEqualsLocaleDto(localeDtoList.get(1), "fr", null);
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

  private DatasourceFactoryRegistry newSpssDatasourceFactoryRegistry() {
    return new DatasourceFactoryRegistry(
        ImmutableSet.<DatasourceFactoryDtoParser>of(new SpssDatasourceFactoryDtoParser() {
          @Override
          protected File resolveLocalFile(String path) {
            return new File(path);
          }
        }));
  }

  private ViewDtos newViewDtos() {
    return new ViewDtos(
        ImmutableSet.<ViewDtoExtension>of(new JavaScriptViewDtoExtension(), new VariableListViewDtoExtension()));
  }

  private TableDto createTableDto() {
    TableDto.Builder builder = TableDto.newBuilder().setName("table").setEntityType("entityType");
    builder.addVariables(VariableDto.newBuilder().setName("name").setEntityType("entityType").setValueType("text")
        .setIsRepeatable(true));
    return builder.build();
  }

  private void assertEqualsLocaleDto(LocaleDto localeDto, String localeName, String displayLocaleName) {
    assertEquals(localeName, localeDto.getName());

    assertEquals(displayLocaleName != null, localeDto.hasDisplay());

    if(localeDto.hasDisplay()) {
      assertEquals(new Locale(localeName).getDisplayName(new Locale(displayLocaleName)), localeDto.getDisplay());
    }
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
  static View eqView(View in) {
    EasyMock.reportMatcher(new ViewMatcher(in));
    return null;
  }
}
