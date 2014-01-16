package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.ui.VariableSuggestOracle;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.gwtplatform.mvp.client.UiHandlers;

public interface ApplicationUiHandlers extends UiHandlers {

  void onDashboard();

  void onProjects();

  void onAdministration();

  void onQuit();

  void onSelection(VariableSuggestOracle.VariableSuggestion suggestion);

}
