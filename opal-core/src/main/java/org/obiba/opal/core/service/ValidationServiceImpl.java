package org.obiba.opal.core.service;

import org.obiba.magma.*;
import org.obiba.opal.core.service.validation.DataValidator;
import org.obiba.opal.core.service.validation.ValidatorFactory;
import org.obiba.opal.core.support.MessageLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.*;

@Component
public class ValidationServiceImpl implements ValidationService {

    //@Autowired
    //private TransactionTemplate txTemplate;

    @Autowired
    ValidatorFactory validatorFactory;

    private boolean isValidationEnabled(Datasource datasource) {
        return true;
        /* @todo implement way to set this attribute on the project
        boolean result = false;
        try {
            result = Boolean.valueOf(datasource.getAttributeStringValue(VALIDATE_ATTRIBUTE));
        } catch (NoSuchAttributeException ex) {
            //ignored
        }
        return result;
        */
    }


    /*
    //previous code performing validation in a transaction
    @Override
    public ValidationResult validateData(final ValueTable valueTable, final MessageListener logger) {

        if (!isValidationEnabled(valueTable.getDatasource())) {
            return null;
        }

        final ValidationResult result = new ValidationResult();

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                validate(valueTable, result, logger);
            }
        };

        txTemplate.execute(
                new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        task.run();
                        status.flush();
                    }
                });

        return result;
    }
*/

    private ValidationResult validate(ValueTable valueTable, final MessageLogger logger,
                                      Map<String, List<DataValidator>> validatorMap) {

        final ValidationResult result = new ValidationResult();

        logger.info("Validating table %s.%s", valueTable.getDatasource().getName(), valueTable.getName());
        Iterator<ValueSet> valueSets = valueTable.getValueSets().iterator();

        while (valueSets.hasNext()) {
            ValueSet vset = valueSets.next();
            for (Map.Entry<String, List<DataValidator>> entry: validatorMap.entrySet()) {
                String varName = entry.getKey();
                Value value = valueTable.getValue(valueTable.getVariable(varName), vset);

                List<DataValidator> validators = entry.getValue();
                for (DataValidator validator: validators) {
                    if (!validator.isValid(value)) {
                        logger.warn(getValidationFailMessage(validator,varName, value));
                        result.addFailure(varName, validator.getType(), value);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public ValidationTask createValidationTask(ValueTable valueTable, MessageLogger logger) {
        InternalValidationTask task = createInternalValidationTask(valueTable, logger);
        if (task.isEmpty()) {
            return null; //noting to validate
        }

        return task;
    }

    private InternalValidationTask createInternalValidationTask(ValueTable valueTable, MessageLogger logger) {
        InternalValidationTask task = new InternalValidationTask(valueTable, logger);

        if (isValidationEnabled(valueTable.getDatasource())) {
            for (Variable var: valueTable.getVariables()) {
                List<DataValidator> validators = validatorFactory.getValidators(var);
                if (validators != null && validators.size() > 0) {
                    logger.info("Validators for variable %s: %s", var.getName(), validators.toString());
                    task.addValidators(var, validators);
                }
            }
        }

        return task;
    }

    private String getValidationFailMessage(DataValidator validator, String varName, Value value) {
        return String.format("Failed validation for rule %s on variable %s: %s", validator.getType(), varName, String.valueOf(value));
    }

    private class InternalValidationTask implements ValidationTask {

        @NotNull
        private final ValueTable valueTable;

        @NotNull
        private final MessageLogger logger;

        private final Map<String, List<DataValidator>> validatorMap = new HashMap<>();

        public InternalValidationTask(ValueTable valueTable, MessageLogger logger) {
            this.valueTable = valueTable;
            this.logger = logger;
        }

        private void addValidators(Variable var, List<DataValidator> validators) {
            validatorMap.put(var.getName(), validators);
        }

        private boolean isEmpty() {
            return validatorMap.isEmpty();
        }

        @Override
        public List<String> getVariableNames() {
            return new ArrayList<>(validatorMap.keySet());
        }

        @Override
        public boolean isValid(Variable var, Value value) {
            List<DataValidator> validators = validatorMap.get(var.getName());
            if (validators != null) {
                for (DataValidator validator: validators) {
                    if (!validator.isValid(value)) {
                        logger.warn(getValidationFailMessage(validator, var.getName(), value));
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public ValidationResult validate() {
            return ValidationServiceImpl.this.validate(valueTable, logger, validatorMap);
        }
    }
}
