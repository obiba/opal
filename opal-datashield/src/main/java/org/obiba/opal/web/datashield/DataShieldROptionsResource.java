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

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.model.DataShield;

public interface DataShieldROptionsResource {

  @GET
  List<DataShield.DataShieldROptionDto> getDataShieldROptions(@QueryParam("profile") String profile);

  @DELETE
  Response deleteDataShieldROptions(@QueryParam("name") List<String> names, @QueryParam("profile") String profile);

}
