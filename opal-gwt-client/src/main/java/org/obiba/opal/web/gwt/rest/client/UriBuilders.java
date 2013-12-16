/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.rest.client;

public enum UriBuilders {

  REPORT_TEMPLATES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("report-templates");
    }
  },

  PROJECT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}");
    }
  },

  PROJECT_SUMMARY {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "summary");
    }
  },

  PROJECT_REPORT_TEMPLATES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "report-templates");
    }
  },

  PROJECT_REPORT_TEMPLATE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "report-template", "{}");
    }
  },

  PROJECT_REPORT_TEMPLATE_REPORTS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "report-template", "{}", "reports");
    }
  },

  PROJECT_COMMANDS_IMPORT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_import");
    }
  },

  PROJECT_COMMANDS_EXPORT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_export");
    }
  },

  PROJECT_COMMANDS_COPY {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_copy");
    }
  },

  PROJECT_TRANSIENT_DATASOURCE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "transient-datasources");
    }
  },

  PROJECT_PERMISSIONS_PROJECT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "permissions", "project");
    }
  },

  PROJECT_PERMISSIONS_DATASOURCE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "permissions", "datasource");
    }
  },

  PROJECT_PERMISSIONS_TABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "permissions", "table", "{}");
    }
  },

  PROJECT_PERMISSIONS_VARIABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "permissions", "table", "{}", "variable", "{}");
    }
  },

  DATASOURCES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasources");
    }
  },

  DATASOURCE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}");
    }
  },

  DATASOURCE_LOCALES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "locales");
    }
  },

  DATASOURCE_TABLES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "tables");
    }
  },

  DATASOURCE_VIEWS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "views");
    }
  },

  DATASOURCE_TABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}");
    }
  },

  DATASOURCE_VIEW {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "view", "{}");
    }
  },

  DATASOURCE_TABLE_INDEX {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "index");
    }
  },

  DATASOURCE_TABLE_VARIABLES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "variables");
    }
  },

  DATASOURCE_TABLE_VARIABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "variable", "{}");
    }
  },

  DATASOURCE_TABLE_FACET_VARIABLE_SEARCH {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "facet", "variable", "{}", "_search");
    }
  },

  DATASOURCE_TABLE_VARIABLES_SEARCH {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "variables", "_search");
    }
  },

  DATASOURCE_TABLE_VALUESETS_SEARCH {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "valueSets", "_search");
    }
  },

  DATASOURCE_TABLE_INDEX_SCHEMA {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "index", "_schema");
    }
  },

  DATASOURCE_VIEW_VARIABLES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "view", "{}", "variables");
    }
  },

  DATASOURCE_VIEW_VARIABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "view", "{}", "variable", "{}");
    }
  },

  DATASOURCE_TABLE_LOCALES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "locales");
    }
  },

  SYSTEM_CONF_GENERAL {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "general");

    }
  },

  SYSTEM_CONF_TAXONOMIES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomies");

    }
  },

  SYSTEM_CONF_TAXONOMY_VOCABULARY {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomy", "{}", "vocabulary", "{}");

    }
  },

  SYSTEM_CONF_TAXONOMY {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomy", "{}");

    }
  },

  SYSTEM_KEYSTORE_HTTPS_CERTIFICATE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "keystore", "https", "certificate");
    }
  },

  SYSTEM_KEYSTORE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "keystore");
    }
  },

  SUBJECT_CREDENTIALS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-credentials");

    }
  },

  GROUPS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "groups");

    }
  },

  SUBJECT_CREDENTIAL {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-credential", "{}");

    }
  },

  GROUP {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "group", "{}");

    }
  },

  DATABASES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases");

    }
  },

  DATABASES_WITH_SETTINGS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases").query("settings", "true");

    }
  },

  DATABASES_FOR_STORAGE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases").query("usage", "storage");

    }
  },

  DATABASES_FOR_IMPORT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases").query("usage", "import");

    }
  },

  DATABASES_FOR_EXPORT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases").query("usage", "export");

    }
  },

  DATABASES_SQL {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases", "sql");

    }
  },

  DATABASES_MONGO_DB {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases", "mongodb");

    }
  },

  DATABASE_IDENTIFIERS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases", "identifiers");

    }
  },

  DATABASE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "database", "{}");

    }
  },

  DATABASE_CONNECTIONS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "database", "{}", "connections");

    }
  },

  JDBC_DRIVERS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases", "jdbc-drivers");

    }

  },

  IDENTIFIERS_TABLES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("identifiers", "tables");
    }
  },

  IDENTIFIERS_TABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("identifiers", "table","{}");
    }
  },

  IDENTIFIERS_TABLE_VARIABLES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("identifiers", "table","{}", "variables");
    }
  },

  IDENTIFIERS_TABLE_VALUESETS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("identifiers", "table","{}", "valueSets");
    }
  };

  public abstract UriBuilder create();
}
