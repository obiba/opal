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

import org.json.JSONObject;
import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

import java.util.List;

@CommandLineInterface(application = "analyse")
public interface AnalyseCommandOptions extends HelpOption {

  @Option(shortName = "p", description = "The source project in which table variables are to be analysed.")
  String getProject();

  @Option(shortName = "a", description = "List of analyse options providing table or variable names in addition to analysis name, plugin, template and routine parameters.")
  List<AnalyseOptions> getAnalyses();

  interface AnalyseOptions {
    String getTable();
    String getVariables();
    String getName();
    String getPlugin();
    String getTemplate();
    JSONObject getParams();
  }
}
