package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.opal.ProjectFactoryDto;

public interface AddProjectModalUiHandlers extends ModalUiHandlers {
  boolean addProject(ProjectFactoryDto project);
}
