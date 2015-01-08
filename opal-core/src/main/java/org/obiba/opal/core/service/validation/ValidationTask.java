package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;
import org.obiba.opal.core.service.ValidationService;
import org.obiba.opal.core.support.MessageLogger;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ThreadFactory;

/**
 * Keeps the state of a running validation: logger, variables, constraints and results.
 */
public class ValidationTask {

    @NotNull
    private final MessageLogger logger;

    private final ThreadFactory threadFactory;

    private final Map<String, List<DataConstraint>> constraintMap = new HashMap<>();

    private ValidationService.ValidationResult resultCollector = new ValidationService.ValidationResult();

    public ValidationTask(MessageLogger logger, ThreadFactory threadFactory) {
        this.logger = logger;
        this.threadFactory = threadFactory;
    }

    public void addConstraints(Variable var, List<DataConstraint> constraints) {
        constraintMap.put(var.getName(), constraints);
    }

    public boolean isEmpty() {
        return constraintMap.isEmpty();
    }

    /**
     * @return list of names of variables under validation
     */
    public List<String> getVariableNames() {
        return new ArrayList<>(constraintMap.keySet());
    }

    /**
     * Validates the given value for variable.
     * @param variable
     * @param value
     * @param entity
     * @throws ValidationException if the given value is not valid
     */
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

    /**
     * Validates the given value, updating the collector results if needed.
     * @param variable
     * @param value
     * @param entity
     */
    public void validateAndCollect(Variable variable, Value value, VariableEntity entity) {
        List<DataConstraint> constraints = constraintMap.get(variable.getName());
        if (constraints != null) {
            for (DataConstraint constraint: constraints) {
                if (!constraint.isValid(value)) {
                    String msg = getMessage(entity.getIdentifier(), variable.getName(), value.toString(), constraint);
                    logger.warn(msg);
                    resultCollector.addFailure(variable.getName(), constraint.getType(), value);
                }
            }
        }
    }

    /**
     * Validates the complete table, returning the results.
     * @param valueTable
     * @return validation results
     */
    public ValidationService.ValidationResult validate(ValueTable valueTable) {

        resultCollector = new ValidationService.ValidationResult();
        logger.info("Validating table %s.%s", valueTable.getDatasource().getName(), valueTable.getName());

        for (Map.Entry<String,List<DataConstraint>> entry: constraintMap.entrySet()) {
            resultCollector.setRules(entry.getKey(), getRuleTypes(entry.getValue()));
        }

        Set<Variable> variables = new LinkedHashSet<>();
        for (String var: getVariableNames()) {
            variables.add(valueTable.getVariable(var));
        }

        ConcurrentValueTableReader.ConcurrentReaderCallback callback = new CollectingCallback(this);
        ConcurrentValueTableReader.Builder builder =
                ConcurrentValueTableReader.Builder.newReader() //
                        .withThreads(threadFactory) //
                        .from(valueTable) //
                        .variablesFilter(variables) //
                        .to(callback); //

        builder.build().read();

        return resultCollector;
    }

    private Set<String> getRuleTypes(List<DataConstraint> validators) {
        Set<String> result = new HashSet<>();
        for (DataConstraint validator: validators) {
            result.add(validator.getType());
        }
        return result;
    }

    private static String getMessage(String entityId, String variable, String value, DataConstraint constraint) {
        return String.format("Validation failed: EntityId %s, Variable %s, Value %s: %s",
                entityId,
                variable,
                value,
                constraint.getMessage());
    }

}
