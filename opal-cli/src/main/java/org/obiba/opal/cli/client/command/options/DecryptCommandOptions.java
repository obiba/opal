package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the decrypt command.
 * 
 * Note that the <code>getFiles</code> method is used to access unparsed file arguments at the end of the command line.
 * 
 * @author cag-dspathis
 * 
 */
@CommandLineInterface(application = "decrypt")
public interface DecryptCommandOptions extends HelpOption {

  @Unparsed(name = "FILE")
  @Option(description = "A list of files to be decrypted.")
  public List<File> getFiles();

  public boolean isFiles();

  public boolean isOutput();

  @Option(description = "File into which the decrypted file is written.")
  public File getOutput();

  @Option(description = "Specify the alias name for the keystore entry.")
  public String getAlias();
}
