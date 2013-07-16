package org.obiba.opal.web.gwt.app.client.project.presenter;

import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface ProjectsUiHandlers extends UiHandlers {

  void onProjectSelection(ProjectDto project);

}
