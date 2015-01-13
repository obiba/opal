package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Base validation behavior for ConcurrentReaderCallbacks
 */
public abstract class BaseValidatingCallback implements ConcurrentValueTableReader.ConcurrentReaderCallback {

    @NotNull
    protected final ValidationTask validationTask;

    private List<String> validationVariables;

    public BaseValidatingCallback(ValidationTask validationTask) {
        this.validationTask = validationTask;
    }

    @Override
    public void onBegin(List<VariableEntity> entities, Variable... variables) {
        this.validationVariables = validationTask.getVariableNames();
    }

    @Override
    public void onValues(VariableEntity entity, Variable[] variables, Value... values) {
        for(int i = 0; i<variables.length; i++) {
            Variable var = variables[i];

            if (!validationVariables.contains(var.getName())) {
                continue; //variable not validated: ignore
            }

            Value value = values[i];

            if (value.isNull()) {
                //we dont validate null: only useful if we have a NotNull validation rule, and make all other rules accept null
            } else if (value.isSequence()) {
                for (Value val: value.asSequence().getValue()) {
                    if (val.isNull()) {
                        //we dont validate null: only useful if we have a NotNull validation rule, and make all other rules accept null
                    } else {
                        doValidation(var, val, entity);
                    }
                }
            } else {
                doValidation(var, value, entity);
            }
        }
    }

    /**
     * Performs the actual validation. Values is never a sequence at this point.
     *
     * @param var
     * @param value
     * @param entity
     */
    protected abstract void doValidation(Variable var, Value value, VariableEntity entity);

}
