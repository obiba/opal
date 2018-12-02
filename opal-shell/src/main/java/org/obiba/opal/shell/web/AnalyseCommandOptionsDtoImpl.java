package org.obiba.opal.shell.web;

import org.json.JSONObject;
import org.obiba.opal.shell.commands.options.AnalyseCommandOptions;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.model.Commands.AnalyseCommandOptionsDto.AnalyseDto;

import java.util.List;
import java.util.stream.Collectors;

public class AnalyseCommandOptionsDtoImpl implements AnalyseCommandOptions {


  private final Commands.AnalyseCommandOptionsDto dto;

  public AnalyseCommandOptionsDtoImpl(Commands.AnalyseCommandOptionsDto dto) {
    this.dto = dto;
  }

  @Override
  public String getProject() {
    return dto.getProject();
  }

  @Override
  public List<AnalyseOptions> getAnalyses() {
    return dto.getAnalysesList().stream().map(AnalyseOptionsImpl::new).collect(Collectors.toList());
  }

  @Override
  public boolean isHelp() {
    return false;
  }

  class AnalyseOptionsImpl implements AnalyseOptions {
    private final AnalyseDto dto;

    private AnalyseOptionsImpl(AnalyseDto dto) {
      this.dto = dto;
    }

    @Override
    public String getId() {
      return dto.getId();
    }

    @Override
    public String getTable() {
      return dto.getTable();
    }

    @Override
    public String getVariables() {
      return dto.getVariables();
    }

    @Override
    public String getName() {
      return dto.getName();
    }

    @Override
    public String getPlugin() {
      return dto.getPlugin();
    }

    @Override
    public String getTemplate() {
      return dto.getTemplate();
    }

    @Override
    public JSONObject getParams() {
      return new JSONObject(dto.getParams());
    }

  }
}
