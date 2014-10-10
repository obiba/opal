package org.obiba.opal.core.service.validation;

import org.obiba.magma.Attribute;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.Variable;

/**
 *
 */
public enum ConstraintType {

    EXTERNAL_VOCABULARY("vocabulary_url"),
    EMBEDDED_VOCABULARY(null),
    MIN_VALUE("min_value"),
    MAX_VALUE("max_value"),
    ;

    private final String attribute;

    private ConstraintType(String attribute) {
        this.attribute = attribute;
    }

    public Attribute getAttributeValue(Variable variable) {
        if (attribute == null) {
            return null;
        }
        try {
            return variable.getAttribute(attribute);
        } catch (NoSuchAttributeException ex) {
            return null;
        }
    }

    public boolean isAtrributeBased() {
        return attribute != null;
    }

    public String getAttribute() {
        return attribute;
    }
}
