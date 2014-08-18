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
        Datasource ds;
        ValueTable valueTable;

        try {
            String datasource = getOptions().getDatasource();
            ds = MagmaEngine.get().getDatasource(datasource);
        } catch (NullPointerException ex) {
            getShell().printf("No datasource specified\n");
            return errorCode;
        } catch (NoSuchDatasourceException ex) {
            getShell().printf("Datasource not found\n");
            return errorCode;
        }

        try {
            String table = getOptions().getTable();
            valueTable = ds.getValueTable(table);
        } catch (NullPointerException ex) {
            getShell().printf("No table specified\n");
            return errorCode;
        } catch (NoSuchValueTableException ex) {
            getShell().printf("Table not found\n");
            return errorCode;
        }

        return executeValidation(valueTable);
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
