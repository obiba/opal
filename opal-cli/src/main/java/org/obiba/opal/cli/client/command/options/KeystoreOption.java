package org.obiba.opal.cli.client.command.options;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the <code>keystore</code> option for commands that use it.
 * 
 * Note that in this interface the <code>keystore</code> option is not mandatory.
 * 
 * @author cag-dspathis
 * 
 */
public interface KeystoreOption {

  @Option
  public String getKeystore();

  public boolean isKeystore();
}
