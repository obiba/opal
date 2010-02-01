package org.obiba.opal.cli.client.command.options;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the <code>site</code> option for commands that use it.
 * 
 * Note that in this interface the <code>site</code> option is not mandatory.
 * 
 * @author cag-dspathis
 * 
 */
public interface SiteOption {

  @Option
  public String getSite();

  public boolean isSite();
}
