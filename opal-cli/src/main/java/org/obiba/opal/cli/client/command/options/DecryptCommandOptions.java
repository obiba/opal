package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
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
public interface DecryptCommandOptions extends GlobalOptions, AuthenticationOptions {

  @Unparsed(name = "FILE")
  public List<File> getFiles();

  public boolean isFiles();

  public boolean isOutput();

  public String getOutput();
}
