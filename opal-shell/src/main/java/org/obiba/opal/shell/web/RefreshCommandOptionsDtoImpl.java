package org.obiba.opal.shell.web;

import org.obiba.opal.shell.commands.options.RefreshCommandOptions;
import org.obiba.opal.web.model.Commands.RefreshCommandOptionsDto;

public class RefreshCommandOptionsDtoImpl implements RefreshCommandOptions {

  private final RefreshCommandOptionsDto dto;

  public RefreshCommandOptionsDtoImpl(RefreshCommandOptionsDto dto) {
    this.dto = dto;
  }

  @Override
  public String getProject() {
    return dto.getProject();
  }

  @Override
  public boolean isHelp() {
    return false;
  }
}
