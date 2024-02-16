/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.spi.r.RMatrix;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * Helper class for R package management.
 */
@Component
public class RPackageResourceHelper {

  private static final Logger log = LoggerFactory.getLogger(RPackageResourceHelper.class);

  public List<OpalR.RPackageDto> getInstalledPackagesDtos(RServerService service) {
    return service.getInstalledPackagesDtos();
  }

  public void removePackage(RServerService service, String name) {
    checkAlphanumeric(name);
    try {
      service.removePackage(name);
    } catch (Exception e) {
      log.warn("Failed to remove the R package: {}", name, e);
    }
  }

  /**
   * Try to load a R package and install it if not found.
   *
   * @param service
   * @param name
   */
  public void ensureCRANPackage(RServerService service, String name) {
    checkAlphanumeric(name);
    try {
      service.ensureCRANPackage(name);
    } catch (Exception e) {
      log.warn("Failed to install the R package from CRAN: {}", name, e);
    }
  }

  /**
   * Install a R package from CRAN, GitHub or Bioconductor.
   *
   * @param service
   * @param name
   * @param ref
   * @param manager
   */
  public void installPackage(RServerService service, String name, String ref, String manager) {
    if (Strings.isNullOrEmpty(manager) || "cran".equalsIgnoreCase(manager))
      installCRANPackage(service, name);
    else if ("gh".equalsIgnoreCase(manager) || "github".equalsIgnoreCase(manager))
      installGitHubPackage(service, name, ref);
    else if ("bioc".equalsIgnoreCase(manager) || "bioconductor".equalsIgnoreCase(manager))
      installBioconductorPackage(service, name);
    else if ("local".equalsIgnoreCase(manager))
      installLocalPackage(service, name);
  }

  /**
   * Install a R package from CRAN.
   *
   * @param service
   * @param name
   */
  public void installCRANPackage(RServerService service, String name) {
    checkAlphanumeric(name);
    try {
      service.installCRANPackage(name);
    } catch (Exception e) {
      log.warn("Failed to install the R package from CRAN: {}", name, e);
    }
  }

  /**
   * Install a R package from GitHub.
   *
   * @param service
   * @param name
   * @param ref
   */
  public void installGitHubPackage(RServerService service, String name, String ref) {
    checkAlphanumeric(name);
    try {
      service.installGitHubPackage(name, ref);
    } catch (Exception e) {
      log.warn("Failed to install the R package from GitHub: {}", name, e);
    }
  }

  /**
   * Install a Bioconductor package.
   *
   * @param service
   * @param name
   */
  public void installBioconductorPackage(RServerService service, String name) {
    checkAlphanumeric(name);
    try {
      service.installBioconductorPackage(name);
    } catch (Exception e) {
      log.warn("Failed to install the R package from Bioconductor: {}", name, e);
    }
  }

  /**
   * Install a local R package archive file.
   *
   * @param service
   * @param path
   */
  public void installLocalPackage(RServerService service, String path) {
    try {
      service.installLocalPackage(path);
    } catch (Exception e) {
      log.warn("Failed to install the R package from path: {}", path, e);
    }
  }

  public void updateAllCRANPackages(RServerService service) {
    try {
      restartRServer(service);
      service.updateAllCRANPackages();
      restartRServer(service);
    } catch (Exception e) {
      log.warn("Failed to update all the CRAN R packages", e);
    }
  }

  //
  // Private methods
  //

  private void restartRServer(RServerService service) {
    try {
      service.stop();
      service.start();
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

    private final String clusterName;

    private final String rServerName;

    private final RMatrix<String> matrix;

    public StringsToRPackageDto(String clusterName, String rServerName, RMatrix<String> matrix) {
      this.clusterName = clusterName;
      this.matrix = matrix;
      this.rServerName = rServerName;
    }

    @Override
    public OpalR.RPackageDto apply(@Nullable String[] input) {
      OpalR.RPackageDto.Builder builder = OpalR.RPackageDto.newBuilder()
          .setCluster(clusterName)
          .setRserver(rServerName);
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
