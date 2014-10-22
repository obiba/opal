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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(type);
        sb.append("[").append(constant).append("]");
        return sb.toString();
    }


}
