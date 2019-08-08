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

import org.obiba.opal.spi.r.RScriptROperation;
import org.obiba.opal.spi.r.RStringMatrix;
import org.obiba.opal.web.datashield.support.DataShieldPackageMethodHelper;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Manages Datashield packages.
 */
@Component
@Transactional
@Path("/datashield/packages")
public class DataShieldPackagesResource {

  @Autowired
  private DataShieldPackageMethodHelper dsPackageMethodeHelper;

  @GET
  public List<OpalR.RPackageDto> getPackages() throws REXPMismatchException {
    RScriptROperation rop = dsPackageMethodeHelper.getInstalledPackages();
    REXP rexp = rop.getResult();
    RStringMatrix matrix = new RStringMatrix(rexp);

    return StreamSupport.stream(matrix.iterateRows().spliterator(), false)
        .map(new RPackageResourceHelper.StringsToRPackageDto(matrix))
        .filter(dsPackageMethodeHelper::isDataShieldPackage)
        .collect(Collectors.toList());
  }

  @POST
  public Response installPackage(@Context UriInfo uriInfo, @QueryParam("name") String name,
                                 @QueryParam("ref") String ref) throws REXPMismatchException {
    dsPackageMethodeHelper.installDatashieldPackage(name, ref);

    // install or re-install all known datashield package methods
    List<OpalR.RPackageDto> pkgs = getPackages();
    for (OpalR.RPackageDto pkg : pkgs) {
      dsPackageMethodeHelper.publish(pkg.getName());
    }
    // make sure last is the one we install (check it is a "datashield" package first)
    if (getPackages().stream().anyMatch(p -> p.getName().equals(name)))
      dsPackageMethodeHelper.publish(name);

    UriBuilder ub = uriInfo.getBaseUriBuilder().path(DataShieldPackageResource.class);
    return Response.created(ub.build(name)).build();
  }

  @DELETE
  public Response deletePackages() {
    try {
      for (OpalR.RPackageDto pkg : getPackages()) {
        dsPackageMethodeHelper.deletePackage(pkg);
      }
    } catch (Exception e) {
      // ignored
    }
    return Response.ok().build();
  }

}
