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
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.service.IdentifiersImportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.model.Identifiers.IdentifiersMappingDto;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.support.InvalidRequestException;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Transactional
@Scope("request")
@Path("/identifiers/mappings")
public class IdentifiersMappingsResource extends AbstractIdentifiersResource {

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  @Autowired
  private DatasourceFactoryRegistry datasourceFactoryRegistry;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private IdentifiersImportService identifiersImportService;

  @Override
  protected OpalFileSystemService getOpalFileSystemService() {
    return opalFileSystemService;
  }

  @Override
  protected IdentifiersTableService getIdentifiersTableService() {
    return identifiersTableService;
  }

  @GET
  public List<IdentifiersMappingDto> getIdentifiersMappings(final @QueryParam("type") List<String> entityTypes) {
    Map<String, List<String>> idsMappings = getIdentifiersMappings();
    List<IdentifiersMappingDto> dtos = Lists.newArrayList();
    for (String idsMapping : idsMappings.keySet()) {

      Iterable<String> types = Iterables.filter(idsMappings.get(idsMapping), new Predicate<String>() {
        @Override
        public boolean apply(@Nullable String input) {
          if (entityTypes == null || entityTypes.isEmpty()) return true;
          for (String type : entityTypes) {
            if (type.toLowerCase().equals(Strings.nullToEmpty(input).toLowerCase())) return true;
          }
          return false;
        }
      });

      if (Iterables.size(types) > 0)
        dtos.add(IdentifiersMappingDto.newBuilder().setName(idsMapping).addAllEntityTypes(types).build());
    }
    return dtos;
  }

  /**
   * Add the system identifiers provided in the body of the request (one per line) to the corresponding identfiers table (if this one exists).
   *
   * @param entityType
   * @param identifiers
   * @return
   */
  @POST
  @Consumes("text/plain")
  @Path("/entities/_import")
  public Response importIdentifiers(@QueryParam("type") String entityType, String identifiers) {
    if (entityType == null || !identifiersTableService.hasIdentifiersTable(entityType))
      throw new InvalidRequestException("No such identifiers table for entity type: " + entityType);
    if (Strings.isNullOrEmpty(identifiers)) throw new InvalidRequestException("A list of identifiers is expected");

    try {
      ImmutableList.Builder<String> builder = ImmutableList.builder();
      for (String id : identifiers.split(System.getProperty("line.separator"))) {
        if (!Strings.isNullOrEmpty(id)) {
          builder.add(id);
        }
      }
      ValueTable table = new StaticValueTable(new StaticDatasource("import-identifiers"), entityType, builder.build(),
          entityType);
      importIdentifiersFromTable(table);
      return Response.ok().build();
    } catch (IOException ex) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          ClientErrorDtos.getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex))
          .build();
    }
  }

  /**
   * Make a transient datasource and copy the identifiers into the corresponding identifiers tables.
   *
   * @param datasourceFactoryDto
   * @return
   */
  @POST
  @Path("/entities/_import")
  public Response importIdentifiers(@NotNull Magma.DatasourceFactoryDto datasourceFactoryDto) {
    if (!getIdentifiersTableService().hasDatasource()) throw new InvalidRequestException("No identifiers database is defined");
    if (datasourceFactoryDto == null) throw new NoSuchDatasourceException("");

    try {
      importIdentifiersFromTransientDatasource(datasourceFactoryDto);
      return Response.ok().build();
    } catch (IOException ex) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          ClientErrorDtos.getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex))
          .build();
    }
  }

  /**
   * If a datasource name is provided, it will be used to import all identifiers from this datasource or just the
   * identifiers from the provided table name in the datasource. Else identifiers of all data datasources will be imported.
   * Applies only to entity types that are handled by the identifiers datasource.
   *
   * @param datasource
   * @param tableList
   * @return
   */
  @POST
  @Path("/entities/_sync")
  public Response importIdentifiers(@QueryParam("datasource") String datasource,
                                    @SuppressWarnings("TypeMayBeWeakened") @QueryParam("table") List<String> tableList) {
    if (!getIdentifiersTableService().hasDatasource()) throw new InvalidRequestException("No identifiers database is defined");
    try {
      if (datasource != null) {
        Datasource ds = MagmaEngine.get().getDatasource(datasource);
        if (tableList != null && !tableList.isEmpty()) {
          for (String table : tableList) {
            importIdentifiersFromTable(ds.getValueTable(table));
          }
        } else {
          importIdentifiersFromDatasource(ds);
        }
      } else {
        importIdentifiersFromAllDatasources();
      }
      return Response.ok().build();
    } catch (IOException ex) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
          ClientErrorDtos.getErrorMessage(Response.Status.INTERNAL_SERVER_ERROR, "DatasourceCopierIOException", ex))
          .build();
    }
  }

  /**
   * Report the number of entities that can be synchronized: entities of any type that are in the data datasource
   * but not in the identifiers datasource.
   *
   * @param datasource
   * @param tableList
   * @return
   */
  @GET
  @Path("/entities/_sync")
  public List<Magma.TableIdentifiersSync> getIdentifiersToBeImported(
      @NotNull @QueryParam("datasource") String datasource,
      @SuppressWarnings("TypeMayBeWeakened") @QueryParam("table") List<String> tableList) {
    if (!getIdentifiersTableService().hasDatasource()) throw new InvalidRequestException("No identifiers database is defined");
    if (datasource == null) throw new NoSuchDatasourceException("");
    final Datasource ds = MagmaEngine.get().getDatasource(datasource);

    ImmutableList.Builder<Magma.TableIdentifiersSync> builder = ImmutableList.builder();

    Iterable<ValueTable> tables = tableList == null || tableList.isEmpty()
        ? ds.getValueTables()
        : Iterables.transform(tableList, new Function<String, ValueTable>() {

      @Override
      public ValueTable apply(String input) {
        return ds.getValueTable(input);
      }
    });

    for (ValueTable vt : tables) {
      List<VariableEntity> entities = Lists.newArrayList();
      if (identifiersTableService.hasIdentifiersTable(vt.getEntityType())) {
        entities = identifiersTableService.getIdentifiersTable(vt.getEntityType()).getVariableEntities();
      }
      builder.add(getTableIdentifiersSync(entities, ds, vt));
    }

    return builder.build();
  }

  /**
   * Get the non-null values of all variable's type vector in CSV format.
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
    if (!getIdentifiersTableService().hasDatasource()) throw new InvalidRequestException("No identifiers database is defined");
    ensureEntityType(entityType);
    ValueTable table = getValueTable(entityType);

    try (ByteArrayOutputStream values = new ByteArrayOutputStream();
         CSVWriter writer = new CSVWriter(new PrintWriter(values))) {
      writeCSVValues(writer, table);

      String filename = "IDs" + (table != null ? "-" + table.getEntityType() : "") + ".csv";

      writer.flush();
      values.flush();

      return Response.ok(values.toByteArray(), "text/csv")
          .header("Content-Disposition", "attachment; filename=\"" + filename + "\"").build();
    }
  }
  //
  // Private methods
  //

  private Magma.TableIdentifiersSync getTableIdentifiersSync(Collection<VariableEntity> entities, Datasource ds,
                                                             ValueTable vt) {
    int count = 0;
    List<VariableEntity> tableEntities = vt.getVariableEntities();
    Magma.TableIdentifiersSync.Builder builder = Magma.TableIdentifiersSync.newBuilder()//
        .setDatasource(ds.getName()).setTable(vt.getName()).setEntityType(vt.getEntityType())
        .setTotal(tableEntities.size());

    for (VariableEntity entity : tableEntities) {
      if (!entities.contains(entity)) {
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
    for (Datasource ds : MagmaEngine.get().getDatasources()) {
      importIdentifiersFromDatasource(ds);
    }
  }

  private void importIdentifiersFromDatasource(Datasource sourceDatasource) throws IOException {
    for (ValueTable sourceTable : sourceDatasource.getValueTables()) {
      importIdentifiersFromTable(sourceTable);
    }
  }

  private void importIdentifiersFromTable(ValueTable sourceTable) throws IOException {
    if (identifiersTableService.hasIdentifiersTable(sourceTable.getEntityType())) {
      identifiersImportService.importIdentifiers(sourceTable);
    }
  }

  private Datasource createTransientDatasource(Magma.DatasourceFactoryDto datasourceFactoryDto) {
    DatasourceFactory factory = datasourceFactoryRegistry.parse(datasourceFactoryDto);
    String uid = MagmaEngine.get().addTransientDatasource(factory);

    return MagmaEngine.get().getTransientDatasourceInstance(uid);
  }

  private Map<String, List<String>> getIdentifiersMappings() {
    Map<String, List<String>> idsMappings = Maps.newHashMap();
    if (getIdentifiersTableService().hasDatasource()) {
      for (ValueTable table : getDatasource().getValueTables()) {
        for (Variable variable : table.getVariables()) {
          if (!idsMappings.containsKey(variable.getName())) {
            idsMappings.put(variable.getName(), new ArrayList<String>());
          }
          idsMappings.get(variable.getName()).add(table.getEntityType());
        }
      }
    }
    return idsMappings;
  }

  private void writeCSVValues(CSVWriter writer, ValueTable table) {

    ImmutableList.Builder<String> builder = ImmutableList.builder();
    builder.add("ID");
    ImmutableList<Variable> variables = ImmutableList.copyOf(table.getVariables());
    builder.addAll(variables.stream().map(Variable::getName).collect(Collectors.toList()));

    // header
    ImmutableList<String> header = builder.build();
    writer.writeNext(header.toArray(new String[header.size()]));

    table.getValueSets().forEach(vs -> {
      String[] row = new String[header.size()];
      row[0] = vs.getVariableEntity().getIdentifier();
      for (int i = 0; i < variables.size(); i++) {
        row[i + 1] = vs.getValueTable().getValue(variables.get(i), vs).toString();
      }
      writer.writeNext(row);
    });
  }

}
