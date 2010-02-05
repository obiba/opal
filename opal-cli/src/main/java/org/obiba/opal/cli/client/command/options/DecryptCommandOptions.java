package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the decrypt command.
 * 
 * Note that the <code>getFiles</code> method is used to access unparsed file arguments at the end of the command
 * line.
 * 
 * @author cag-dspathis
 * 
 */
@CommandLineInterface(application = "decrypt")
public interface DecryptCommandOptions extends HelpOption {

  @Unparsed(name = "FILE")
  @Option(shortName = "f", description = "The list of files to be decrypted.")
  public List<File> getFiles();

  public boolean isOutput();

  @Option(shortName = "o", longName = "out", description = "The directory into which the decrypted files are written. Default is current directory.")
  public File getOutput();

  @Option(shortName = "a", description = "The alias name of the encryption key pair.")
  public String getAlias();
}
