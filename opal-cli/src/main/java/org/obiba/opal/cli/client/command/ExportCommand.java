/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import org.obiba.opal.cli.client.command.options.ExportCommandOptions;

/**
 * Provides ability to export Magma tables to an existing datasource or an Excel file.
 */
@CommandUsage(description = "Export Magma tables to an existing destination datasource or out to a specified Excel file.\n\nSyntax: export (--destination NAME | --out FILE) --tables NAME [NAME...]")
public class ExportCommand extends AbstractCommand<ExportCommandOptions> {

  public void execute() {
    validateOptions();
    if(options.isDestination()) {
      System.out.println("export to datasource...");
    } else if(options.isOut()) {
      throw new UnsupportedOperationException("Exporting to an Excel file is not currently supported.");
    }
  }

  private void validateOptions() {
    if(options.isDestination() && options.isOut()) {
      throw new IllegalArgumentException("The 'destination' option and the 'out' option are mutually exclusive.");
    }
  }

}