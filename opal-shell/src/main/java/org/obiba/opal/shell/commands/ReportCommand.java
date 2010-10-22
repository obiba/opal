/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands;

import org.obiba.opal.shell.commands.options.ReportCommandOptions;

@CommandUsage(description = "Generate a report based on the specified report template.", syntax = "Syntax: report --name TEMPLATE")
public class ReportCommand extends AbstractOpalRuntimeDependentCommand<ReportCommandOptions> {

  @Override
  public int execute() {
    // TODO implement ReportCommand.
    System.out.println("Executing report command!");
    return 0;
  }

  public String toString() {
    return "report -n " + getOptions().getName();
  }

}
