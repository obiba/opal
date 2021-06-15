/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import org.obiba.opal.core.runtime.App;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.RServerException;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;

import java.util.List;
import java.util.Map;

public interface RServerService {

  String AGGREGATE_METHODS = "AggregateMethods";

  String ASSIGN_METHODS = "AssignMethods";

  String OPTIONS = "Options";

  String[] defaultFields = new String[]{"Title", "Description", "Author", "Maintainer",
      "Date/Publication", AGGREGATE_METHODS, ASSIGN_METHODS, OPTIONS};

  String getName();

  void start();

  void stop();

  boolean isRunning();

  RServerState getState() throws RServerException;

  RServerSession newRServerSession(String user) throws RServerException;

  /**
   * Shortcut for one shot R code execution.
   *
   * @param rop
   */
  void execute(ROperation rop) throws RServerException;

  /**
   * Get the inner App from which the R server was built.
   *
   * @return
   */
  App getApp();

  /**
   * Check whether the App object is associated to the R server service, order to avoid name conflicts
   * (same app name but from different server or with different type).
   *
   * @param app
   * @return
   */
  boolean isFor(App app);

  /**
   * Get the list of installed R packages.
   *
   * @return
   */
  List<OpalR.RPackageDto> getInstalledPackagesDtos();

  /**
   * Get a single R package description. The same R package can be installed at different locations, different versions.
   *
   * @param name
   * @return
   */
  List<OpalR.RPackageDto> getInstalledPackageDto(String name);

  /**
   * Remove package with provided name.
   *
   * @param name
   */
  void removePackage(String name) throws RServerException;

  /**
   * Extract DataSHIELD settings from installed packages.
   */
  Map<String, List<Opal.EntryDto>> getDataShieldPackagesProperties();

  /**
   * Install a R package from CRAN if not already installed.
   *
   * @param name
   */
  void ensureCRANPackage(String name) throws RServerException;

  /**
   * Install a R package from CRAN.
   *
   * @param name
   */
  void installCRANPackage(String name) throws RServerException;

  /**
   * Install a R package from GitHub.
   *
   * @param name
   * @param ref
   */
  void installGitHubPackage(String name, String ref) throws RServerException;

  /**
   * Install a Bioconductor package.
   *
   * @param name
   */
  void installBioconductorPackage(String name) throws RServerException;

  /**
   * Install a local R package archive file.
   *
   * @param path
   * @throws RServerException
   */
  void installLocalPackage(String path) throws RServerException;

  /**
   * Try to update all CRAN packages.
   */
  void updateAllCRANPackages() throws RServerException;

  /**
   * Get the tail of the log file.
   *
   * @param nbLines
   * @return
   */
  String[] getLog(Integer nbLines);
}
