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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.service.UnitKeyStoreService;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.TableResource;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import de.schlichtherle.io.FileInputStream;

@Component
@Path("/functional-units")
public class FunctionalUnitsResource {

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitsResource.class);

  private OpalRuntime opalRuntime;

  private final UnitKeyStoreService unitKeyStoreService;

  private final ImportService importService;

  private final DatasourceFactoryRegistry datasourceFactoryRegistry;

  private final String keysTableReference;

  @Autowired
  public FunctionalUnitsResource(OpalRuntime opalRuntime, UnitKeyStoreService unitKeyStoreService, ImportService importService, DatasourceFactoryRegistry datasourceFactoryRegistry, @Value("${org.obiba.opal.keys.tableReference}") String keysTableReference) {
    super();
    this.opalRuntime = opalRuntime;
    this.unitKeyStoreService = unitKeyStoreService;
    this.importService = importService;
    this.datasourceFactoryRegistry = datasourceFactoryRegistry;
    this.keysTableReference = keysTableReference;
  }

  //
  // Functional Units
  //

  @GET
  public List<Opal.FunctionalUnitDto> getFunctionalUnits() {
    final List<Opal.FunctionalUnitDto> functionalUnits = Lists.newArrayList();
    for(FunctionalUnit functionalUnit : opalRuntime.getFunctionalUnits()) {
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
    if(opalRuntime.getOpalConfiguration().hasFunctionalUnit(unit.getName())) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitAlreadyExists").build()).build();
    }

    ResponseBuilder response = null;
    try {
      FunctionalUnit functionalUnit = new FunctionalUnit(unit.getName(), unit.getKeyVariableName());
      if(unit.hasSelect()) {
        functionalUnit.setSelect(new JavascriptClause(unit.getSelect()));
      }
      functionalUnit.setUnitKeyStoreService(unitKeyStoreService);

      opalRuntime.getOpalConfiguration().addOrReplaceFunctionalUnit(functionalUnit);
      opalRuntime.writeOpalConfiguration();

      try {
        prepareKeysTable(unit);
        response = Response.created(UriBuilder.fromPath("/").path(FunctionalUnitResource.class).build(unit.getName()));
      } catch(IOException e) {
        opalRuntime.getOpalConfiguration().removeFunctionalUnit(unit.getName());
        response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitCreationFailed", e).build());
      }

    } catch(RuntimeException e) {
      response = Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "FunctionalUnitCreationFailed", e).build());
    }

    return response.build();
  }

  private void prepareKeysTable(Opal.FunctionalUnitDto unit) throws IOException {
    // add unit key variable in identifiers table
    ValueTable keysTable = getKeysTable();
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
    return new TableResource(getKeysTable());
  }

  @GET
  @Path("/entities/excel")
  @Produces("application/vnd.ms-excel")
  public Response getExcelIdentifiers() throws MagmaRuntimeException, IOException {
    try {
      String destinationName = getKeysDatasourceName();
      ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
      ExcelDatasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);

      copyEntities(destinationDatasource);

      return Response.ok(excelOutput.toByteArray(), "application/vnd.ms-excel").header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
    } catch(NoSuchFunctionalUnitException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("/entities/csv")
  @Produces("text/csv")
  public Response getCSVIdentifiers() throws MagmaRuntimeException, IOException {
    try {
      String destinationName = getKeysDatasourceName();

      CsvDatasource destinationDatasource = new CsvDatasource(destinationName);
      File output = File.createTempFile("ids-", ".csv");
      destinationDatasource.addValueTable(getKeysTable().getName(), null, output);

      copyEntities(destinationDatasource);

      ByteArrayOutputStream bos = copyFile(output);
      output.delete();

      return Response.ok(bos.toByteArray(), "text/csv").header("Content-Disposition", "attachment; filename=\"" + destinationName + ".csv\"").build();
    } catch(NoSuchFunctionalUnitException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private ByteArrayOutputStream copyFile(File file) throws IOException {
    InputStream fis = new FileInputStream(file);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    for(int readNum; (readNum = fis.read(buf)) != -1;) {
      bos.write(buf, 0, readNum);
    }

    bos.flush();
    bos.close();
    fis.close();

    return bos;
  }

  private void copyEntities(Datasource destinationDatasource) throws IOException {
    Initialisables.initialise(destinationDatasource);
    try {
      DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyMetadata().build();
      copier.copy(getKeysTable(), destinationDatasource);
    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }
  }

  @POST
  @Path("/entities")
  public Response importIdentifiers(DatasourceFactoryDto datasourceFactoryDto) {
    Response response = null;

    Datasource sourceDatasource = createTransientDatasource(datasourceFactoryDto);

    try {
      importService.importIdentifiers(sourceDatasource.getName());
      response = Response.ok().build();
    } catch(NoSuchDatasourceException ex) {
      response = Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "DatasourceNotFound", ex).build()).build();
    } catch(NoSuchValueTableException ex) {
      response = Response.status(Status.NOT_FOUND).entity(ClientErrorDtos.getErrorMessage(Status.NOT_FOUND, "ValueTableNotFound", ex).build()).build();
    } catch(IOException ex) {
      response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex).build()).build();
    } finally {
      Disposables.silentlyDispose(sourceDatasource);
    }

    return response;
  }

  //
  // Private methods
  //

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

  private ValueTable getKeysTable() {
    return MagmaEngineTableResolver.valueOf(keysTableReference).resolveTable();
  }

  private String getKeysDatasourceName() {
    return MagmaEngineTableResolver.valueOf(keysTableReference).getDatasourceName();
  }

}
