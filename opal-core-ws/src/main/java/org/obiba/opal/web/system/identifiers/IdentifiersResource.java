package org.obiba.opal.web.system.identifiers;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VectorSource;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitIdentifiers;
import org.obiba.opal.web.magma.DatasourceTablesResource;
import org.obiba.opal.web.magma.DroppableTableResource;
import org.obiba.opal.web.magma.TableResource;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Component
@Transactional
@Scope("request")
@Path("/system/identifiers")
@Api(value = "/system/identifiers", description = "Operations about identifiers")
public class IdentifiersResource {

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

  private Datasource getDatasource() {
    return identifiersTableService.getDatasource();
  }

  private Set<Locale> getLocales() {
    // Get locales from server config
    return Sets.newHashSet(serverService.getConfig().getLocales());
  }

}
