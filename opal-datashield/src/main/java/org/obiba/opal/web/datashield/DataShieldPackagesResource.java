/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.RStringMatrix;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Manages Datashield packages.
 */
@Component
@Path("/datashield/packages")
public class DataShieldPackagesResource extends RPackageResource {
  private static final Logger log = LoggerFactory.getLogger(DataShieldPackagesResource.class);

  @Autowired
  public DataShieldPackagesResource(OpalRService opalRService) {
    super(opalRService);
  }

  @GET
  public List<OpalR.RPackageDto> getPackages() throws REXPMismatchException {
    RScriptROperation rop = getInstalledPackages();
    REXP rexp = rop.getResult();
    RStringMatrix matrix = new RStringMatrix(rexp);
    Iterable<OpalR.RPackageDto> dtos = Iterables
        .filter(Iterables.transform(matrix.iterateRows(), new StringsToRPackageDto(matrix)),
            new DataShieldPackagePredicate());

    return Lists.newArrayList(dtos);
  }

  @POST
  public Response installPackage(@Context UriInfo uriInfo, @QueryParam("name") String name,
      @QueryParam("ref") String ref) {
    installDatashieldPackage(name, ref);
    UriBuilder ub = uriInfo.getBaseUriBuilder().path(DataShieldPackageResource.class);
    return Response.created(ub.build(name)).build();
  }

}
