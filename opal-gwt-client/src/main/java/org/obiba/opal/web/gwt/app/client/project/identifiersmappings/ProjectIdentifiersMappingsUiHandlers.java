package org.obiba.opal.web.gwt.app.client.project.identifiersmappings;

import com.gwtplatform.mvp.client.UiHandlers;
import org.obiba.opal.web.model.client.opal.ProjectDto;

interface ProjectIdentifiersMappingsUiHandlers extends UiHandlers {
  void addIdMappings();
  void editIdMapping(ProjectDto.IdentifiersMappingDto mapping);
  void removeIdMapping(ProjectDto.IdentifiersMappingDto mapping);
}
