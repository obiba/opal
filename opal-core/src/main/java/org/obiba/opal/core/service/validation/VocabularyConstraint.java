package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;

import java.util.Set;

/**
 * Impl of DataConstraint that checks for existence in vocabulary codes.
 */
public class VocabularyConstraint implements DataConstraint {

	public static final String TYPE = "Vocabulary";
	
    private final String messagePattern;
    private final Set<String> codes;

    public VocabularyConstraint(String messagePattern, Set<String> codes) {
        this.messagePattern = messagePattern;
        this.codes = codes;
    }

    @Override
    public boolean isValid(Value value) {
        if (value.isNull()) {
            return true;
        }
        return codes.contains(value.toString());
    }

    @Override
	public String getType() {
		return TYPE;
	}


    @Override
    public String getMessage(String variable, String value) {
        return String.format(messagePattern, variable, value);
    }

}
