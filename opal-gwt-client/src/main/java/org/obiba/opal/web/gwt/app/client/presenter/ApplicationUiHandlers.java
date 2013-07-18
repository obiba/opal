package org.obiba.opal.web.gwt.app.client.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface ApplicationUiHandlers extends UiHandlers {

  void onDashboard();

  void onProjects();

  void onAdministration();

  void onHelp();

  void onQuit();

}
