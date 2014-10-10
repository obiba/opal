package org.obiba.opal.core.service;

import org.obiba.magma.*;
import org.obiba.opal.core.service.validation.DataConstraint;
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


    @Override
    public boolean isValidationEnabled(ValueTable valueTable) {
        return true;
        /* @todo enabled when ValueTable supports attributes
        try {
            return Boolean.valueOf(valueTable.getAttributeStringValue(VALIDATE_ATTRIBUTE));
        } catch (NoSuchAttributeException ex) {
            return false;
        }
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
                                      Map<String, List<DataConstraint>> constraintMap) {

        final ValidationResult result = new ValidationResult();

        logger.info("Validating table %s.%s", valueTable.getDatasource().getName(), valueTable.getName());

        for (ValueSet vset : valueTable.getValueSets()) {
            for (Map.Entry<String, List<DataConstraint>> entry : constraintMap.entrySet()) {
                String varName = entry.getKey();
                Value value = valueTable.getValue(valueTable.getVariable(varName), vset);

                List<DataConstraint> constraints = entry.getValue();
                for (DataConstraint constraint : constraints) {
                    if (!constraint.isValid(value)) {
                        logger.warn(getValidationFailMessage(constraint, varName, value));
                        result.addFailure(varName, constraint.getType(), value);
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

        if (isValidationEnabled(valueTable)) {
            for (Variable var: valueTable.getVariables()) {
                List<DataConstraint> constraints = validatorFactory.getValidators(valueTable, var);
                if (constraints != null && constraints.size() > 0) {
                    logger.info("Validators for variable %s: %s", var.getName(), constraints.toString());
                    task.addConstraints(var, constraints);
                }
            }
        }

        return task;
    }

    private String getValidationFailMessage(DataConstraint validator, String varName, Value value) {
        return String.format("Failed validation for rule %s on variable %s: %s", validator.getType(), varName, String.valueOf(value));
    }

    private class InternalValidationTask implements ValidationTask {

        @NotNull
        private final ValueTable valueTable;

        @NotNull
        private final MessageLogger logger;

        private final Map<String, List<DataConstraint>> constraintMap = new HashMap<>();

        public InternalValidationTask(ValueTable valueTable, MessageLogger logger) {
            this.valueTable = valueTable;
            this.logger = logger;
        }

        private void addConstraints(Variable var, List<DataConstraint> constraints) {
            constraintMap.put(var.getName(), constraints);
        }

        private boolean isEmpty() {
            return constraintMap.isEmpty();
        }

        @Override
        public List<String> getVariableNames() {
            return new ArrayList<>(constraintMap.keySet());
        }

        @Override
        public boolean isValid(Variable var, Value value) {
            List<DataConstraint> constraints = constraintMap.get(var.getName());
            if (constraints != null) {
                for (DataConstraint constraint: constraints) {
                    if (!constraint.isValid(value)) {
                        logger.warn(getValidationFailMessage(constraint, var.getName(), value));
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public ValidationResult validate() {
            return ValidationServiceImpl.this.validate(valueTable, logger, constraintMap);
        }
    }
}
