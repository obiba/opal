package org.obiba.opal.web.gwt.app.client.unit.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface FunctionalUnitUiHandlers extends UiHandlers {
  void removeUnit();
  void updateUnit();

  void exportIdentifiers();
  void exportIdentifiersMapping();
  void importIdentifiersFromFile();
  void generateIdentifiers();

  void addCryptographicKey();
}
