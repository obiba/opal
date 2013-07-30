package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DataShieldMethodUiHandlers extends UiHandlers {
  void save();
  void cancel();
  void onDialogHidden();
}
