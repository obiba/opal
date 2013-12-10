/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.identifiers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.lang.Closeables;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.unit.FunctionalUnitIdentifiers;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import au.com.bytecode.opencsv.CSVWriter;

@Component
@Transactional
@Scope("request")
@Path("/system/identifiers/mapping/{entityType}/unit/{unit}")
@Api(value = "/system/identifiers/mapping/{entityType}/unit/{unit}",
    description = "Operations about a specific identifiers mapping")
public class IdentifiersMappingUnitResource extends AbstractIdentifiersResource {

  private IdentifiersTableService identifiersTableService;

  @PathParam("entityType")
  private String entityType;

  @PathParam("unit")
  private String unit;

  @Autowired
  public void setIdentifiersTableService(IdentifiersTableService identifiersTableService) {
    this.identifiersTableService = identifiersTableService;
  }

  @Override
  protected IdentifiersTableService getIdentifiersTableService() {
    return identifiersTableService;
  }

  @GET
  @ApiOperation(value = "Get a specific identifiers mapping for an entity type")
  public Magma.VariableDto get() {
    ValueTable table = getValueTable();
    Variable variable = table.getVariable(unit);

    return Dtos.asDto(variable).build();
  }

  @DELETE
  @ApiOperation(value = "Delete a specific identifiers mapping for an entity type")
  public Response delete() {
    ValueTableWriter vtw = null;
    ValueTableWriter.VariableWriter vw = null;
    try {
      ValueTable table = getValueTable();
      // The variable must exist
      Variable v = table.getVariable(unit);
      vtw = table.getDatasource().createWriter(table.getName(), table.getEntityType());

      vw = vtw.writeVariables();
      vw.removeVariable(v);

      return Response.ok().build();

    } finally {
      Closeables.closeQuietly(vw);
      Closeables.closeQuietly(vtw);
    }
  }

  @GET
  @Path("/entities")
  @ApiOperation(value = "Get identifiers as entities")
  public List<Magma.VariableEntityDto> getUnitEntities() {
    return Lists.newArrayList(Iterables.transform(new FunctionalUnitIdentifiers(getValueTable(), unit).getUnitEntities(),
        Dtos.variableEntityAsDtoFunc));
  }

  @GET
  @Path("/entities/_count")
  public String getEntitiesCount() {
    return String.valueOf(Iterables.size(getUnitIdentifiers()));
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
  @ApiOperation(value = "Get identifiers mapping in CSV", produces = "text/csv")
  public Response getVectorCSVValues() throws MagmaRuntimeException, IOException {
    ValueTable table = getValueTable();
    Variable variable = table.getVariable(unit);

    ByteArrayOutputStream values = new ByteArrayOutputStream();
    CSVWriter writer = null;
    try {
      writer = new CSVWriter(new PrintWriter(values));
      writeCSVValues(writer, table, variable);
    } finally {
      if(writer != null) writer.close();
    }

    return Response.ok(values.toByteArray(), "text/csv").header("Content-Disposition",
        "attachment; filename=\"" + table.getName() + "-" + variable.getName() + ".csv\"").build();
  }

  /**
   * Get the non-null values of a variable's vector in plain format.
   *
   * @return
   * @throws MagmaRuntimeException
   * @throws IOException
   */
  @GET
  @Path("/_export")
  @Produces("text/plain")
  @AuthenticatedByCookie
  @ApiOperation(value = "Get identifiers in plain text", produces = "text/plain")
  public Response getVectorValues() throws MagmaRuntimeException, IOException {
    ValueTable table = getValueTable();
    Variable variable = table.getVariable(unit);

    ByteArrayOutputStream values = new ByteArrayOutputStream();
    Writer writer = null;
    try {
      writer = new PrintWriter(values);
      writePlainValues(writer);
    } finally {
      if(writer != null) writer.close();
    }

    return Response.ok(values.toByteArray(), "text/plain").header("Content-Disposition",
        "attachment; filename=\"" + table.getName() + "-" + variable.getName() + ".txt\"").build();
  }

  //
  // Private methods
  //

  private Iterable<FunctionalUnitIdentifiers.UnitIdentifier> getUnitIdentifiers() {
    return Iterables.filter(new FunctionalUnitIdentifiers(getValueTable(),unit), new Predicate<FunctionalUnitIdentifiers.UnitIdentifier>() {
      @Override
      public boolean apply(@Nullable FunctionalUnitIdentifiers.UnitIdentifier input) {
        return input.hasUnitIdentifier();
      }
    });
  }

  private ValueTable getValueTable() {
    ValueTable table = getValueTable(entityType);
    if(table == null) throw new NoSuchElementException("No identifiers mapping found for entity type: " + entityType);
    return table;
  }

  private void writeCSVValues(CSVWriter writer, ValueTable table, Variable variable) {
    // header
    writer.writeNext(new String[] { table.getEntityType(), variable.getName() });
    for (FunctionalUnitIdentifiers.UnitIdentifier unitId : getUnitIdentifiers()) {
      writer.writeNext(new String[] { unitId.getOpalIdentifier(), unitId.getUnitIdentifier() });
    }
  }

  private void writePlainValues(Writer writer) throws IOException {
    for (FunctionalUnitIdentifiers.UnitIdentifier unitId : getUnitIdentifiers()) {
      writer.write(unitId.getUnitIdentifier() + "\n");
    }
  }

}
