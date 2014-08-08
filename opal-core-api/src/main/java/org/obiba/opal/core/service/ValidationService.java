package org.obiba.opal.core.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.support.MessageListener;

/**
 * Service for validation.
 */
public interface ValidationService {

    public static final String VALIDATE_ATTRIBUTE = "validate";
    public static final String VOCABULARY_URL_ATTRIBUTE = "vocabulary_url";

    /**
     * Returns true if all the values in the table are valid.
     * If the tables is not configured for validation, returns true immediately.
     * This method is fail-fast, so intended to return false on the first invalid value, not to collect all the failures.
     *
     * @param valueTable
     * @param  listener
     * @return
     */
    boolean isValid(ValueTable valueTable, MessageListener listener);

    /**
     * Validates the table values, collecting all the failures and returning the result.
     *
     * @param valueTable table to be validated
     * @param listener
     * @return validation result (if datasource is configured for validation), or null
     */
    ValidationResult validateData(ValueTable valueTable, MessageListener listener);

    /**
     * Container for validation results.
     */
    public class ValidationResult {

        private final Map<List<String>, Set<Value>> failureMap = new HashMap<>();

        /**
         * Adds a validation failure to this.
         * @param variable
         * @param rule
         * @param value
         */
        public synchronized void addFailure(String variable, String rule, Value value) {
        	List<String> key = getKey(variable, rule);
            Set<Value> set = failureMap.get(key);
            if (set == null) {
                set = new HashSet<>();
                
                failureMap.put(key, set);
            }
            set.add(value);
        }
        
        public boolean hasFailures() {
        	return failureMap.size() > 0;
        }

        /**
         * Gets all the values that failed the given variable/rule_type pair.
         *  
         * @param variableRulePair
         * @return
         */
        public Set<Value> getFailedValues(List<String> variableRulePair) {
        	Set<Value> result = failureMap.get(variableRulePair);
        	return result != null ? new HashSet<>(result) : Collections.<Value>emptySet();
        }

        /**
         * Gets all the values that failed the given variable and rule_type.
         * 
         * @param variable
         * @param rule rule type
         * @return
         */
        public Set<Value> getFailedValues(String variable, String rule) {
        	return getFailedValues(getKey(variable, rule));
        }
        
        /**
         * @return list of pairs variable/rule_type that have at least one failure
         */
        public List<List<String>> getFailurePairs() {
        	List<List<String>> result = new ArrayList<>();
        	for (List<String> key: failureMap.keySet()) {
        		result.add(key);
        	}
        	return result;
        }
        
        private List<String> getKey(String variable, String rule) {
        	List<String> list = new ArrayList<>();
        	list.add(variable);
        	list.add(rule);
        	return Collections.unmodifiableList(list);
        }
    }
    
}
