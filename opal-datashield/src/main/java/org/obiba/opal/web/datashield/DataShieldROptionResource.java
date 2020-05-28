/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.model.DataShield;

public interface DataShieldROptionResource {

  @DELETE
  Response deleteDataShieldROption(@QueryParam("name") String name);

  @POST
  @PUT
  Response addOrUpdateDataShieldROption(DataShield.DataShieldROptionDto dto);

  @GET
  Response getDataShieldROption(@QueryParam("name") String name);
}
