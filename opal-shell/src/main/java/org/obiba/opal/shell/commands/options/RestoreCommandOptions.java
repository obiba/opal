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

@CommandLineInterface(application = "restore")
public interface RestoreCommandOptions extends HelpOption {

  @Option(longName = "project", shortName = "p", description = "The project to be restored.")
  String getProject();

  @Option(longName = "archive", shortName = "a", description = "Archive directory or zip file.")
  String getArchive();

  @Option(longName="password", shortName = "pwd", description = "Password of the zip file (optional).")
  String getPassword();

  boolean isPassword();

  @Option(longName="override", shortName = "o", description = "Override existing item to restore (optional, default is false).")
  boolean getOverride();

  boolean isOverride();
}
