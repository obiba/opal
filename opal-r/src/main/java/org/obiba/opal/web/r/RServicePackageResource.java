/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import org.obiba.opal.spi.r.RScriptROperation;
import org.obiba.opal.spi.r.RStringMatrix;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Scope("request")
@Path("/service/r/package/{name}")
public class RServicePackageResource extends RPackageResource {

  @GET
  public OpalR.RPackageDto getPackage(@PathParam("name") String name) throws REXPMismatchException {
    RScriptROperation rop = getInstalledPackages();
    REXP rexp = rop.getResult();
    RStringMatrix matrix = new RStringMatrix(rexp);
    return StreamSupport.stream(matrix.iterateRows().spliterator(), false)
        .map(new StringsToRPackageDto(matrix))
        .filter(dto -> dto.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new NoSuchRPackageException(name));
  }

  @DELETE
  public Response deletePackage(@PathParam("name") String name) {
    try {
      removePackage(name);
    } catch (Exception e) {
      // ignore
    }
    return Response.noContent().build();
  }
}
