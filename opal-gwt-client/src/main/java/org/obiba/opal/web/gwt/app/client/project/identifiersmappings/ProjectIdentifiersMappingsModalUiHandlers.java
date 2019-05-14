package org.obiba.opal.web.gwt.app.client.project.identifiersmappings;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.opal.ProjectDto;

interface ProjectIdentifiersMappingsModalUiHandlers extends ModalUiHandlers {
  void updateMappings(String tableName);

  void save(ProjectDto.IdentifiersMappingDto mapping);
}
