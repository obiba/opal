/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXPMismatchException;

public interface DataShieldPackageResource {
  void setName(String name);

  @GET
  OpalR.RPackageDto getPackage() throws REXPMismatchException;

  /**
   * Get all the methods of the package.
   *
   * @return
   * @throws org.rosuda.REngine.REXPMismatchException
   */
  @GET
  @Path("/methods")
  DataShield.DataShieldPackageMethodsDto getPackageMethods() throws REXPMismatchException;

  /**
   * Publish all the methods of the package.
   *
   * @return the installed methods
   * @throws org.rosuda.REngine.REXPMismatchException
   */
  @PUT
  @Path("methods")
  DataShield.DataShieldPackageMethodsDto publishPackageMethods() throws REXPMismatchException;

  /**
   * Silently deletes a package and its methods.
   *
   * @return
   */
  @DELETE
  Response deletePackage();
}
