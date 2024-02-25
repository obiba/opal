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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
