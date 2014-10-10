package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;

import java.util.Set;

/**
 * Impl of DataConstraint that checks for existence in vocabulary codes.
 */
public class VocabularyConstraint implements DataConstraint {

	public static final String TYPE = "Vocabulary";
	
    private final String name;
    private final Set<String> codes;

    public VocabularyConstraint(String name, Set<String> codes) {
        this.name = name;
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
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getName());
    }

}
