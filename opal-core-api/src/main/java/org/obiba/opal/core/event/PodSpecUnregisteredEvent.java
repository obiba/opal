package org.obiba.opal.core.event;

import org.obiba.opal.core.domain.kubernetes.PodSpec;

public class PodSpecUnregisteredEvent {

    private final PodSpec spec;

    public PodSpecUnregisteredEvent(PodSpec spec) {
        this.spec = spec;
    }

    public PodSpec getPodSpec() {
        return spec;
    }
}
