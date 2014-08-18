package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;

import java.util.Set;

/**
 * Impl of DataValidator that checks for existence of vocabulary codes.
 */
public class VocabularyValidator implements DataValidator {

	public static final String TYPE = "Vocabulary";
	
    private final String name;
    private final Set<String> codes;

    public VocabularyValidator(String name, Set<String> codes) {
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
