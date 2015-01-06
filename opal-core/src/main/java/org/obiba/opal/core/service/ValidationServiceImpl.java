package org.obiba.opal.core.service;

import com.google.common.annotations.VisibleForTesting;
import org.obiba.magma.*;
import org.obiba.magma.Value;
import org.obiba.opal.core.service.validation.DataConstraint;
import org.obiba.opal.core.service.validation.ValidatorFactory;
import org.obiba.opal.core.support.MessageLogger;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.*;

@Component
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    private TransactionTemplate txTemplate;

    @Autowired
    ValidatorFactory validatorFactory;

    @org.springframework.beans.factory.annotation.Value("${org.obiba.opal.validation.skip:false}")
    private boolean skipValidation;

    @Override
    public boolean isValidationEnabled(ValueTable valueTable) {
        //@todo improve this when either project or table have attributes
        if (skipValidation) {
            return false;
        }

        return valueTable.isView(); //only views have validation enabled by default
    }

    private ValidationResult validateInTransaction(final ValueTable valueTable, final MessageLogger logger,
                                                   final Map<String, List<DataConstraint>> validatorMap) {

        if (!isValidationEnabled(valueTable)) {
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
        collectResults(valueTable, logger, task.constraintMap, result);
        return result;
    }

    private Set<String> getRuleTypes(List<DataConstraint> validators) {
        Set<String> result = new HashSet<>();
        for (DataConstraint validator: validators) {
            result.add(validator.getType());
        }
        return result;
    }

    private void collectResults(ValueTable valueTable,
                                final MessageLogger logger,
                                Map<String, List<DataConstraint>> constraintMap,
                                ValidationResult collector) {

        logger.info("Validating table %s.%s", valueTable.getDatasource().getName(), valueTable.getName());

        for (Map.Entry<String,List<DataConstraint>> entry: constraintMap.entrySet()) {
            collector.setRules(entry.getKey(), getRuleTypes(entry.getValue()));
        }

        for (ValueSet vset : valueTable.getValueSets()) {
            for (Map.Entry<String, List<DataConstraint>> entry : constraintMap.entrySet()) {
                String varName = entry.getKey();
                Value value = valueTable.getValue(valueTable.getVariable(varName), vset);

                if (value.isSequence()) {
                    ValueSequence seq = value.asSequence();
                    for (Value val: seq.getValue()) {
                        validate(varName, entry.getValue(), logger, collector, val, vset.getVariableEntity().getIdentifier());
                    }
                } else {
                    validate(varName, entry.getValue(), logger, collector, value, vset.getVariableEntity().getIdentifier());
                }
            }
        }
    }

    private void validate(String varName, List<DataConstraint> constraints, MessageLogger logger,
                          ValidationResult collector, Value value, String id) {

        for (DataConstraint constraint : constraints) {
            if (!constraint.isValid(value)) {
                logger.warn(getMessage(id, varName, value.toString(), constraint));
                collector.addFailure(varName, constraint.getType(), value);
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
        return String.format("Validation failed: EntityId %s, Variable %s, Value %s: %s",
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
            return ValidationServiceImpl.this.validateInTransaction(valueTable, logger, constraintMap);
        }
    }
}

