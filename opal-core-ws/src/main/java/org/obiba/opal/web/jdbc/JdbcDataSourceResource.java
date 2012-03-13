/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.core.runtime.jdbc.JdbcDataSource;
import org.obiba.opal.core.runtime.jdbc.JdbcDataSourceRegistry;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Opal.JdbcDataSourceDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcDataSourceResource {

  private final JdbcDataSourceRegistry jdbcDataSourceRegistry;

  private final JdbcDataSource jdbcDataSource;

  JdbcDataSourceResource(JdbcDataSourceRegistry jdbcDataSourceRegistry, JdbcDataSource jdbcDataSource) {
    this.jdbcDataSourceRegistry = jdbcDataSourceRegistry;
    this.jdbcDataSource = jdbcDataSource;
  }

  @GET
  public JdbcDataSourceDto get() {
    return Dtos.JdbcDataSourceDtos.asDto.apply(jdbcDataSource);
  }

  @DELETE
  public Response delete() {
    if(jdbcDataSource.isEditable() == false) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    jdbcDataSourceRegistry.remove(jdbcDataSource);
    return Response.ok().build();
  }

  @PUT
  public Response update(JdbcDataSourceDto dto) {
    if(jdbcDataSource.isEditable() == false) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    if(dto.getName().equals(jdbcDataSource.getName()) == false) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDto.newBuilder().setCode(Status.BAD_REQUEST.getStatusCode()).setStatus("InvalidName").build()).build();
    }
    jdbcDataSourceRegistry.update(Dtos.JdbcDataSourceDtos.fromDto.apply(dto));
    return Response.ok().build();
  }

  @POST
  @Path("/connections")
  public Response testConnection() {
    ClientErrorDto error = ClientErrorDtos.getErrorMessage(Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", "").build();
    try {
      JdbcTemplate t = new JdbcTemplate(jdbcDataSourceRegistry.getDataSource(jdbcDataSource.getName(), null));
      Boolean result = t.execute(new ConnectionCallback<Boolean>() {

        @Override
        public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
          return con.isValid(1);
        }
      });
      if(result != null && result == true) {
        return Response.ok().build();
      }
    } catch(DataAccessException dae) {
      error = ClientErrorDtos.getErrorMessage(Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", dae).build();
    } catch(RuntimeException e) {
      error = ClientErrorDtos.getErrorMessage(Status.SERVICE_UNAVAILABLE, "DatabaseConnectionFailed", e).build();
    }
    return Response.status(Status.SERVICE_UNAVAILABLE).entity(error).build();
  }

}
