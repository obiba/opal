package org.obiba.opal.web.gwt.app.client.administration.database.list;

import org.obiba.opal.web.gwt.app.client.administration.database.edit.sql.SqlDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

public final class DatabaseListColumns {

  public static final String TEST_ACTION = "Test";

  public static final String UNREGISTER_ACTION = "Unregister";

  private final Translations translations;

  public DatabaseListColumns(Translations translations) {

    this.translations = translations;
  }

  public final Column<DatabaseDto, String> name = new TextColumn<DatabaseDto>() {
    @Override
    public String getValue(DatabaseDto dto) {
      String value = dto.getName();
      if(dto.getDefaultStorage()) value += " (" + translations.defaultStorage().toLowerCase() + ")";
      return value;
    }
  };

  public final Column<DatabaseDto, String> url = new TextColumn<DatabaseDto>() {

    @Override
    public String getValue(DatabaseDto dto) {
      if(dto.hasSqlSettings()) return dto.getSqlSettings().getUrl();
      return dto.getMongoDbSettings().getUrl();
    }
  };

  public final Column<DatabaseDto, String> type = new TextColumn<DatabaseDto>() {

    @Override
    public String getValue(DatabaseDto dto) {
      if(dto.hasSqlSettings()) return translations.sqlLabel();
      return translations.mongoDbLabel();
    }
  };

  public final Column<DatabaseDto, String> usage = new TextColumn<DatabaseDto>() {
    @Override
    public String getValue(DatabaseDto dto) {
      return SqlDatabaseModalPresenter.Usage.valueOf(dto.getUsage().getName()).getLabel();
    }
  };

  public final Column<DatabaseDto, String> schema = new TextColumn<DatabaseDto>() {
    @Override
    public String getValue(DatabaseDto dto) {
      if(dto.hasSqlSettings())
        return SqlDatabaseModalPresenter.SqlSchema.valueOf(dto.getSqlSettings().getSqlSchema().getName()).getLabel();
      return translations.opalMongoLabel();
    }
  };

  public final Column<DatabaseDto, String> username = new TextColumn<DatabaseDto>() {
    @Override
    public String getValue(DatabaseDto dto) {
      if(dto.hasSqlSettings()) return dto.getSqlSettings().getUsername();
      return dto.getMongoDbSettings().getUsername();
    }
  };

//  public ActionsColumn<DatabaseDto> actions = new ActionsColumn<DatabaseDto>(new ActionsProvider<DatabaseDto>() {
//
//    @Override
//    public String[] allActions() {
//      return new String[] { TEST_ACTION, EDIT_ACTION, UNREGISTER_ACTION };
//    }
//
//    @Override
//    public String[] getActions(DatabaseDto dto) {
////      if(dto.getUsedForIdentifiers()) {
//////        return dto.getHasEntities() ? new String[] { EDIT_ACTION, TEST_ACTION } : allActions();
////      }
//      return dto.getHasDatasource() ? new String[] { EDIT_ACTION, TEST_ACTION } : allActions();
//    }
//  });
}
