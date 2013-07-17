package org.obiba.opal.web.gwt.app.client.unit.presenter;

import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface FunctionalUnitsUiHandlers extends UiHandlers {
  void addUnit();
  void selectUnit(FunctionalUnitDto dto);
  void exportIdentifiers();
  void importIdentifiers();
  void synchronizeIdentifiers();
}
