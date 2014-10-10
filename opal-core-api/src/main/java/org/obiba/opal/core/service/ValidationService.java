package org.obiba.opal.core.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.support.MessageLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for validation.
 * The validation itself is done inside the ValidationTask.
 */
public interface ValidationService {

    public static final String VALIDATE_ATTRIBUTE = "validate";

    /**
     * @param valueTable table to be validated
     * @param logger
     * @return new validation task for the given table and listener, or null if validation is not enabled for that table.
     */
    ValidationTask createValidationTask(ValueTable valueTable, MessageLogger logger);

    /**
     * @param valueTable
     * @return true if validation is enabled for the given table.
     */
    boolean isValidationEnabled(ValueTable valueTable);

    /**
     * Container for validation results.
     */
    public class ValidationResult {

        private final Map<String, Set<String>> ruleMap = new HashMap<>();
        private final SetMultimap<List<String>, Value> failureMap = HashMultimap.<List<String>, Value>create();

        public void setRules(String variable, Set<String> rules) {
            ruleMap.put(variable, Collections.unmodifiableSet(rules));
        }

        /**
         * Adds a validation failure to this.
         * @param variable
         * @param rule
         * @param value
         */
        public void addFailure(String variable, String rule, Value value) {
            failureMap.put(ImmutableList.of(variable, rule), value);
        }
        
        public boolean hasFailures() {
        	return failureMap.size() > 0;
        }

        /**
         * Gets all the values that failed the given variable and rule_type.
         * 
         * @param variable
         * @param rule rule type
         * @return
         */
        public Set<Value> getFailedValues(String variable, String rule) {
            return ImmutableSet.copyOf(failureMap.get(ImmutableList.of(variable, rule)));
        }
        
        /**
         * @return list of pairs variable/rule_type that have at least one failure
         */
        public Set<List<String>> getFailurePairs() {
            return ImmutableSet.copyOf(failureMap.keySet());
        }


        /**
         * @return the map of all variables with some validation enabled to a set of validation rules
         */
        public Map<String, Set<String>> getVariableRules() {
            return ruleMap;
        }
    }

    /**
     * Represents a delayed validation task that is aware of the table under validation.
     */
    public interface ValidationTask {

        /**
         * @return list of names of variables under validation
         */
        List<String> getVariableNames();

        /**
         * @param variable
         * @param value
         * @return false if variable has validation enabled and some rule failed for given value, true otherwise
         */
        boolean isValid(Variable variable, Value value);

        /**
         * Validates the whole table data, collecting and returning the results
         * @return
         */
        ValidationResult validate();
    }
    
}
