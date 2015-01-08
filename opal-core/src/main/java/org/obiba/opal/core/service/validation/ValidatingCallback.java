package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * BaseValidatingCallback impl that wraps/decorates another callback and fails-fast (with a ValidationException)
 */
public class ValidatingCallback extends BaseValidatingCallback {

    @NotNull
    private final ConcurrentValueTableReader.ConcurrentReaderCallback delegate;

    public ValidatingCallback(ValidationTask validationTask,
                              ConcurrentValueTableReader.ConcurrentReaderCallback delegate) {
        super(validationTask);
        this.delegate = delegate;
    }

    @Override
    public void onBegin(List<VariableEntity> entities, Variable... variables) {
        super.onBegin(entities, variables);
        delegate.onBegin(entities, variables);
    }

    @Override
    public void onComplete() {
        delegate.onComplete();
    }

    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    @Override
    protected void doValidation(Variable var, Value value, VariableEntity entity) {
        //throws ValidationException if validation fails
        this.validationTask.validate(var, value, entity);
    }

}
