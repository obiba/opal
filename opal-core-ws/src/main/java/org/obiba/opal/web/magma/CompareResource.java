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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.springframework.stereotype.Component;

@Component
public class CompareResource {

  ValueTable compared;

  public CompareResource() {
  }

  public CompareResource(ValueTable compared) {
    this.compared = compared;
  }

  @GET
  @Path("/{with}")
  public Response compare(@PathParam("with") String with) {
    String datasourceName = MagmaEngineTableResolver.valueOf(with).getDatasourceName();
    String tableName = MagmaEngineTableResolver.valueOf(with).getTableName();

    System.out.println("datasourceName " + datasourceName);
    System.out.println("tableName " + tableName);

    ValueTable withTable = MagmaEngine.get().getDatasource(datasourceName).getValueTable(tableName);

    // @TODO Missing implementation

    return Response.ok().build();
  }

}
