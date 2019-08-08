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
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.NoSuchRPackageException;
import org.obiba.opal.web.r.RPackageResource;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.stream.StreamSupport;

/**
 * Base class for R package management.
 */
public abstract class DataShieldRPackageResource extends RPackageResource {

  private static final Logger log = LoggerFactory.getLogger(DataShieldRPackageResource.class);

  RScriptROperation installDatashieldPackage(String name, String ref) {
    return installPackage(name, ref, "datashield");
  }

  protected OpalR.RPackageDto getDatashieldPackage(final String name) throws REXPMismatchException {
    RScriptROperation rop = getInstalledPackages();
    REXP rexp = rop.getResult();
    RStringMatrix matrix = new RStringMatrix(rexp);

    return StreamSupport.stream(matrix.iterateRows().spliterator(), false)
        .map(new StringsToRPackageDto(matrix))
        .filter(dto -> dto != null && dto.getName().equals(name) && isDataShieldPackage(dto))
        .findFirst().orElseThrow(() -> new NoSuchRPackageException(name));
  }

  protected boolean isDataShieldPackage(@Nullable OpalR.RPackageDto input) {
    if (input == null) return false;
    for (Opal.EntryDto entry : input.getDescriptionList()) {
      String key = entry.getKey();
      if (AGGREGATE_METHODS.equals(key) || ASSIGN_METHODS.equals(key) || OPTIONS.equals(key)) {
        return !"NA".equals(entry.getValue());
      }
    }
    return false;
  }

}
