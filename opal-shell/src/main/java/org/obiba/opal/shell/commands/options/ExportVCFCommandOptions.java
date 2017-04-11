/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
import uk.co.flamingpenguin.jewel.cli.Unparsed;

import java.util.List;

/**
 * This interface declares the options that may be used with the import VCF command.
 */
@CommandLineInterface(application = "export-vcf")
public interface ExportVCFCommandOptions extends HelpOption {

  @Unparsed(name = "NAME")
  List<String> getNames();

  @Option(shortName = "p", description = "The project associated to the VCF store.")
  String getProject();

  @Option(shortName = "d", description = "The destination folder.")
  String getDestination();

  @Option(shortName = "t", description = "The optional table reference to be used to subset samples.")
  String getTable();

  boolean hasTable();

  @Option(shortName = "c", description = "With case control")
  boolean isCaseControl();

  @Option(shortName = "m", description = "With participant identifier mapping")
  String getParticipantIdentifiersMapping();
}
