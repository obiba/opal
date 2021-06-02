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

import org.obiba.opal.web.datashield.support.DataShieldPackageMethodHelper;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

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
  public List<OpalR.RPackageDto> getPackage(@PathParam("name") String name, @QueryParam("profile") String profile) {
    return methodPublisherImpl.getPackage(profile, name);
  }

  /**
   * Get all the methods of the package.
   *
   * @return
   * @throws org.rosuda.REngine.REXPMismatchException
   */
  @GET
  @Path("/methods")
  public DataShield.DataShieldPackageMethodsDto getPackageMethods(@PathParam("name") String name, @QueryParam("profile") String profile) {
    return methodPublisherImpl.getPackageMethods(profile, name);
  }

  /**
   * Publish all the methods of the package.
   *
   * @return the installed methods
   * @throws org.rosuda.REngine.REXPMismatchException
   */
  @PUT
  @Path("methods")
  public DataShield.DataShieldPackageMethodsDto publishPackageMethods(@PathParam("name") String name, @QueryParam("profile") String profile) {
    return methodPublisherImpl.publish(profile, name);
  }

  /**
   * Silently deletes a package and its methods.
   *
   * @return
   */
  @DELETE
  public Response deletePackage(@PathParam("name") String name, @QueryParam("profile") String profile) {
    try {
      methodPublisherImpl.deletePackage(profile, name);
    } catch (Exception e) {
      // ignored
    }
    return Response.ok().build();
  }

}
