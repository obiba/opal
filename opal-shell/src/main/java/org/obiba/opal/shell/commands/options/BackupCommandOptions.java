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

@CommandLineInterface(application = "backup")
public interface BackupCommandOptions extends HelpOption {

  @Option(longName = "project", shortName = "p", description = "The project to be backed up.")
  String getProject();

  @Option(longName = "archive", shortName = "a", description = "Archive directory.")
  String getArchive();

  @Option(longName="override", shortName = "o", description = "Override existing archive (optional, default is false).")
  boolean getOverride();

  boolean isOverride();

  @Option(longName="viewsAsTables", shortName = "vt", description = "Treat views as tables, i.e. export data (optional, default is false).")
  boolean getViewsAsTables();

  boolean isViewsAsTables();
}
