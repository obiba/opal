/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.mongodb.client.MongoDatabase;
import org.obiba.magma.SocketFactoryProvider;
import org.obiba.magma.datasource.mongodb.MongoDBDatasourceFactory;
import org.obiba.magma.datasource.mongodb.MongoDBFactory;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.MultipleIdentifiersDatabaseException;
import org.obiba.opal.core.service.database.NoSuchDatabaseException;
import org.obiba.opal.web.database.Dtos;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.obiba.opal.web.model.Database.DatabaseDto;

/**
 * Class is not transactional because of delete method
 */
@Component
@Scope("request")
@Path("/system/database/{name}")
public class DatabaseResource {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private SocketFactoryProvider socketFactoryProvider;

  @PathParam("name")
  private String name;

  @GET
  public DatabaseDto get() {
    Database database = getDatabase();
    return Dtos.asDto(database, databaseRegistry.hasDatasource(database));
  }

  @DELETE
  public Response delete() {
    Database database = getDatabase();
    databaseRegistry.delete(database);
    return Response.ok().build();
  }

  @PUT
  public Response update(DatabaseDto dto) throws MultipleIdentifiersDatabaseException {
    Database database = Dtos.fromDto(dto);
    try {
      // Allow edition of all fields except database name
      database.setName(databaseRegistry.getDatabase(name).getName());
    } catch(NoSuchDatabaseException ignored) {
      // do nothing if it's a new database
    }
    databaseRegistry.update(database);
    return Response.ok().build();
  }

  @POST
  @Path("/connections")
  @Transactional(readOnly = true)
  public Response testConnection() {
    Database database = getDatabase();
    if(database.hasSqlSettings()) {
      return testSqlConnection();
    }
    if(database.hasMongoDbSettings()) {
      return testMongoConnection(database.getMongoDbSettings());
    }
    throw new RuntimeException("Connection test not yet implemented for database " + database.getClass());
  }

  @GET
  @Path("/hasEntities")
  public Response getHasEntities() {
    Database database = databaseRegistry.getDatabase(name);
    return Response.ok().entity(String.valueOf(databaseRegistry.hasEntities(database))).build();
  }

  private Database getDatabase() {
    return databaseRegistry.getDatabase(name);
  }

  private Response testSqlConnection() {
    try {
      JdbcOperations jdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource(name, null));
      return jdbcTemplate.execute(new ConnectionCallback<Response>() {
        @Override
        public Response doInConnection(Connection con) throws SQLException, DataAccessException {
          return con.isValid(1) ? Response.ok().build() : databaseConnectionFailed();
        }
      });
    } catch(RuntimeException e) {
      return databaseConnectionFailed();
    }
  }

  private Response testMongoConnection(MongoDbSettings mongoDbSettings) {
    try {
      MongoDBDatasourceFactory datasourceFactory = mongoDbSettings.createMongoDBDatasourceFactory("_test", socketFactoryProvider);
      List<String> dbs = StreamSupport.stream(datasourceFactory.getMongoDBFactory().getMongoClient().listDatabaseNames().spliterator(), false)
          .collect(Collectors.toList());
      if(dbs.contains(datasourceFactory.getMongoDbDatabaseName())) {
        return Response.ok().build();
      }
      return datasourceFactory.getMongoDBFactory().execute(db -> {
        db.getCollection("_coll_test").drop();
        return Response.ok().build();
      });
    } catch(RuntimeException e) {
      return databaseConnectionFailed();
    }
  }

  private Response databaseConnectionFailed() {
    return Response.status(SERVICE_UNAVAILABLE)
        .entity(ClientErrorDtos.getErrorMessage(SERVICE_UNAVAILABLE, "DatabaseConnectionFailed").build()).build();
  }

}
