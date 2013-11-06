package org.obiba.opal.web.gwt.app.client.magma.variable.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

public interface VariablePropertiesModalUiHandlers extends ModalUiHandlers {

  void onSave(String name, String valueType, boolean repeatable, String unit, String mimeType, String occurrenceGroup, String referencedEntityType);
}
