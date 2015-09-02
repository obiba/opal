package org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface DataExportUiHandlers extends ModalUiHandlers {
  void cancel();

  /**
   *
   * @param dataFormat
   * @param out Destination folder or database depending on the data format.
   * @param idMapping
   */
  void onSubmit(String dataFormat, String out, String idMapping);
}
