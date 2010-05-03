/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd;

import java.util.List;

import org.obiba.opal.shell.commands.options.HelpOption;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 *
 */
@CommandLineInterface(application = "grant")
public interface GrantCommandOptions extends HelpOption {

  @Option(shortName = "u")
  String getUnit();

  @Option(shortName = "p", defaultValue = "read")
  String getPerm();

  @Unparsed(name = "tables")
  List<String> getTables();
}
