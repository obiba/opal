package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;

/**
 * Created by carlos on 7/28/14.
 */
public interface DataValidator {

    String getName();

    boolean isValid(Value value);

}
