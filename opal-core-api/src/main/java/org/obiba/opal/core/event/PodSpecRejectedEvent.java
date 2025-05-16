package org.obiba.opal.core.event;

import org.obiba.opal.core.domain.kubernetes.PodSpec;

public class PodSpecRejectedEvent {

    private final PodSpec spec;

    public PodSpecRejectedEvent(PodSpec spec) {
        this.spec = spec;
    }

    public PodSpec getPodSpec() {
        return spec;
    }
}
