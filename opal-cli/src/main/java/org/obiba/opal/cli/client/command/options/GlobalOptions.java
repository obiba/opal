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

  @Option(helpRequest = true, shortName = "h")
  public boolean isHelp();

  @Option(shortName = "q")
  public boolean isQuiet();

  @Option(shortName = "v")
  public boolean isVerbose();

  @Option
  public String getOutput();

  public boolean isOutput();
}
