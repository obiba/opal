/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.commands.options;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the split command.
 * <p/>
 * Note that the <code>getFiles</code> method is used to access unparsed file arguments at the end of the command line.
 */
@CommandLineInterface(application = "split")
public interface SplitCommandOptions extends HelpOption {

  @Option(shortName = "u",
      description = "The functional unit. This is used to resolve filenames and decrypt the input file if necessary.")
  String getUnit();

  @Option(shortName = "o", longName = "out",
      description = "The directory into which the decrypted files are written. Directory is created if it does not exist.")
  String getOutput();

  @Option(shortName = "s", longName = "size",
      description = "Maximum number of entities to write per file. Default is 500.", defaultValue = "500")
  Integer getChunkSize();

  @Unparsed(name = "FILE")
  List<String> getFiles();

}
