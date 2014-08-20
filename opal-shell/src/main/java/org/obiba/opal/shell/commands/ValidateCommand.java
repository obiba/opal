package org.obiba.opal.shell.commands;

import org.json.JSONObject;
import org.obiba.magma.*;
import org.obiba.opal.core.service.ValidationService;
import org.obiba.opal.core.service.ValidationService.ValidationResult;
import org.obiba.opal.core.support.MessageLogger;
import org.obiba.opal.shell.OpalShellMessageAdapter;
import org.obiba.opal.shell.commands.options.ValidateCommandOptions;
import org.obiba.opal.web.model.Opal.ValidationResultDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
@CommandUsage(description = "Validate data of one or more tables/views in a datasource",
        syntax = "Syntax: import --datasource NAME --table NAME")
public class ValidateCommand
        extends AbstractOpalRuntimeDependentCommand<ValidateCommandOptions>
        implements ExtendedCommand<ValidateCommandOptions, ValidationResultDto> {

    @Autowired
    private ValidationService validationService;

    //cached outcome, built from ValidationResultDto.Builder
    private ValidationResultDto resultDto;

    //builder of ValidationResultDto, where all the state is kept
    private ValidationResultDto.Builder resultBuilder = ValidationResultDto.newBuilder();

    @Override
    public int execute() {
        resultBuilder.setTriggerDate(System.currentTimeMillis());
        ValueTable valueTable;

        String datasource = getOptions().getDatasource();
        if (datasource == null) {
            resultBuilder.setErrorMessage("No datasource");
            return err("No datasource specified\n");
        }
        resultBuilder.setDatasource(datasource);

        String table = getOptions().getTable();
        if (table == null) {
            resultBuilder.setErrorMessage("No table");
            return err("No table specified\n");
        }
        resultBuilder.setTable(table);

        try {
            Datasource ds = MagmaEngine.get().getDatasource(datasource);
            valueTable = ds.getValueTable(table);
        } catch (NoSuchDatasourceException ex) {
            resultBuilder.setErrorMessage(ex.toString());
            return err("Datasource not found\n");
        } catch (NoSuchValueTableException ex) {
            resultBuilder.setErrorMessage(ex.toString());
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
            ValidationResult result = task.validate();
            resultBuilder.setHasFailures(result.hasFailures());
            resultBuilder.setRules(getRulesJson(result));
            resultBuilder.setFailures(getFailuresJson(result));
            return CommandResultCode.SUCCESS;
        } catch (Exception ex) {
            logger.error("Error running validation: " + ex.toString());
            ex.printStackTrace(System.err);
            resultBuilder.setErrorMessage(ex.toString());
            return CommandResultCode.CRITICAL_ERROR;
        }
    }

    @Override
    public void setJobId(int id) {
        resultBuilder.setJobId(id);
    }

    public ValidationResultDto getResult() {
        if (resultDto == null) {
            resultDto = resultBuilder.build();
        }
        return resultDto;
    }

    private static String getRulesJson(ValidationResult result) {
        JSONObject obj = new JSONObject(result.getVariableRules());
        return obj.toString();
    }

    private static String getFailuresJson(ValidationResult result) {
        Map<List<String>,Set<String>> map = new HashMap<>();
        for (List<String> pair: result.getFailurePairs()) {
            Set<String> strings = new HashSet<>();
            for (Value value: result.getFailedValues(pair.get(0), pair.get(1))) {
                strings.add(value.toString());
            }
            map.put(pair, strings);
        }
        JSONObject obj = new JSONObject(map);
        return obj.toString();
    }

}
