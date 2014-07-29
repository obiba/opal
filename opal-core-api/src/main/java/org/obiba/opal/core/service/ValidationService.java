package org.obiba.opal.core.service;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;

import java.util.Set;

/**
 * Created by carlos on 7/29/14.
 */
public interface ValidationService {

    /**
     * Validates the data of given valueTable, logging any issues found to the logger.
     *
     * @param ds datasource holding the validation rules
     * @param valueTable data to be validated
     * @param listener where failures/messages are logged
     */
    void validateData(Datasource ds, ValueTable valueTable, ValidationListener listener);

    public interface ValidationListener extends MessageLogger {

        void addFailure(VariableEntity entity, Set<String> failedValidationRules);

    }

}
