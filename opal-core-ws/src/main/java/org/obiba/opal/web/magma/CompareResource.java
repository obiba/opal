/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

public interface CompareResource {

  void setComparedDatasource(Datasource comparedDatasource);

  void setComparedTable(ValueTable comparedTable);

  @GET
  @Path("/{with}")
  Response compare(@PathParam("with") String with, @QueryParam("merge") @DefaultValue("false") boolean merge);
}
