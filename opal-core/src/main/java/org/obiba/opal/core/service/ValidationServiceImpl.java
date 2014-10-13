package org.obiba.opal.core.service;

import org.obiba.magma.*;
import org.obiba.opal.core.service.validation.DataConstraint;
import org.obiba.opal.core.service.validation.ValidatorFactory;
import org.obiba.opal.core.support.MessageLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;
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
                        logger.warn(getMessage(vset.getVariableEntity().getIdentifier(), varName, value.toString(), constraint));
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

    private static String getMessage(String entityId, String variable, String value, DataConstraint constraint) {
        return String.format("Validation failed: Entity %s, Variable %s, Value %s: %s",
                entityId,
                variable,
                value,
                constraint.getMessage());
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
        public void validate(Variable variable, Value value, VariableEntity entity) throws ValidationException {
            List<DataConstraint> constraints = constraintMap.get(variable.getName());
            if (constraints != null) {
                for (DataConstraint constraint: constraints) {
                    if (!constraint.isValid(value)) {
                        String msg = getMessage(entity.getIdentifier(), variable.getName(), value.toString(), constraint);
                        throw new ValidationException(msg);
                    }
                }
            }
        }

        @Override
        public ValidationResult validate() {
            return ValidationServiceImpl.this.validate(valueTable, logger, constraintMap);
        }
    }
}
