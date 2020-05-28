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

import org.obiba.opal.web.datashield.support.DataShieldPackageMethodHelper;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Manage a Datashield Package.
 */
@Component
@Transactional
@Scope("request")
@Path("/datashield/package/{name}")
public class DataShieldPackageResource {

  @Autowired
  private DataShieldPackageMethodHelper methodPublisherImpl;

  @GET
  public OpalR.RPackageDto getPackage(@PathParam("name") String name) throws REXPMismatchException {
    return methodPublisherImpl.getPackage(name);
  }

  /**
   * Get all the methods of the package.
   *
   * @return
   * @throws org.rosuda.REngine.REXPMismatchException
   */
  @GET
  @Path("/methods")
  public DataShield.DataShieldPackageMethodsDto getPackageMethods(@PathParam("name") String name) throws REXPMismatchException {
    return methodPublisherImpl.getPackageMethods(name);
  }

  /**
   * Publish all the methods of the package.
   *
   * @return the installed methods
   * @throws org.rosuda.REngine.REXPMismatchException
   */
  @PUT
  @Path("methods")
  public DataShield.DataShieldPackageMethodsDto publishPackageMethods(@PathParam("name") String name) throws REXPMismatchException {
    return methodPublisherImpl.publish(name);
  }

  /**
   * Silently deletes a package and its methods.
   *
   * @return
   */
  @DELETE
  public Response deletePackage(@PathParam("name") String name) {
    try {
      methodPublisherImpl.deletePackage(name);
    } catch (Exception e) {
      // ignored
    }
    return Response.ok().build();
  }

}
