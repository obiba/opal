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

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.shell.commands.options.ShowCommandOptions;

/**
 * Displays on the standard output the fully qualified name of elements that are available in Opal.
 */
@CommandUsage(description = "Displays the fully qualified name of each data element currently available in Opal.", syntax = "Syntax: show [--datasources] [--tables]")
public class ShowCommand extends AbstractOpalRuntimeDependentCommand<ShowCommandOptions> {

  public void execute() {

    boolean displayDatasources = options.getDatasources();
    boolean displayTables = options.getTables();

    // If no options are specified, default behavior is to list both datasources and tables.
    if(!displayDatasources && !displayTables) {
      displayDatasources = true;
      displayTables = true;
    }

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {

      if(displayDatasources) {
        getShell().printf("%s\n", datasource.getName());
      }

      Set<ValueTable> tables = datasource.getValueTables();
      if(displayTables) {
        for(ValueTable valueTable : tables) {
          getShell().printf("%s.%s\n", datasource.getName(), valueTable.getName());
        }
      }
    }

  }
}
