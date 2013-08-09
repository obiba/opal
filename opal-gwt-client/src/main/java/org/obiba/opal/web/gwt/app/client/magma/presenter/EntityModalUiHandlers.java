package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.magma.TableDto;

public interface EntityModalUiHandlers extends ModalUiHandlers {
  void selectTable(TableDto selectedTable);
  void loadVariables();
  void filterVariables(String filter);
}
