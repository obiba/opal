/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.core.unit.FunctionalUnitIdentifiers;
import org.obiba.opal.core.unit.FunctionalUnitIdentifiers.UnitIdentifier;
import org.obiba.opal.core.unit.FunctionalUnitService;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.TableResource;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.TableIdentifiersSync;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.FunctionalUnitDto;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
@Path("/functional-units")
public class FunctionalUnitsResource extends AbstractFunctionalUnitResource {

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitsResource.class);

  private final FunctionalUnitService functionalUnitService;

  private final OpalRuntime opalRuntime;

  private final UnitKeyStoreService unitKeyStoreService;

  private final ImportService importService;

  private final DatasourceFactoryRegistry datasourceFactoryRegistry;

  private final IdentifiersTableService identifiersTableService;

  @Autowired
  public FunctionalUnitsResource(FunctionalUnitService functionalUnitService, OpalRuntime opalRuntime, UnitKeyStoreService unitKeyStoreService, ImportService importService, DatasourceFactoryRegistry datasourceFactoryRegistry, IdentifiersTableService identifiersTableResolver) {
    super();
    this.functionalUnitService = functionalUnitService;
    this.opalRuntime = opalRuntime;
    this.unitKeyStoreService = unitKeyStoreService;
    this.importService = importService;
    this.datasourceFactoryRegistry = datasourceFactoryRegistry;
    this.identifiersTableService = identifiersTableResolver;
  }

  //
  // Functional Units
  //

  @GET
  public List<Opal.FunctionalUnitDto> getFunctionalUnits() {
    final List<Opal.FunctionalUnitDto> functionalUnits = Lists.newArrayList();
    for(FunctionalUnit functionalUnit : getFunctionalUnitService().getFunctionalUnits()) {
      Opal.FunctionalUnitDto.Builder fuBuilder = Opal.FunctionalUnitDto.newBuilder().setName(functionalUnit.getName()).setKeyVariableName(functionalUnit.getKeyVariableName());
      if(functionalUnit.getSelect() instanceof JavascriptClause) {
        fuBuilder.setSelect(((JavascriptClause) functionalUnit.getSelect()).getScript());
      }
      functionalUnits.add(fuBuilder.build());
    }
    sortByName(functionalUnits);
    return functionalUnits;
  }

  @POST
  public Response createFunctionalUnit(Opal.FunctionalUnitDto unit) {
    if(getFunctionalUnitService().hasFunctionalUnit(unit.getName())) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitAlreadyExists").build()).build();
    }

    ResponseBuilder response = null;
    try {
      FunctionalUnit functionalUnit = new FunctionalUnit(unit.getName(), unit.getKeyVariableName());
      if(unit.hasSelect()) {
        functionalUnit.setSelect(new JavascriptClause(unit.getSelect()));
      }
      functionalUnit.setUnitKeyStoreService(unitKeyStoreService);

      getFunctionalUnitService().addOrReplaceFunctionalUnit(functionalUnit);

      try {
        prepareKeysTable(unit);
        createUnitsDirectory(unit.getName());
        response = Response.created(UriBuilder.fromPath("/").path(FunctionalUnitResource.class).build(unit.getName()));
      } catch(IOException e) {
        getFunctionalUnitService().removeFunctionalUnit(unit.getName());
        response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitCreationFailed", e).build());
      }

    } catch(RuntimeException e) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitCreationFailed", e).build());
    }

    return response.build();
  }

  private void createUnitsDirectory(String unitName) throws FileSystemException {
    FileObject unitsDir = resolveFileInFileSystem("/units/" + unitName);
    unitsDir.createFolder();
  }

  private void prepareKeysTable(Opal.FunctionalUnitDto unit) throws IOException {
    // add unit key variable in identifiers table
    ValueTable keysTable = identifiersTableService.getValueTable();
    if(!keysTable.hasVariable(unit.getKeyVariableName())) {
      Variable keyVariable = Variable.Builder.newVariable(unit.getKeyVariableName(), TextType.get(), keysTable.getEntityType()).build();

      ValueTableWriter writer = keysTable.getDatasource().createWriter(keysTable.getName(), keysTable.getEntityType());
      try {
        VariableWriter vw = writer.writeVariables();
        try {
          // Create private variables
          vw.writeVariable(keyVariable);
        } finally {
          vw.close();
        }
      } finally {
        writer.close();
      }
    }
  }

  //
  // Entities
  //

  @Path("/entities/table")
  public TableResource getEntitiesTable() {
    return new TableResource(identifiersTableService.getValueTable());
  }

  @GET
  @Path("/entities/csv")
  @Produces("text/csv")
  @AuthenticatedByCookie
  public Response getCSVIdentifiers() throws MagmaRuntimeException, IOException {
    try {
      String destinationName = identifiersTableService.getValueTable().getDatasource().getName();

      ByteArrayOutputStream ids = new ByteArrayOutputStream();
      PrintWriter writer = new PrintWriter(ids);
      List<Iterator<UnitIdentifier>> unitIdIters = writeIdentifiersHeader(writer);
      // value sets
      if(unitIdIters.size() > 0) {
        writeUnitIdentifiers(writer, unitIdIters);
      } else {
        writeOpalIdentifiers(writer);
      }
      writer.close();

      return Response.ok(ids.toByteArray(), "text/csv").header("Content-Disposition", "attachment; filename=\"" + destinationName + ".csv\"").build();
    } catch(NoSuchFunctionalUnitException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private List<Iterator<UnitIdentifier>> writeIdentifiersHeader(PrintWriter writer) {
    ValueTable keysTable = identifiersTableService.getValueTable();
    List<Iterator<UnitIdentifier>> unitIdIters = new ArrayList<Iterator<UnitIdentifier>>();

    // header
    writer.append('"').append(FunctionalUnit.OPAL_INSTANCE).append('"');
    for(FunctionalUnit functionalUnit : getFunctionalUnitService().getFunctionalUnits()) {
      if(keysTable.hasVariable(functionalUnit.getKeyVariableName())) {
        unitIdIters.add(new FunctionalUnitIdentifiers(keysTable, functionalUnit).iterator());
        writer.append(",\"").append(functionalUnit.getName()).append('"');
      }
    }
    writer.append('\n');

    return unitIdIters;
  }

  private void writeUnitIdentifiers(PrintWriter writer, List<Iterator<UnitIdentifier>> unitIdIters) {
    while(unitIdIters.get(0).hasNext()) {
      // opal and unit identifiers
      boolean opalIdWritten = false;
      for(Iterator<UnitIdentifier> unitIdsIter : unitIdIters) {
        UnitIdentifier unitIdentifier = unitIdsIter.next();
        if(!opalIdWritten) {
          writer.append('\"').append(unitIdentifier.getOpalIdentifier()).append('\"');
          opalIdWritten = true;
        }
        writer.append(',').append(unitIdentifier.hasUnitIdentifier() ? "\"" + unitIdentifier.getUnitIdentifier() + "\"" : "");
      }
      writer.append('\n');
    }
  }

  private void writeOpalIdentifiers(PrintWriter writer) {
    // no unit: list of opal ids
    TreeSet<VariableEntity> opalEntities = new TreeSet<VariableEntity>(identifiersTableService.getValueTable().getVariableEntities());
    for(VariableEntity entity : opalEntities) {
      writer.append('\"').append(entity.getIdentifier()).append("\"\n");
    }
  }

  /**
   * All identifiers from the transient datasource (given the datasource factory) will be imported.
   * @param datasourceFactoryDto
   * @return
   */
  @POST
  @Path("/entities")
  public Response importIdentifiers(DatasourceFactoryDto datasourceFactoryDto) {
    try {
      importIdentifiersFromTransientDatasource(datasourceFactoryDto);
      return Response.ok().build();
    } catch(NoSuchDatasourceException ex) {
      return Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "DatasourceNotFound", ex).build()).build();
    } catch(NoSuchValueTableException ex) {
      return Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "ValueTableNotFound", ex).build()).build();
    } catch(IOException ex) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex).build()).build();
    }
  }

  /**
   * If a datasource name is provided, it will be used to import all identifiers from this datasource or just the
   * identifiers from the provided table name in the datasource. Else all datasources identifiers will be imported.
   * @param datasource
   * @param table
   * @return
   */
  @POST
  @Path("/entities/sync")
  public Response importIdentifiers(@QueryParam("datasource")
  String datasource, @QueryParam("table")
  String table) {
    try {
      if(datasource != null) {
        Datasource ds = MagmaEngine.get().getDatasource(datasource);
        if(table != null) {
          importIdentifiersFromTable(ds.getValueTable(table));
        } else {
          importIdentifiersFromDatasource(ds);
        }
      } else {
        importIdentifiersFromAllDatasources();
      }
      return Response.ok().build();
    } catch(NoSuchDatasourceException ex) {
      return Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "DatasourceNotFound", ex).build()).build();
    } catch(NoSuchValueTableException ex) {
      return Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "ValueTableNotFound", ex).build()).build();
    } catch(IOException ex) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex).build()).build();
    }
  }

  @GET
  @Path("/entities/sync")
  public List<TableIdentifiersSync> getIdentifiersToBeImported(@QueryParam("datasource")
  String datasource, @QueryParam("table")
  String table) {
    Datasource ds = MagmaEngine.get().getDatasource(datasource);

    ImmutableList.Builder<TableIdentifiersSync> builder = ImmutableList.builder();

    Iterable<ValueTable> tables = Iterables.filter(table == null ? ds.getValueTables() : Collections.singleton(ds.getValueTable(table)), new Predicate<ValueTable>() {

      @Override
      public boolean apply(ValueTable input) {
        return input.getEntityType().equals(identifiersTableService.getEntityType());
      }
    });

    Set<VariableEntity> entities = identifiersTableService.getValueTable().getVariableEntities();
    for(ValueTable vt : tables) {
      TableIdentifiersSync tsync = TableIdentifiersSync.newBuilder()//
      .setDatasource(ds.getName()).setTable(vt.getName())//
      .setCount(getIdentifiersToBeImportedCount(entities, vt)).build();
      builder.add(tsync);
    }

    return builder.build();
  }

  private int getIdentifiersToBeImportedCount(Set<VariableEntity> entities, ValueTable vt) {
    int count = 0;
    for(VariableEntity entity : vt.getVariableEntities()) {
      if(!entities.contains(entity)) {
        log.info("{}: {}", vt.getName(), entity.getIdentifier());
        count++;
      }
    }
    return count;
  }

  @GET
  @Path("/entities/identifiers/map/units")
  public List<FunctionalUnitDto> getUnitsFromIdentifiersMap(@QueryParam("path")
  String path) throws IOException {
    // check the headers

    File mapFile = resolveLocalFile(path);
    CSVReader reader = new CSVReader(new FileReader(mapFile.getPath()));

    // find the units
    List<FunctionalUnitDto> unitDtos = new ArrayList<FunctionalUnitDto>();
    for(FunctionalUnit functionalUnit : getUnitsFromIdentifiersMap(reader)) {
      Opal.FunctionalUnitDto.Builder fuBuilder = Opal.FunctionalUnitDto.newBuilder().//
      setName(functionalUnit.getName()). //
      setKeyVariableName(functionalUnit.getKeyVariableName());
      unitDtos.add(fuBuilder.build());
    }

    return unitDtos;
  }

  //
  // Private methods
  //

  private void importIdentifiersFromTransientDatasource(DatasourceFactoryDto datasourceFactoryDto) throws NoSuchValueTableException, IOException {
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
    if(sourceTable.getEntityType().equals(identifiersTableService.getEntityType())) {
      importService.importIdentifiers(sourceTable);
    }
  }

  private Datasource createTransientDatasource(DatasourceFactoryDto datasourceFactoryDto) {
    DatasourceFactory factory = datasourceFactoryRegistry.parse(datasourceFactoryDto);
    String uid = MagmaEngine.get().addTransientDatasource(factory);

    return MagmaEngine.get().getTransientDatasourceInstance(uid);
  }

  private void sortByName(List<Opal.FunctionalUnitDto> units) {
    // sort alphabetically
    Collections.sort(units, new Comparator<Opal.FunctionalUnitDto>() {

      @Override
      public int compare(Opal.FunctionalUnitDto d1, Opal.FunctionalUnitDto d2) {
        return d1.getName().compareTo(d2.getName());
      }

    });
  }

  @Override
  protected OpalRuntime getOpalRuntime() {
    return opalRuntime;
  }

  @Override
  protected FunctionalUnitService getFunctionalUnitService() {
    return functionalUnitService;
  }
}
