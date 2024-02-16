/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.obiba.datashield.r.expr.ParseException;
import org.obiba.opal.web.r.RSessionResource;

public interface DataShieldSessionResource extends RSessionResource {

  @POST
  @Path("/aggregate")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response aggregateBinary(@QueryParam("async") @DefaultValue("false") boolean async, String body) throws ParseException;

  @POST
  @Path("/aggregate")
  @Produces(MediaType.APPLICATION_JSON)
  Response aggregateJSON(@QueryParam("async") @DefaultValue("false") boolean async, String body) throws ParseException;

}
