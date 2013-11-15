package org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface DataExportUiHandlers extends ModalUiHandlers {
  void cancel();

  void onSubmit(String fileFormat, String outFile, String unit);
}
