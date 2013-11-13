package org.obiba.opal.web.gwt.app.client.magma.copydata.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface DataCopyUiHandlers extends ModalUiHandlers {
  void cancel();

  void onSubmit();
}
