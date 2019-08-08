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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.web.datashield.support.DataShieldPackageMethodImpl;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manage a Datashield Package.
 */
@Component
@Transactional
@Scope("request")
@Path("/datashield/package/{name}")
public class DataShieldPackageResourceRestImpl extends DataShieldRPackageResource implements DataShieldPackageResource {

  @Autowired
  private DataShieldPackageMethodImpl methodPublisherImpl;

  @PathParam("name")
  private String name;

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  @GET
  public OpalR.RPackageDto getPackage() throws REXPMismatchException {
    return methodPublisherImpl.getPackage(name);
  }

  @Override
  @GET
  @Path("/methods")
  public DataShield.DataShieldPackageMethodsDto getPackageMethods() throws REXPMismatchException {
    return methodPublisherImpl.getPackageMethods(name);
  }

  @Override
  @PUT
  @Path("methods")
  public DataShield.DataShieldPackageMethodsDto publishPackageMethods() throws REXPMismatchException {
    return methodPublisherImpl.publish(name);
  }

  @Override
  @DELETE
  public Response deletePackage() {
    try {
      methodPublisherImpl.deletePackage(name);
    } catch(REXPMismatchException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.ok().build();
  }

}
