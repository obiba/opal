package org.obiba.opal.web.identifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.Disposables;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.IdentifiersImportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.DatasourceTablesResource;
import org.obiba.opal.web.magma.DroppableTableResource;
import org.obiba.opal.web.magma.TableResource;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.IdentifiersMappingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Component
@Transactional
@Scope("request")
@Path("/identifiers")
@Api(value = "/identifiers", description = "Operations about identifiers")
public class IdentifiersResource extends AbstractIdentifiersResource {

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private DatasourceFactoryRegistry datasourceFactoryRegistry;

  @Autowired
  private OpalGeneralConfigService serverService;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private IdentifiersImportService identifiersImportService;

  @Override
  protected OpalRuntime getOpalRuntime() {
    return opalRuntime;
  }

  @Override
  protected IdentifiersTableService getIdentifiersTableService() {
    return identifiersTableService;
  }

  @Path("/tables")
  public DatasourceTablesResource getTables() {
    DatasourceTablesResource resource = applicationContext.getBean(DatasourceTablesResource.class);
    resource.setDatasource(getDatasource());
    return resource;
  }

  @Path("/table/{table}")
  public TableResource getTable(@PathParam("table") String table) {
    return getTableResource(getDatasource().getValueTable(table));
  }

  @GET
  @Path("/mappings")
  @ApiOperation(value = "Get the identifiers mappings")
  public List<Opal.IdentifiersMappingDto> getIdentifiersMappings(final @QueryParam("type") List<String> entityTypes) {
    Map<String, List<String>> idsMappings = getIdentifiersMappings();
    List<IdentifiersMappingDto> dtos = Lists.newArrayList();
    for(String idsMapping : idsMappings.keySet()) {

      Iterable<String> types = Iterables.filter(idsMappings.get(idsMapping), new Predicate<String>() {
        @Override
        public boolean apply(@Nullable String input) {
          if(entityTypes == null || entityTypes.isEmpty()) return true;
          for(String type : entityTypes) {
            if(type.toLowerCase().equals(input.toLowerCase())) return true;
          }
          return false;
        }
      });

      if(Iterables.size(types) > 0)
        dtos.add(IdentifiersMappingDto.newBuilder().setName(idsMapping).addAllEntityTypes(types).build());
    }
    return dtos;
  }

  @POST
  @Path("/mappings/entities")
  public Response importIdentifiers(Magma.DatasourceFactoryDto datasourceFactoryDto) {
    try {
      importIdentifiersFromTransientDatasource(datasourceFactoryDto);
      return Response.ok().build();
    } catch(NoSuchDatasourceException ex) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.NOT_FOUND, "DatasourceNotFound", ex)).build();
    } catch(NoSuchValueTableException ex) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.NOT_FOUND, "ValueTableNotFound", ex)).build();
    } catch(IOException ex) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          ClientErrorDtos.getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex))
          .build();
    }
  }

  /**
   * If a datasource name is provided, it will be used to import all identifiers from this datasource or just the
   * identifiers from the provided table name in the datasource. Else all datasources identifiers will be imported.
   *
   * @param datasource
   * @param table
   * @return
   */
  @POST
  @Path("/mappings/entities/sync")
  public Response importIdentifiers(@QueryParam("datasource") String datasource,
      @SuppressWarnings("TypeMayBeWeakened") @QueryParam("table") List<String> tableList) {
    try {
      if(datasource != null) {
        Datasource ds = MagmaEngine.get().getDatasource(datasource);
        if(tableList != null && tableList.size() > 0) {
          for(String table : tableList) {
            importIdentifiersFromTable(ds.getValueTable(table));
          }
        } else {
          importIdentifiersFromDatasource(ds);
        }
      } else {
        importIdentifiersFromAllDatasources();
      }
      return Response.ok().build();
    } catch(NoSuchDatasourceException ex) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.NOT_FOUND, "DatasourceNotFound", ex)).build();
    } catch(NoSuchValueTableException ex) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.NOT_FOUND, "ValueTableNotFound", ex)).build();
    } catch(IOException ex) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          ClientErrorDtos.getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex))
          .build();
    }
  }

  @GET
  @Path("/mappings/entities/sync")
  public List<Magma.TableIdentifiersSync> getIdentifiersToBeImported(@QueryParam("datasource") String datasource,
      @SuppressWarnings("TypeMayBeWeakened") @QueryParam("table") List<String> tableList) {
    final Datasource ds = MagmaEngine.get().getDatasource(datasource);

    ImmutableList.Builder<Magma.TableIdentifiersSync> builder = ImmutableList.builder();

    Iterable<ValueTable> tables = Iterables.filter(tableList == null || tableList.isEmpty()
        ? ds.getValueTables()
        : Iterables.transform(tableList, new Function<String, ValueTable>() {

          @Override
          public ValueTable apply(String input) {
            return ds.getValueTable(input);
          }
        }), new Predicate<ValueTable>() {

      @Override
      public boolean apply(ValueTable input) {
        return identifiersTableService.hasIdentifiersTable(input.getEntityType());
      }
    });

    for(ValueTable vt : tables) {
      Set<VariableEntity> entities = identifiersTableService.getIdentifiersTable(vt.getEntityType()).getVariableEntities();
      builder.add(getTableIdentifiersSync(entities, ds, vt));
    }

    return builder.build();
  }

  //
  // Private methods
  //

  private Magma.TableIdentifiersSync getTableIdentifiersSync(Collection<VariableEntity> entities, Datasource ds,
      ValueTable vt) {
    int count = 0;
    Set<VariableEntity> tableEntities = vt.getVariableEntities();
    Magma.TableIdentifiersSync.Builder builder = Magma.TableIdentifiersSync.newBuilder()//
        .setDatasource(ds.getName()).setTable(vt.getName()).setTotal(tableEntities.size());

    for(VariableEntity entity : tableEntities) {
      if(!entities.contains(entity)) {
        count++;
      }
    }
    builder.setCount(count);

    return builder.build();
  }

  private void importIdentifiersFromTransientDatasource(Magma.DatasourceFactoryDto datasourceFactoryDto)
      throws NoSuchValueTableException, IOException {
    Datasource sourceDatasource = createTransientDatasource(datasourceFactoryDto);
    try {
      importIdentifiersFromDatasource(sourceDatasource);
    } finally {
      Disposables.silentlyDispose(sourceDatasource);
    }
  }

  private void importIdentifiersFromAllDatasources() throws IOException {
    for(Datasource ds : MagmaEngine.get().getDatasources()) {
      importIdentifiersFromDatasource(ds);
    }
  }

  private void importIdentifiersFromDatasource(Datasource sourceDatasource) throws IOException {
    for(ValueTable sourceTable : sourceDatasource.getValueTables()) {
      importIdentifiersFromTable(sourceTable);
    }
  }

  private void importIdentifiersFromTable(ValueTable sourceTable) throws IOException {
    if(identifiersTableService.hasIdentifiersTable(sourceTable.getEntityType())) {
      identifiersImportService.importIdentifiers(sourceTable);
    }
  }

  private Datasource createTransientDatasource(Magma.DatasourceFactoryDto datasourceFactoryDto) {
    DatasourceFactory factory = datasourceFactoryRegistry.parse(datasourceFactoryDto);
    String uid = MagmaEngine.get().addTransientDatasource(factory);

    return MagmaEngine.get().getTransientDatasourceInstance(uid);
  }

  //
  // Private methods
  //

  private Map<String, List<String>> getIdentifiersMappings() {
    Map<String, List<String>> idsMappings = Maps.newHashMap();
    for(ValueTable table : getDatasource().getValueTables()) {
      for(Variable variable : table.getVariables()) {
        if(!idsMappings.containsKey(variable.getName())) {
          idsMappings.put(variable.getName(), new ArrayList<String>());
        }
        idsMappings.get(variable.getName()).add(table.getEntityType());
      }
    }
    return idsMappings;
  }

  private TableResource getTableResource(ValueTable table) {
    TableResource resource = getDatasource().canDropTable(table.getName())
        ? applicationContext.getBean("droppableTableResource", DroppableTableResource.class)
        : applicationContext.getBean("tableResource", TableResource.class);
    resource.setValueTable(table);
    resource.setLocales(getLocales());
    return resource;
  }

  private Set<Locale> getLocales() {
    // Get locales from server config
    return Sets.newHashSet(serverService.getConfig().getLocales());
  }

}
