package org.obiba.opal.web.gwt.app.client.project.identifiersmappings.event;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;
import org.obiba.opal.web.model.client.opal.ProjectDto;

@GenEvent
public class IdentifiersMappingAdded {
  @Order(0)
  ProjectDto.IdentifiersMappingDto analysisDto;
}
