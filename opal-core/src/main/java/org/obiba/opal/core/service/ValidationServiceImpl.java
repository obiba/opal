package org.obiba.opal.core.service;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchAttributeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.validation.DataValidator;
import org.obiba.opal.core.service.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by carlos on 7/28/14.
 */
@Component
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    private TransactionTemplate txTemplate;

    private boolean isValidationEnabled(Datasource datasource) {
        boolean result = false;
        try {
            result = Boolean.valueOf(datasource.getAttributeStringValue(VALIDATE_ATTRIBUTE));
        } catch (NoSuchAttributeException ex) {
            //ignored
        }
        return result;
    }

    @Override
    public ValidationResult validateData(final ValueTable valueTable) {

        if (!isValidationEnabled(valueTable.getDatasource())) {
            return null;
        }

        final ValidationResult result = new ValidationResult();

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                validate(valueTable, result);
            }
        };

        txTemplate.execute(
                new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        task.run();
                        status.flush();
                    }
                });

        return result;
    }

    /**
     * This method does not use database transaction, and so suitable for unit tests without database. 
     * @param valueTable
     * @param collector
     */
    void validate(final ValueTable valueTable,
                          final ValidationResult collector) {

        final ValidatorFactory validatorFactory = new ValidatorFactory();
        Map<String, List<DataValidator>> validatorMap = new HashMap<>();

        for (Variable var: valueTable.getVariables()) {
            List<DataValidator> validators = validatorFactory.getValidators(var);
            if (validators != null && validators.size() > 0) {
                validatorMap.put(var.getName(), validators);
            }
        }

        if (validatorMap.isEmpty()) {
            return; //noting to validate
        }

        Iterator<ValueSet> valueSets = valueTable.getValueSets().iterator();
        while (valueSets.hasNext()) {
            ValueSet vset = valueSets.next();
            validate(validatorMap, valueTable, vset, collector);
        }
    }

    private void validate(Map<String, List<DataValidator>> validatorMap, ValueTable valueTable,
                          ValueSet valueSet, ValidationResult collector) {

        for (Map.Entry<String, List<DataValidator>> entry: validatorMap.entrySet()) {
        	String varName = entry.getKey();
            Value value = valueTable.getValue(valueTable.getVariable(varName), valueSet);
            List<DataValidator> validators = entry.getValue();
            for (DataValidator validator: validators) {
                if (!validator.isValid(value)) {
                    collector.addFailure(varName, validator.getType(), value);
                }
            }
        }
    }
}
