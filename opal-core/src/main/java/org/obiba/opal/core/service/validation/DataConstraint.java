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
     * @return unique name of this constraint
     */
    String getName();

    /**
     * @param value
     * @return true if given value is valid for this constraint
     */
    boolean isValid(Value value);

}
