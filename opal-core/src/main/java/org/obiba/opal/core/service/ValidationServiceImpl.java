package org.obiba.opal.core.service;

import com.google.common.annotations.VisibleForTesting;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.concurrent.ConcurrentValueTableReader.ConcurrentReaderCallback;
import org.obiba.opal.core.service.validation.DataConstraint;
import org.obiba.opal.core.service.validation.ValidationTask;
import org.obiba.opal.core.service.validation.ValidatingCallback;
import org.obiba.opal.core.service.validation.ValidatorFactory;
import org.obiba.opal.core.support.MessageLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadFactory;

@Component
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    ValidatorFactory validatorFactory;

    @org.springframework.beans.factory.annotation.Value("${org.obiba.opal.validation.skip:false}")
    private boolean skipValidation;

    @Autowired
    private ThreadFactory threadFactory;

    @VisibleForTesting
    boolean isValidationEnabled(ValueTable valueTable) {
        //@todo improve this when either project or table have attributes
        if (skipValidation) {
            return false;
        }

        return valueTable.isView(); //only views have validation enabled by default
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResult validate(ValueTable valueTable, MessageLogger logger) {
        ValidationTask task = createValidationTask(valueTable, logger);
        if (task == null) {
            return null;
        }
        return task.validate(valueTable);
    }

    @Override
    public ConcurrentReaderCallback createValidatingCallback(ValueTable valueTable,
                                                             ConcurrentReaderCallback delegate,
                                                             MessageLogger logger) {

        ValidationTask task = createValidationTask(valueTable, logger);
        if (task == null) {
            return null; //no validation required
        }

        return new ValidatingCallback(task, delegate);
    }

    private ValidationTask createValidationTask(ValueTable valueTable, MessageLogger logger) {

        if (isValidationEnabled(valueTable)) {
            ValidationTask task = new ValidationTask(logger, threadFactory);
            for (Variable var: valueTable.getVariables()) {
                List<DataConstraint> constraints = validatorFactory.getValidators(valueTable, var);
                if (constraints != null && constraints.size() > 0) {
                    logger.info("Validators for variable %s: %s", var.getName(), constraints.toString());
                    task.addConstraints(var, constraints);
                }
            }

            if (!task.isEmpty()) {
                return task; //only return task if some validation exist
            }
        }

        return null;
    }

}

