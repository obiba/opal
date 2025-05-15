package org.obiba.opal.core.event;

import org.obiba.opal.core.domain.kubernetes.PodSpec;

public class PodSpecRegisteredEvent {

    private final PodSpec spec;

    public PodSpecRegisteredEvent(PodSpec spec) {
        this.spec = spec;
    }

    public PodSpec getPodSpec() {
        return spec;
    }
}
