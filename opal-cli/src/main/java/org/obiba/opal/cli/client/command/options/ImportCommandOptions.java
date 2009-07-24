package org.obiba.opal.cli.client.command.options;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 */
@CommandLineInterface(application = "import")
public interface ImportCommandOptions extends GlobalOptions, KeystoreOption, DateOption, SiteOption, TagsOption {

  @Option(shortName = { "l" })
  public boolean isListJobs();

  @Option(shortName = { "r" })
  public String getRun();

  public boolean isRun();

  @Unparsed(name = "Job Parmaters")
  public List<String> getJobParameters();

  public boolean isJobParameters();

}
