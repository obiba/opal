package org.obiba.opal.core.service.security;

import org.obiba.oidc.OIDCConfiguration;

public class DuplicateIDProviderException extends RuntimeException {

    private final OIDCConfiguration existing;

    public DuplicateIDProviderException(OIDCConfiguration existing) {
        super("An ID provider with name '" + existing.getName() + "' already exists");
        this.existing = existing;
    }

    public OIDCConfiguration getExisting() {
        return existing;
    }
}
