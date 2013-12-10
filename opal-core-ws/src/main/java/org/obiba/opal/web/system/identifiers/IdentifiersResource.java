package org.obiba.opal.web.system.identifiers;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.web.magma.DatasourceTablesResource;
import org.obiba.opal.web.magma.DroppableTableResource;
import org.obiba.opal.web.magma.TableResource;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Component
@Transactional
@Scope("request")
@Path("/system/identifiers")
@Api(value = "/system/identifiers", description = "Operations about identifiers")
public class IdentifiersResource extends AbstractIdentifiersResource {

  private OpalGeneralConfigService serverService;

  private IdentifiersTableService identifiersTableService;

  private ApplicationContext applicationContext;

  @Autowired
  public void setServerService(OpalGeneralConfigService serverService) {
    this.serverService = serverService;
  }

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Autowired
  public void setIdentifiersTableService(IdentifiersTableService identifiersTableService) {
    this.identifiersTableService = identifiersTableService;
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
  public List<Opal.IdentifiersMappingDto> getIdentifiersMappings() {
    ImmutableList.Builder<Opal.IdentifiersMappingDto> builder = ImmutableList.builder();
    for(ValueTable table : getDatasource().getValueTables()) {
      Opal.IdentifiersMappingDto.Builder b = Opal.IdentifiersMappingDto.newBuilder();
      b.setEntityType(table.getEntityType());
      for(Variable variable : table.getVariables()) {
        b.addUnits(variable.getName());
      }
      builder.add(b.build());
    }
    return builder.build();
  }

  @GET
  @Path("/mapping/{type}")
  @ApiOperation(value = "Get the identifiers mapping for an entity type")
  public Opal.IdentifiersMappingDto getIdentifiersMapping(
      @PathParam("type") @DefaultValue("Participant") String entityType) {
    ValueTable table = getValueTable(entityType);
    if(table == null) throw new NoSuchElementException("No identifiers mapping found for entity type: " + entityType);

    Opal.IdentifiersMappingDto.Builder b = Opal.IdentifiersMappingDto.newBuilder();
    b.setEntityType(table.getEntityType());
    for(Variable variable : table.getVariables()) {
      b.addUnits(variable.getName());
    }
    return b.build();
  }

  //
  // Private methods
  //

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
