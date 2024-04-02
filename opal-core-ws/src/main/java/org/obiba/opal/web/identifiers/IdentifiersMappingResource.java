/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.identifiers;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.obiba.magma.*;
import org.obiba.magma.datasource.csv.support.CsvDatasourceFactory;
import org.obiba.magma.support.Disposables;
import org.obiba.opal.core.identifiers.IdentifierGeneratorImpl;
import org.obiba.opal.core.identifiers.IdentifiersMapping;
import org.obiba.opal.core.identifiers.IdentifiersMaps;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.service.IdentifiersImportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.io.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Transactional
@Scope("request")
@Path("/identifiers/mapping/{name}")
public class IdentifiersMappingResource extends AbstractIdentifiersResource {

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private IdentifiersImportService identifiersImportService;

  @Autowired
  private DatasourceFactoryRegistry datasourceFactoryRegistry;

  @PathParam("name")
  private String name;

  @Override
  protected IdentifiersTableService getIdentifiersTableService() {
    return identifiersTableService;
  }

  @Override
  protected OpalFileSystemService getOpalFileSystemService() {
    return opalFileSystemService;
  }

  @GET
  public Magma.VariableDto get(@QueryParam("type") String entityType) {
    ensureEntityType(entityType);
    ValueTable table = getValueTable(entityType);
    Variable variable = table.getVariable(name);

    return Dtos.asDto(variable).build();
  }

  @DELETE
  public Response delete(@QueryParam("type") String entityType) {
    ensureEntityType(entityType);

    ValueTable table = getValueTable(entityType);
    // The variable must exist
    Variable v = table.getVariable(name);
    try(ValueTableWriter tableWriter = table.getDatasource().createWriter(table.getName(), table.getEntityType());
        ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      variableWriter.removeVariable(v);
      return Response.ok().build();
    }
  }

  @GET
  @Path("/entities")
  public List<Magma.VariableEntityDto> getUnitEntities(@QueryParam("type") String entityType) {
    ensureEntityType(entityType);
    return Lists.newArrayList(Iterables
        .transform(new IdentifiersMaps(getValueTable(entityType), name).getPrivateEntities(),
            Dtos.variableEntityAsDtoFunc));
  }

  @GET
  @Path("/_count")
  public String getEntitiesCount(@QueryParam("type") String entityType) {
    ensureEntityType(entityType);
    return String.valueOf(Iterables.size(getUnitIdentifiers(entityType)));
  }

  /**
   * Copy the provided identifiers mapping into the corresponding identifiers table. New system identifiers will be
   * added, existing mapped identifiers will be overridden.
   *
   * @param entityType
   * @param separator
   * @param identifiersMap
   * @return
   */
  @POST
  @Consumes("text/plain")
  @Path("/_import")
  public Response importIdentifiers(@QueryParam("type") String entityType,
      @QueryParam("separator") @DefaultValue(",") String separator, String identifiersMap) {
    ensureEntityType(entityType);
    Response response = null;
    File csvData = null;
    try {
      CsvDatasourceFactory factory = new CsvDatasourceFactory();
      csvData = File.createTempFile("opal", ".csv");
      PrintWriter writer = new PrintWriter(csvData);
      writer.println("ID" + separator + name);
      writer.print(identifiersMap);
      writer.flush();
      writer.close();
      factory.setSeparator(separator);
      factory.addTable(entityType, csvData, entityType);
      response = copyIdentifiers(createTransientDatasource(factory));

    } catch(IOException e) {
      response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          ClientErrorDtos.getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", e))
          .build();
    } finally {
      if(csvData != null) csvData.delete();
    }

    return response;
  }

  @POST
  @Path("/_import")
  public Response importIdentifiers(Magma.DatasourceFactoryDto datasourceFactoryDto,
      @QueryParam("type") String entityType, @QueryParam("select") String select) {
    ensureEntityType(entityType);
    return importIdentifiers(createTransientDatasource(datasourceFactoryDto), entityType, select);
  }

  /**
   * Generate identifiers.
   *
   * @param entityType
   * @param size
   * @param zeros
   * @param prefix
   * @return
   */
  @POST
  @Path("/_generate")
  public Response importIdentifiers(@QueryParam("type") String entityType, @QueryParam("size") Integer size,
      @QueryParam("zeros") Boolean zeros, @QueryParam("checksum") Boolean checksum, @QueryParam("prefix") String prefix) {
    ensureEntityType(entityType);
    try {
      IdentifierGeneratorImpl pId = new IdentifierGeneratorImpl();
      if(size != null) pId.setKeySize(size);
      if(zeros != null) pId.setAllowStartWithZero(zeros);
      if(prefix != null) pId.setPrefix(prefix);
      if(checksum != null) pId.setWithCheckDigit(checksum);

      int count = identifiersImportService.importIdentifiers(new IdentifiersMapping(name, entityType), pId);
      return Response.ok().entity(Integer.toString(count)).build();
    } catch(MagmaRuntimeException ex) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "ImportIdentifiersError", ex))
          .build();
    }
  }

  /**
   * Get the non-null values of a variable's vector in CSV format.
   *
   * @return
   * @throws org.obiba.magma.MagmaRuntimeException
   * @throws java.io.IOException
   */
  @GET
  @Path("/_export")
  @Produces("text/csv")
  @AuthenticatedByCookie
  public Response getVectorCSVValues(@QueryParam("type") String entityType) throws MagmaRuntimeException, IOException {
    ensureEntityType(entityType);
    ValueTable table = getValueTable(entityType);
    Variable variable = table.getVariable(name);

    ByteArrayOutputStream values = new ByteArrayOutputStream();
    CSVWriter writer = null;
    try {
      writer = new CSVWriter(new PrintWriter(values));
      writeCSVValues(writer, table, variable);
    } finally {
      if(writer != null) writer.close();
    }

    return Response.ok(values.toByteArray(), "text/csv").header("Content-Disposition",
        "attachment; filename=\"" + table.getEntityType() + "-" + variable.getName() + ".csv\"").build();
  }

  /**
   * Get the non-null values of a variable's vector in plain format.
   *
   * @return
   * @throws MagmaRuntimeException
   * @throws IOException
   */
  @GET
  @Path("/entities/_export")
  @Produces("text/plain")
  @AuthenticatedByCookie
  public Response getVectorValues(@QueryParam("type") String entityType) throws MagmaRuntimeException, IOException {
    ensureEntityType(entityType);
    ValueTable table = getValueTable(entityType);
    Variable variable = table.getVariable(name);

    ByteArrayOutputStream values = new ByteArrayOutputStream();
    Writer writer = null;
    try {
      writer = new PrintWriter(values);
      writePlainValues(writer, table);
    } finally {
      if(writer != null) writer.close();
    }

    return Response.ok(values.toByteArray(), "text/plain").header("Content-Disposition",
        "attachment; filename=\"" + table.getEntityType() + "-" + variable.getName() + ".txt\"").build();
  }

  //
  // Private methods
  //

  private Response importIdentifiers(Datasource sourceDatasource, String entityType, String select) {
    Response response = null;
    try {
      identifiersImportService.importIdentifiers(new IdentifiersMapping(name, entityType), sourceDatasource, select);
      response = Response.ok().build();
    } catch(NoSuchDatasourceException ex) {
      response = Response.status(Response.Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.NOT_FOUND, "DatasourceNotFound", ex)).build();
    } catch(NoSuchValueTableException ex) {
      response = Response.status(Response.Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.NOT_FOUND, "ValueTableNotFound", ex)).build();
    } catch(IOException ex) {
      response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          ClientErrorDtos.getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex))
          .build();
    } finally {
      Disposables.silentlyDispose(sourceDatasource);
    }

    return response;
  }

  private Response copyIdentifiers(Datasource identifiersDatasource) {
    Response response = null;
    try {
      for(ValueTable identifiersTable : identifiersDatasource.getValueTables()) {
        identifiersImportService.copyIdentifiers(identifiersTable);
      }
      response = Response.ok().build();
    } catch(NoSuchDatasourceException ex) {
      response = Response.status(Response.Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.NOT_FOUND, "DatasourceNotFound", ex)).build();
    } catch(NoSuchValueTableException ex) {
      response = Response.status(Response.Status.NOT_FOUND)
          .entity(ClientErrorDtos.getErrorMessage(Response.Status.NOT_FOUND, "ValueTableNotFound", ex)).build();
    } catch(IOException ex) {
      response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          ClientErrorDtos.getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex))
          .build();
    } finally {
      Disposables.silentlyDispose(identifiersDatasource);
    }

    return response;
  }

  private Datasource createTransientDatasource(Magma.DatasourceFactoryDto datasourceFactoryDto) {
    return createTransientDatasource(datasourceFactoryRegistry.parse(datasourceFactoryDto));
  }

  private Datasource createTransientDatasource(DatasourceFactory factory) {
    String uid = MagmaEngine.get().addTransientDatasource(factory);

    return MagmaEngine.get().getTransientDatasourceInstance(uid);
  }

  private List<IdentifiersMaps.IdentifiersMap> getUnitIdentifiers(String entityType) {
    return StreamSupport.stream(new IdentifiersMaps(getValueTable(entityType), name).spliterator(), false) //
    .filter(map -> map != null && map.hasPrivateIdentifier()) //
    .collect(Collectors.toList());
  }

  @Override
  @NotNull
  protected ValueTable getValueTable(@NotNull String entityType) {
    ValueTable table = super.getValueTable(entityType);
    if(table == null) throw new NoSuchElementException("No identifiers mapping found for entity type: " + entityType);
    return table;
  }

  private void writeCSVValues(CSVWriter writer, ValueTable table, Variable variable) {
    // header
    writer.writeNext(new String[] { table.getEntityType(), variable.getName() });
    getUnitIdentifiers(table.getEntityType()) //
        .forEach(unitId -> writer.writeNext(new String[] { unitId.getSystemIdentifier(), unitId.getPrivateIdentifier() }));
  }

  private void writePlainValues(Writer writer, ValueTable table) throws IOException {
    getUnitIdentifiers(table.getEntityType()) //
    .forEach(unitId -> {
      try {
        writer.write(unitId.getPrivateIdentifier() + "\n");
      } catch (IOException e) {
        // ignore
      }
    });
  }

}
