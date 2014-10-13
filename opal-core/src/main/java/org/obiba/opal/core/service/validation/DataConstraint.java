package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;

/**
 * Represents a data constraint rule.
 */
public interface DataConstraint {

	/**
	 * @return type of constraint
	 */
    String getType();
    
    /**
     * @param value
     * @return true if given value is valid for this constraint
     */
    boolean isValid(Value value);

    /**
     * @param variable
     * @param value
     * @return message for failed constraint given the variable name and offending value
     */
    String getMessage(String variable, String value);

}
