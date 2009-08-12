package org.obiba.opal.cli.client.command.options;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;

@CommandLineInterface(application = "import")
public interface ImportCommandOptions extends KeystoreOption, DateOption, SiteOption, TagsOption, JobRunningCommandOptions {

}
