package org.obiba.opal.web.system.identifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.web.magma.DatasourceTablesResource;
import org.obiba.opal.web.magma.DroppableTableResource;
import org.obiba.opal.web.magma.TableResource;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.IdentifiersMappingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
  public List<Opal.IdentifiersMappingDto> getIdentifiersMappings(final @QueryParam("type") List<String> entityTypes) {
    Map<String, List<String>> idsMappings = getIdentifiersMappings();
    List<IdentifiersMappingDto> dtos = Lists.newArrayList();
    for(String idsMapping : idsMappings.keySet()) {

      Iterable<String> types = Iterables.filter(idsMappings.get(idsMapping), new Predicate<String>() {
        @Override
        public boolean apply(@Nullable String input) {
          if (entityTypes == null || entityTypes.isEmpty()) return true;
          for (String type : entityTypes) {
            if (type.toLowerCase().equals(input.toLowerCase())) return true;
          }
          return false;
        }
      });

      if(Iterables.size(types) > 0)
        dtos.add(IdentifiersMappingDto.newBuilder().setName(idsMapping).addAllEntityTypes(types).build());
    }
    return dtos;
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
