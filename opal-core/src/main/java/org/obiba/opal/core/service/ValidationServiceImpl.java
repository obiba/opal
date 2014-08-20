package org.obiba.opal.core.service;

import com.google.common.annotations.VisibleForTesting;
import org.obiba.magma.*;
import org.obiba.opal.core.service.validation.DataValidator;
import org.obiba.opal.core.service.validation.ValidatorFactory;
import org.obiba.opal.core.support.MessageLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.util.*;

@Component
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    private TransactionTemplate txTemplate;

    @Autowired
    ValidatorFactory validatorFactory;

    private boolean isValidationEnabled(Datasource datasource) {
        return true;
        /* @todo implement way to set this attribute on the project
        try {
            return Boolean.valueOf(datasource.getAttributeStringValue(VALIDATE_ATTRIBUTE));
        } catch (NoSuchAttributeException ex) {
            return false;
        }
        */
    }

    private ValidationResult validateInTransaction(final ValueTable valueTable, final MessageLogger logger,
                                      final Map<String, List<DataValidator>> validatorMap) {

        if (!isValidationEnabled(valueTable.getDatasource())) {
            return null;
        }

        final ValidationResult result = new ValidationResult();

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                collectResults(valueTable, logger, validatorMap, result);
            }
        };

        txTemplate.execute(
                new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        task.run();
                        //status.flush();
                    }
                });

        return result;

    }

    /**
     * For testing purposes only.
     * Creates a validation task and runs the validation with no transaction/real database required
     * @param valueTable
     * @param logger
     * @return
     */
    @VisibleForTesting
    ValidationResult validateNoTransaction(ValueTable valueTable, MessageLogger logger) {
        InternalValidationTask task = createInternalValidationTask(valueTable, logger);
        final ValidationResult result = new ValidationResult();
        collectResults(valueTable, logger, task.validatorMap, result);
        return result;
    }

    private Set<String> getRuleTypes(List<DataValidator> validators) {
        Set<String> result = new HashSet<>();
        for (DataValidator validator: validators) {
            result.add(validator.getType());
        }
        return result;
    }

    private void collectResults(ValueTable valueTable,
                                final MessageLogger logger,
                                Map<String, List<DataValidator>> validatorMap,
                                ValidationResult collector) {

        logger.info("Validating table %s.%s", valueTable.getDatasource().getName(), valueTable.getName());

        for (Map.Entry<String,List<DataValidator>> entry: validatorMap.entrySet()) {
            collector.setRules(entry.getKey(), getRuleTypes(entry.getValue()));
        }

        for (ValueSet vset : valueTable.getValueSets()) {
            for (Map.Entry<String, List<DataValidator>> entry : validatorMap.entrySet()) {
                String varName = entry.getKey();
                Value value = valueTable.getValue(valueTable.getVariable(varName), vset);

                List<DataValidator> validators = entry.getValue();
                for (DataValidator validator : validators) {
                    if (!validator.isValid(value)) {
                        logger.warn(getValidationFailMessage(validator, varName, value));
                        collector.addFailure(varName, validator.getType(), value);
                    }
                }
            }
        }
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
            return ValidationServiceImpl.this.validateInTransaction(valueTable, logger, validatorMap);
        }
    }
}
