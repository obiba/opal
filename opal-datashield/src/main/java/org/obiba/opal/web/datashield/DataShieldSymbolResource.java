/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.datashield;

import javax.ws.rs.core.Response;

import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.web.r.RSymbolResource;

public class DataShieldSymbolResource extends RSymbolResource {

  public DataShieldSymbolResource(OpalRSession rSession, String name) {
    super(rSession, name);
  }

  /**
   * Overridden to prevent accessing individual-level data
   */
  public Response getSymbol() {
    return Response.noContent().build();
  }
}
