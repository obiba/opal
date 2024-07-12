package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.security.SubjectToken;

public class DuplicateSubjectTokenException extends RuntimeException {
    private final SubjectToken existing;

    public DuplicateSubjectTokenException(SubjectToken token) {
        super("Subject token with name " + token.getName() + " already exists for principal " + token.getPrincipal());
        existing = token;
    }

    public SubjectToken getExisting() {
        return existing;
    }
}
