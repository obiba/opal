/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the R command.
 */
@CommandLineInterface(application = "install-R-package")
public interface RPackageCommandOptions extends HelpOption {
  @Option(shortName = "r", description = "The R cluster name.")
  String getRCluster();

  @Option(shortName = "p", description = "The R package name.")
  String getName();
  @Option(shortName = "m", description = "The R manager name.")
  String getManager();
  @Option(shortName = "e", description = "The R code reference.")
  String getRef();
}
