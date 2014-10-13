package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;

/**
 *
 */
public class MinValueConstraint extends NumberConstraint {

    public MinValueConstraint(double value) {
        super("MinValue", value);
    }

    @Override
    public boolean isValid(Value value) {
        if (value.isNull()) {
            return true; //null is not our problem
        }

        return Number.class.cast(value.getValue()).doubleValue() >= constant.doubleValue();
    }

    @Override
    public String getMessage() {
        return String.format("Lower than %s", constant.toString());
    }

}
