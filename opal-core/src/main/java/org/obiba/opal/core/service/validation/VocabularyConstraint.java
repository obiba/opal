package org.obiba.opal.core.service.validation;

import org.obiba.magma.Value;

import java.util.Set;

/**
 * Impl of DataConstraint that checks for existence in vocabulary codes.
 */
public class VocabularyConstraint implements DataConstraint {

	public static final String TYPE = "Vocabulary";
	
    private final String source;
    private final Set<String> codes;

    public VocabularyConstraint(String source, Set<String> codes) {
        this.source = source;
        this.codes = codes;
    }

    @Override
    public final boolean isValid(Value value) {
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
    public final String getMessage() {
        return String.format("Not found in %s", source);
    }

}
