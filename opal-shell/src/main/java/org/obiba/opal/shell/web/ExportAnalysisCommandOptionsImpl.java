package org.obiba.opal.shell.web;

import org.obiba.opal.shell.commands.options.ExportAnalysisCommandOptions;
import org.obiba.opal.web.model.Commands;

import java.util.List;

public class ExportAnalysisCommandOptionsImpl implements ExportAnalysisCommandOptions {

  private final Commands.ExportAnalysisCommandOptionsDto dto;

  public ExportAnalysisCommandOptionsImpl(Commands.ExportAnalysisCommandOptionsDto dto) {
    this.dto = dto;
  }

  @Override
  public String getProject() {
    return dto.getProject();
  }

  @Override
  public List<String> getTables() {
    return dto.getTablesList();
  }

  @Override
  public boolean isHelp() {
    return false;
  }
}
