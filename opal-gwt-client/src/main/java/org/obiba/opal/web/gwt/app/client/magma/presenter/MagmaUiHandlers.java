package org.obiba.opal.web.gwt.app.client.magma.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface MagmaUiHandlers extends UiHandlers {

  void onDatasourceSelection(String name);

  void onTableSelection(String datasource, String table);

}
