package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.ProjectFactoryDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface ProjectsUiHandlers extends UiHandlers {

  void onProjectSelection(ProjectDto project);

  boolean onAddProject(ProjectFactoryDto project);

}
