package org.obiba.opal.shell.commands;


import org.obiba.opal.shell.commands.options.FileBundleCommandOptions;

@CommandUsage(description = "Prepare a file bundle.",
    syntax = "Syntax: file-bundle --path PATH [--password PASSWORD]")
public class FileBundleCommand extends AbstractOpalRuntimeDependentCommand<FileBundleCommandOptions> {

  @Override
  public int execute() {
    return 0;
  }
}
