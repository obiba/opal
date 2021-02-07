/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.spi.r.RMatrix;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Helper class for R package management.
 */
@Component
public class RPackageResourceHelper {

  private static final Logger log = LoggerFactory.getLogger(RPackageResourceHelper.class);

  @Autowired
  protected RServerManagerService rServerManagerService;

  public List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    return rServerManagerService.getDefaultRServer().getInstalledPackagesDtos();
  }

  public void removePackage(String name) {
    checkAlphanumeric(name);
    try {
      rServerManagerService.getDefaultRServer().removePackage(name);
    } catch (Exception e) {
      log.warn("Failed to remove the R package: {}", name, e);
    }
  }

  /**
   * Try to load a R package and install it if not found.
   *
   * @param name
   */
  public void ensureCRANPackage(String name) {
    checkAlphanumeric(name);
    try {
      rServerManagerService.getDefaultRServer().ensureCRANPackage(name);
    } catch (Exception e) {
      log.warn("Failed to install the R package from CRAN: {}", name, e);
    }
  }

  /**
   * Install a R package from CRAN.
   *
   * @param name
   */
  public void installCRANPackage(String name) {
    checkAlphanumeric(name);
    try {
      rServerManagerService.getDefaultRServer().installCRANPackage(name);
    } catch (Exception e) {
      log.warn("Failed to install the R package from CRAN: {}", name, e);
    }
  }

  /**
   * Install a R package from GitHub.
   *
   * @param name
   * @param ref
   */
  public void installGitHubPackage(String name, String ref) {
    checkAlphanumeric(name);
    try {
      rServerManagerService.getDefaultRServer().installGitHubPackage(name, ref);
    } catch (Exception e) {
      log.warn("Failed to install the R package from GitHub: {}", name, e);
    }
  }

  /**
   * Install a Bioconductor package.
   *
   * @param name
   */
  public void installBioconductorPackage(String name) {
    checkAlphanumeric(name);
    try {
      rServerManagerService.getDefaultRServer().installBioconductorPackage(name);
    } catch (Exception e) {
      log.warn("Failed to install the R package from Bioconductor: {}", name, e);
    }
  }

  public void updateAllCRANPackages() {
    try {
      restartRServer();
      rServerManagerService.getDefaultRServer().updateAllCRANPackages();
      restartRServer();
    } catch (Exception e) {
      log.warn("Failed to update all the CRAN R packages", e);
    }
  }

  //
  // Private methods
  //

  private void restartRServer() {
    try {
      rServerManagerService.getDefaultRServer().stop();
      rServerManagerService.getDefaultRServer().start();
    } catch (Exception ex) {
      log.error("Error while restarting R server after package install: {}", ex.getMessage(), ex);
    }
  }

  /**
   * Simple security check: the provided name (package name or Github user/organization name) must be alphanumeric.
   *
   * @param name
   */
  private void checkAlphanumeric(String name) {
    if (!name.matches("[a-zA-Z0-9/\\-\\\\.]+"))
      throw new IllegalArgumentException("Not a valid name: " + name);
  }

  public static class StringsToRPackageDto implements Function<String[], OpalR.RPackageDto> {

    private int current = 0;

    private final RMatrix<String> matrix;

    public StringsToRPackageDto(RMatrix<String> matrix) {
      this.matrix = matrix;
    }

    @Override
    public OpalR.RPackageDto apply(@Nullable String[] input) {
      OpalR.RPackageDto.Builder builder = OpalR.RPackageDto.newBuilder();
      if (input != null) {
        for (int i = 0; i < input.length; i++) {
          if (!Strings.isNullOrEmpty(input[i]) && !"NA".equals(input[i])) {
            Opal.EntryDto.Builder entry = Opal.EntryDto.newBuilder();
            entry.setKey(matrix.getColumnNames()[i]);
            entry.setValue(input[i]);
            builder.addDescription(entry);
          }
        }
      }
      builder.setName(matrix.getRowNames()[current++]);
      return builder.build();
    }
  }

}
