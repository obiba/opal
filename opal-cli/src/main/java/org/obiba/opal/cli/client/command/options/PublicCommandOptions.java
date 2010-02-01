package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * This interface declares the options that may be used with the "public" command.
 */
@CommandLineInterface(application = "public")
public interface PublicCommandOptions extends GlobalOptions {

  @Option(description = "Specify the alias name for the keystore entry.")
  public String getAlias();

  @Option(description = "Export to this file. Base64 Encoded.")
  public List<File> getRfc();

  public boolean isRfc();

}
