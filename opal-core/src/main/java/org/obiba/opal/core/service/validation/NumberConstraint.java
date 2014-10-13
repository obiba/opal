package org.obiba.opal.core.service.validation;

/**
 *
 */
public abstract class NumberConstraint implements DataConstraint {

    protected final Number constant;
    private final String type;

    public NumberConstraint(String type, Number constant) {
        this.type = type;
        this.constant = constant;
    }

    @Override
    public String getType() {
        return type;
    }

}
