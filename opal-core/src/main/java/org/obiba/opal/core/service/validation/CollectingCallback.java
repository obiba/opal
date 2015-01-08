package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

/**
 * A BaseValidatingCallback impl that collects all the validation errors.
 */
public class CollectingCallback extends BaseValidatingCallback {

    public CollectingCallback(ValidationTask validationTask) {
        super(validationTask);
    }

    @Override
    protected void doValidation(Variable var, Value value, VariableEntity entity) {
        validationTask.validateAndCollect(var, value, entity);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
