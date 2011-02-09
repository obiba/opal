/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class TablesResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(TablesResource.class);

  private final Datasource datasource;

  public TablesResource(Datasource datasource) {
    if(datasource == null) throw new IllegalArgumentException("datasource cannot be null");
    this.datasource = datasource;
  }

  @GET
  public List<Magma.TableDto> getTables() {
    final List<Magma.TableDto> tables = Lists.newArrayList();
    UriBuilder tableLink = UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getTable");
    for(ValueTable valueTable : datasource.getValueTables()) {
      tables.add(Dtos.asDto(valueTable, tableLink).build());
    }
    sortByName(tables);

    return tables;
  }

  @POST
  public Response createTable(TableDto table) {

    try {
      // ClientErrorDto errorDto;

      // @TODO Verify that the datasource allows table creation (Magma does not offer this yet)
      // if(datasource.isReadOnly()) {
      // errorMessage = return Response.status(Status.BAD_REQUEST).entity(getErrorMessage(Status.BAD_REQUEST,
      // "CannotCreateTable")).build();
      // } else

      if(datasource.hasValueTable(table.getName())) {
        return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "TableAlreadyExists").build()).build();
      } else {
        writeVariablesToTable(table);
        return Response.created(UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getTable").build(datasource.getName(), table.getName())).build();
      }
    } catch(Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ClientErrorDtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, e.getMessage()).build()).build();
    }
  }

  private void writeVariablesToTable(TableDto table) {
    VariableWriter vw = null;
    try {
      vw = datasource.createWriter(table.getName(), table.getEntityType()).writeVariables();

      for(VariableDto dto : table.getVariablesList()) {
        vw.writeVariable(Dtos.fromDto(dto));
      }
    } finally {
      StreamUtil.silentSafeClose(vw);
    }
  }

  private void sortByName(List<Magma.TableDto> tables) {
    // sort alphabetically
    Collections.sort(tables, new Comparator<Magma.TableDto>() {

      @Override
      public int compare(TableDto t1, TableDto t2) {
        return t1.getName().compareTo(t2.getName());
      }

    });
  }
}
