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

  PROJECTS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("projects");
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

  PROJECT_PERMISSIONS_SUBJECTS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "permissions", "subjects");
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

  DATASOURCES_COUNT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasources", "count");
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

  DATASOURCE_TABLES_EXCEL {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "tables", "excel");
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

  DATASOURCE_TABLE_FACETS_SEARCH {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "facets", "_search");
    }
  },

  DATASOURCE_TABLE_FACET_VARIABLE_SEARCH {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "facet", "variable", "{}", "_search");
    }
  },

  DATASOURCE_TABLE_VARIABLE_SUMMARY {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "variable", "{}", "summary");
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

  DATASOURCE_VIEW_VARIABLES_FILE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "view", "{}", "variables", "file");
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

  SYSTEM_CONF_TAXONOMIES_IMPORT_DEFAULT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomies", "import", "_default");

    }
  },

  SYSTEM_CONF_TAXONOMIES_SUMMARIES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomies", "summaries");

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

  SYSTEM_NAME {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "name");

    }
  },

  SYSTEM_CHARSET {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "charset");

    }
  },

  PROJECT_KEYSTORE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "keystore");
    }
  },

  PROJECT_KEYSTORE_ALIAS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "keystore", "{}");
    }
  },

  PROJECT_KEYSTORE_ALIAS_CERTIFICATE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "keystore", "{}", "certificate");
    }
  },

  SUBJECT_CREDENTIALS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-credentials");

    }
  },

  SUBJECT_CREDENTIAL_PASSWORD_UPDATE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-credential", "_current", "password");

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

  PROFILES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-profiles");

    }
  },

  PROFILE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-profile", "{}");

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

  DATABASE_IDENTIFIERS_CONNECTIONS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "database", "_identifiers", "connections");

    }
  },
  DATABASE_IDENTIFIERS_HAS_ENTITIES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "database", "_identifiers", "hasEntities");

    }
  },

  SYSTEM_STATUS_DATABASES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "status", "databases");

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
      return UriBuilder.create().segment("identifiers", "table", "{}");
    }
  },

  IDENTIFIERS_TABLE_VARIABLES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("identifiers", "table", "{}", "variables");
    }
  },

  IDENTIFIERS_TABLE_VARIABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("identifiers", "table", "{}", "variable", "{}");
    }
  },

  IDENTIFIERS_TABLE_VALUESETS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("identifiers", "table", "{}", "valueSets");
    }
  },

  VCS_VARIABLE_COMMIT_INFOS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "view", "{}", "vcs", "variable", "{}", "commits");
    }
  },

  VCS_VARIABLE_COMMIT_INFO {
    @Override
    public UriBuilder create() {
      return UriBuilder.create()
          .segment("datasource", "{}", "view", "{}", "vcs", "variable", "{}", "commit", "{}", "{}");
    }
  },

  VCS_VARIABLE_BLOB {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "view", "{}", "vcs", "variable", "{}", "blob", "{}");
    }

  },

  BOOKMARK {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-profile", "_current", "bookmark", "{}");
    }
  },

  BOOKMARKS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-profile", "_current", "bookmarks");
    }
  },

  SERVICE_SEARCH_INDICES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "search", "indices");
    }
  },

  SYSTEM_ENV {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "env");

    }
  },

  SHELL_COMMANDS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("shell", "commands");

    }
  },

  DATASHIELD_ROPTIONS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "options");

    }
  },

  DATASHIELD_ROPTION {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "option");

    }
  };

  public abstract UriBuilder create();
}
