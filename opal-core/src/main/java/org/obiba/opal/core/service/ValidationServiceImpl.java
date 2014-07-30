package org.obiba.opal.core.service;

import org.obiba.magma.Datasource;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by carlos on 7/28/14.
 */
@Component
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    private TransactionTemplate txTemplate;

    @Override
    public void validateData(final Datasource ds, final ValueTable valueTable,
                             final ValidationListener listener) {

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                validate(ds, valueTable, listener);
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
    }

    private void validate(final Datasource ds, final ValueTable valueTable,
                          final ValidationListener listener) {

        final ValidatorFactory validatorFactory = new ValidatorFactory();
        Map<String, List<DataValidator>> validatorMap = new HashMap<>();
        ValueTable vt = ds.getValueTable(valueTable.getName());

        for (Variable var: vt.getVariables()) {
            List<DataValidator> validators = validatorFactory.getValidators(var, listener);
            //copyAttributes(var, valueTable.getVariable(var.getName()));
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
            Set<String> failures = getFailures(validatorMap, valueTable, vset);
            if (failures.size() > 0) {
                listener.addFailure(vset.getVariableEntity(), failures);
            }
        }

    }

    public Set<String> getFailures(Map<String, List<DataValidator>> validatorMap, ValueTable valueTable,
                                   ValueSet valueSet) {

        Set<String> result = new HashSet<>();
        for (Map.Entry<String, List<DataValidator>> entry: validatorMap.entrySet()) {
            Value value = valueTable.getValue(valueTable.getVariable(entry.getKey()), valueSet);
            List<DataValidator> validators = entry.getValue();
            for (DataValidator validator: validators) {
                if (!validator.isValid(value)) {
                    result.add(validator.getName());
                }
            }
        }
        return result;
    }
}
