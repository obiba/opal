package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares the options that may be used with the onyx-import command.
 * 
 * Note that the <code>getFile</code> method is used to access an (optional) unparsed file argument at the end of the
 * command line.
 * 
 * @author cag-dspathis
 * 
 */
@CommandLineInterface(application = "onyx-import")
public interface OnyxImportCommandOptions extends GlobalOptions, KeystoreOption, DateOption, SiteOption, TagsOption {

  @Unparsed(name = "FILE")
  public List<File> getFiles();

  public boolean isFiles();
}
