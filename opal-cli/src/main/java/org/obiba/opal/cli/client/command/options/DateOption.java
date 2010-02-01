package org.obiba.opal.cli.client.command.options;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the <code>date</code> option for commands that use it.
 * 
 * Note that in this interface the <code>date</code> option is not mandatory.
 * 
 * @author cag-dspathis
 * 
 */
public interface DateOption {

  @Option
  public String getDate();

  public boolean isDate();
}
