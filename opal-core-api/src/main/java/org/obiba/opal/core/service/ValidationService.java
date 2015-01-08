package org.obiba.opal.core.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.concurrent.ConcurrentValueTableReader.ConcurrentReaderCallback;
import org.obiba.opal.core.support.MessageLogger;

import java.util.*;

/**
 * Service for validation.
 */
public interface ValidationService {

    public static final String VALIDATE_ATTRIBUTE = "validate";

    /**
     * @param valueTable table to validate data
     * @param logger
     * @return result of validation, or null if not enabled/required for the given table
     */
    ValidationResult validate(ValueTable valueTable, MessageLogger logger);

    /**
     * Creates a validating ConcurrentReaderCallback wrapping the given one, if validations are configured, or null otherwise.
     * @param valueTable
     * @param delegate
     * @param logger
     * @return wrapper validating callback, or null
     */
    ConcurrentReaderCallback createValidatingCallback(ValueTable valueTable, ConcurrentReaderCallback delegate, MessageLogger logger);

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

}
