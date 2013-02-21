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

import java.util.Iterator;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.RStringMatrix;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.web.datashield.support.NoSuchRPackageException;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Manage a Datashield Package.
 */
@Component
@Scope("request")
@Path("/datashield/package/{name}")
public class DataShieldPackageResource extends RPackageResource {

  @PathParam("name")
  private String name;

  @Autowired
  public DataShieldPackageResource(OpalRService opalRService) {
    super(opalRService);
  }

  @GET
  public OpalR.RPackageDto getPackage() throws REXPMismatchException {
    RScriptROperation rop = getInstalledPackages();
    REXP rexp = rop.getResult();
    final RStringMatrix matrix = new RStringMatrix(rexp);

    Iterator<OpalR.RPackageDto> iter = Iterables
        .filter(Iterables.transform(matrix.iterateRows(), new StringsToRPackageDto(matrix)),
            new DataShieldPackagePredicate() {
              @Override
              public boolean apply(@Nullable OpalR.RPackageDto input) {
                return input.getName().equals(name) && super.apply(input);
              }
            }).iterator();

    if(iter.hasNext()) {
      return iter.next();
    }
    throw new NoSuchRPackageException(name);
  }

  /**
   * Silently deletes a package.
   *
   * @return
   */
  @DELETE
  public Response deletePackage() {
    removePackage(name);
    return Response.ok().build();
  }

}
