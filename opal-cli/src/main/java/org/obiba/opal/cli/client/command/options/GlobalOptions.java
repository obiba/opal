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

}