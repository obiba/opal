package org.obiba.opal.cli.client.command.options;

import java.util.List;

import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

public interface JobRunningCommandOptions extends GlobalOptions {

  @Option(shortName = "l")
  public boolean isListJobs();

  @Option(shortName = "r")
  public String getRun();

  public boolean isRun();

  @Option(shortName = "f")
  public boolean isForce();

  @Option(shortName = "n")
  public boolean isNext();

  @Unparsed(name = "Job Parameters")
  public List<String> getJobParameters();

  public boolean isJobParameters();

}
