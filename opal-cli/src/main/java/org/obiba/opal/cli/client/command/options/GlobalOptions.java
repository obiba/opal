package org.obiba.opal.cli.client.command.options;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * Global command options.
 * 
 * Note that none of the options is mandatory.
 * 
 * @author cag-dspathis
 * 
 */
public interface GlobalOptions {

  @Option(helpRequest = true, shortName = "h", description = "Display this help.")
  public boolean isHelp();

  @Option(shortName = "q", description = "Quiet operation.")
  public boolean isQuiet();

  @Option(shortName = "v", description = "Verbose operation")
  public boolean isVerbose();

  @Option(description = "Directory where to output.")
  public String getOutput();

  public boolean isOutput();

  @Option(shortName = "u", description = "Username to use when connecting to Opal.")
  public String getUsername();

  public boolean isUsername();

  @Option(shortName = "p", description = "Password to use when connecting to Opal.")
  public String getPassword();

  public boolean isPassword();

}