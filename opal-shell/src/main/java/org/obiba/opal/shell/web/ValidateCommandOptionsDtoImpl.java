package org.obiba.opal.shell.web;

import org.obiba.opal.shell.commands.options.ValidateCommandOptions;
import org.obiba.opal.web.model.Commands.ValidateCommandOptionsDto;

/**
 *
 */
public class ValidateCommandOptionsDtoImpl implements ValidateCommandOptions {

    private final ValidateCommandOptionsDto dto;

    public ValidateCommandOptionsDtoImpl(ValidateCommandOptionsDto dto) {
        this.dto = dto;
    }

    @Override
    public String getDatasource() {
        return dto.getProject();
    }

    @Override
    public String getTable() {
        return dto.getTable();
    }

    @Override
    public String getVariable() {
        return dto.getVariable();
    }

    @Override
    public boolean isHelp() {
        return false;
    }
}
