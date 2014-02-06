package org.obiba.opal.web.gwt.app.client.administration.database.list.identifiers;

import com.gwtplatform.mvp.client.UiHandlers;

public interface IdentifiersDatabaseUiHandlers extends UiHandlers {

  void createSql();

  void createMongo();

  void edit();

  void testConnection();

  void deleteDatabase();
}
