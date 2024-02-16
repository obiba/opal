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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.DataShield;

public interface DataShieldROptionResource {

  @DELETE
  Response deleteDataShieldROption(@QueryParam("name") String name, @QueryParam("profile") String profile);

  @POST
  @PUT
  Response addOrUpdateDataShieldROption(@QueryParam("profile") String profile, DataShield.DataShieldROptionDto dto);

  @GET
  Response getDataShieldROption(@QueryParam("name") String name, @QueryParam("profile") String profile);
}
