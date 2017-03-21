package org.obiba.opal.web.gwt.app.client.project.genotypes.event;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;

@GenEvent
public class VcfMappingDeleteRequest
{
  @Order(0)
  String projectName;
}
