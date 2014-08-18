package org.obiba.opal.shell.commands;

import org.obiba.magma.*;
import org.obiba.opal.core.service.ValidationService;
import org.obiba.opal.core.support.MessageLogger;
import org.obiba.opal.shell.OpalShellMessageAdapter;
import org.obiba.opal.shell.commands.options.ValidateCommandOptions;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
@CommandUsage(description = "Validate data of one or more tables/views in a datasource",
        syntax = "Syntax: import --datasource NAME --tables NAMES")
public class ValidateCommand extends AbstractOpalRuntimeDependentCommand<ValidateCommandOptions> {

    @Autowired
    private ValidationService validationService;

    @Override
    public int execute() {
        ValueTable valueTable;

        String datasource = getOptions().getDatasource();
        if (datasource == null) {
            return err("No datasource specified\n");
        }

        String table = getOptions().getTable();
        if (table == null) {
            return err("No table specified\n");
        }

        try {
            Datasource ds = MagmaEngine.get().getDatasource(datasource);
            valueTable = ds.getValueTable(table);
        } catch (NoSuchDatasourceException ex) {
            return err("Datasource not found\n");
        } catch (NoSuchValueTableException ex) {
            return err("Table not found\n");
        }

        return executeValidation(valueTable);
    }

    private int err(String errmsg) {
        getShell().printf(errmsg);
        return CommandResultCode.CRITICAL_ERROR;
    }

    private int executeValidation(ValueTable valueTable) {
        MessageLogger logger = new OpalShellMessageAdapter(getShell());
        ValidationService.ValidationTask task =  validationService.createValidationTask(valueTable, logger);
        try {
            task.validate(); //@todo: must we validate in a transaction? if so, we need a new ValidationService method
            return CommandResultCode.SUCCESS;
        } catch (Exception ex) {
            return CommandResultCode.CRITICAL_ERROR;
        }
    }
}
