/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

import org.obiba.opal.r.cluster.RServerCluster;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.shell.commands.options.RPackagesCommandOptions;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.NoSuchElementException;

@CommandUsage(description = "Update all R packages of a R cluster.",
    syntax = "Syntax: r-packages --cluster CLUSTER")
public class RPackagesCommand extends AbstractOpalRuntimeDependentCommand<RPackagesCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(RPackagesCommand.class);

  @Autowired
  protected RServerManagerService rServerManagerService;

  @Autowired
  protected RPackageResourceHelper rPackageHelper;

  @Override
  public int execute() {
    // Get the R cluster name
    String clusterName = getOptions().getRCluster();
    getShell().progress(String.format("[%s] Preparing R cluster", clusterName), 0, 2, 0);
    RServerCluster cluster;
    try {
      cluster = rServerManagerService.getRServerCluster(clusterName);
    } catch (NoSuchElementException e) {
      getShell().printf("R servers cluster '%s' does not exist.\n", clusterName);
      return 1;
    }

    try {
      getShell().progress(String.format("[%s] Updating all R packages", clusterName), 1, 2, 10);
      rPackageHelper.updateAllCRANPackages(cluster);
      getShell().progress(String.format("[%s] All R packages updated", clusterName), 2, 2, 100);
      return 0;
    } catch (Exception e) {
      getShell().printf("Cannot update all R packages: '/service/r/cluster/%s'", clusterName);
      return 1;
    }
  }

  @Override
  public String toString() {
    return "r-packages -n \"" + getOptions().getRCluster() + "\"";
  }

}
