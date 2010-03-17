/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.shiro.SecurityUtils;

@Path("/logout")
public class LogoutResource {

  @GET
  public Boolean logout() {
    SecurityUtils.getSubject().logout();
    return true;
  }
}
