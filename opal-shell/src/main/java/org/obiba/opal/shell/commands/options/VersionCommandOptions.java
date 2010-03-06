/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;

/**
 * This interface declares the options that may be used with the version command.
 * 
 * Note that there <bold>no</bold> options for this command.
 * 
 * @author cag-dspathis
 * 
 */
@CommandLineInterface(application = "version")
public interface VersionCommandOptions extends HelpOption {
  // no options
}
