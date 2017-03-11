package org.obiba.opal.web.gwt.app.client.project.genotypes.event;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Optional;
import com.gwtplatform.dispatch.annotation.Order;

@GenEvent
public class VcfFileUploadRequest {
    @Order(0)
    String file;

    @Optional @Order(1)
    String name;
}
