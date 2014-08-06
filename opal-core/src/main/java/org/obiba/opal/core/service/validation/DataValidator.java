package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;

/**
 * Represents a data validation rule.
 */
public interface DataValidator {

	/**
	 * @return type of validator
	 */
    String getType();
    
    /**
     * @return unique name of this validator
     */
    String getName();

    /**
     * @param value
     * @return true if given value is valid for this rule
     */
    boolean isValid(Value value);

}
