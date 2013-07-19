package org.obiba.opal.web.gwt.app.client.presenter;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

public interface ApplicationUiHandlers extends UiHandlers {

  void onDashboard();

  void onProjects();

  void onAdministration();

  void onHelp();

  void onQuit();

  void onSelection(SelectionEvent<SuggestOracle.Suggestion> event);

}
