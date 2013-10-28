/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.database.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.database.presenter.AbstractDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.IdentifiersDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.IdentifiersDatabaseUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.MongoDbSettingsDto;
import org.obiba.opal.web.model.client.database.SqlSettingsDto;

import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class IdentifiersDatabaseView extends ViewWithUiHandlers<IdentifiersDatabaseUiHandlers>
    implements IdentifiersDatabasePresenter.Display {

  interface Binder extends UiBinder<Widget, IdentifiersDatabaseView> {}

  @UiField
  Panel databasePanel;

  @UiField
  Panel createPanel;

  @UiField
  PropertiesTable properties;

  @UiField
  IconAnchor edit;

  private final Translations translations;

  @Inject
  public IdentifiersDatabaseView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    edit.setTitle(translations.editLabel());
    databasePanel.setVisible(false);
  }

  @UiHandler("createSql")
  void createSql(ClickEvent event) {
    getUiHandlers().createSql();
  }

  @UiHandler("createMongo")
  void createMongo(ClickEvent event) {
    getUiHandlers().createMongo();
  }

  @UiHandler("edit")
  void edit(ClickEvent event) {
    getUiHandlers().edit();
  }

  @UiHandler("testConnection")
  void testConnection(ClickEvent event) {
    getUiHandlers().testConnection();
  }

  @Override
  public void setDatabase(@Nullable DatabaseDto database) {
    properties.clearProperties();
    boolean hasDatabase = database != null;
    createPanel.setVisible(!hasDatabase);
    databasePanel.setVisible(hasDatabase);
    edit.setVisible(hasDatabase);
    if(hasDatabase) {
      showSqlProperties(database.getSqlSettings());
      showMongoProperties(database.getMongoDbSettings());
    }
  }

  private void showSqlProperties(@Nullable SqlSettingsDto sqlDatabase) {
    if(sqlDatabase == null) return;
    properties.addProperty(translations.typeLabel(), translations.sqlLabel());
    properties.addProperty(translations.sqlSchemaLabel(),
        AbstractDatabasePresenter.SqlSchema.valueOf(sqlDatabase.getSqlSchema().getName()).getLabel());
    properties.addProperty(translations.urlLabel(), sqlDatabase.getUrl());
    properties.addProperty(translations.driverLabel(), sqlDatabase.getDriverClass());
    properties.addProperty(translations.usernameLabel(), sqlDatabase.getUsername());
    properties.addProperty(translations.propertiesLabel(), sqlDatabase.getProperties());
  }

  private void showMongoProperties(@Nullable MongoDbSettingsDto mongoDatabase) {
    if(mongoDatabase == null) return;
    properties.addProperty(translations.typeLabel(), translations.mongoDbLabel());
    properties.addProperty(translations.urlLabel(), mongoDatabase.getUrl());
    properties.addProperty(translations.usernameLabel(), mongoDatabase.getUsername());
    properties.addProperty(translations.propertiesLabel(), mongoDatabase.getProperties());
  }

}
