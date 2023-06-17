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
import org.obiba.opal.shell.commands.options.RPackageCommandOptions;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.NoSuchElementException;

@CommandUsage(description = "Install R package of a R cluster.",
    syntax = "Syntax: r-package --cluster CLUSTER")
public class RPackageCommand extends AbstractOpalRuntimeDependentCommand<RPackageCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(RPackageCommand.class);

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
      getShell().progress(String.format("[%s] Installing R package: %s", clusterName, getOptions().getName()), 1, 2, 10);
      rPackageHelper.installPackage(cluster, getOptions().getName(), getOptions().getRef(), getOptions().getManager());
      getShell().progress(String.format("[%s] R package installed: %s", clusterName, getOptions().getName()), 2, 2, 100);
      return 0;
    } catch (Exception e) {
      getShell().printf("Cannot install R package: '/service/r/cluster/%s/package/%s' %s", clusterName, getOptions().getName(), e.getMessage());
      return 1;
    }
  }

  @Override
  public String toString() {
    return "r-package -n \"" + getOptions().getRCluster() + "\"";
  }

}
