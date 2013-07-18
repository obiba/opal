package org.obiba.opal.web.gwt.app.client.project.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface VariableUiHandlers extends UiHandlers {

  void onNextVariable();

  void onPreviousVariable();

  void onEdit();

  void onRemove();

  void onAddToView();

  void onCategorizeToAnother();

  void onCategorizeToThis();

  void onDeriveCustom();

  void onShowSummary();

  void onShowValues();
}
