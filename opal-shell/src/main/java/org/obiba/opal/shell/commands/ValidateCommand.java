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
        int errorCode = CommandResultCode.CRITICAL_ERROR;
        Datasource ds = null;
        ValueTable valueTable = null;

        String datasource = getOptions().getDatasource();
        if (datasource == null) {
            getShell().printf("No datasource specified\n");
        } else {
            try {
                ds = MagmaEngine.get().getDatasource(datasource);
            } catch (NoSuchDatasourceException ex) {
                getShell().printf("Datasource not found\n");
            }
        }

        if (ds != null) {
            String table = getOptions().getTable();
            if (table == null) {
                getShell().printf("No table specified\n");
            }

            try {
                valueTable = ds.getValueTable(table);
            } catch (NoSuchValueTableException ex) {
                getShell().printf("Table not found\n");
            }
        }

        if (valueTable != null) {
            return executeValidation(valueTable);
        } else {
            return errorCode;
        }
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
