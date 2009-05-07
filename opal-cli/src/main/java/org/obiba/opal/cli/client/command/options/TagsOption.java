package org.obiba.opal.cli.client.command.options;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the <code>tags</code> option for commands that use it.
 * 
 * A list of one or more tags may be specified (separated by commas).
 * 
 * Note that in this interface the <code>tags</code> option is not mandatory.
 * 
 * @author cag-dspathis
 * 
 */
public interface TagsOption {

  @Option
  public String getTags();

  public boolean isTags();
}