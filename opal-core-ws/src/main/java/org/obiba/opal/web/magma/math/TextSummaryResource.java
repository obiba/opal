/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.math;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public interface TextSummaryResource extends SummaryResource {

  @GET
  @POST
  Response get(@QueryParam("offset") Integer offset, //
      @QueryParam("limit") Integer limit, //
      @QueryParam("fullIfCached") @DefaultValue("false") boolean fullIfCached, //
      @QueryParam("resetCache") @DefaultValue("false") boolean resetCache,
      @QueryParam("count") @DefaultValue("20") int maxResults);

}
