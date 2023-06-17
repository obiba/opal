/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
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

  PROJECT_IDENTIFIERS_MAPPINGS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "identifiers-mappings");
    }
  },

  PROJECT_IDENTIFIERS_MAPPING {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "identifiers-mapping");
    }
  },

  PROJECT_RESOURCES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "resources");
    }
  },

  PROJECT_RESOURCE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "resource", "{}");
    }
  },

  PROJECT_RESOURCE_TEST {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "resource", "{}", "_test");
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

  PROJECT_COMMANDS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands");
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

  PROJECT_COMMANDS_BACKUP {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_backup");
    }
  },

  PROJECT_COMMANDS_RESTORE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_restore");
    }
  },

  PROJECT_COMMANDS_COPY {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_copy");
    }
  },

  PROJECT_COMMANDS_RELOAD {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_reload");
    }
  },

  PROJECT_STATE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "state");
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

  DATASOURCES_ENTITY_TYPES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasources", "entity-types");
    }
  },

  DATASOURCE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}");
    }
  },

  DATASOURCE_SQL {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "_sql");
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

  DATASOURCES_TABLES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasources", "tables");
    }
  },

  DATASOURCES_ENTITIES_COUNT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasources", "entities", "_count");
    }
  },

  DATASOURCES_ENTITIES_SUGGEST {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasources", "entities", "_suggest");
    }
  },

  DATASOURCES_ENTITIES_SEARCH {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasources", "entities", "_search");
    }
  },

  DATASOURCES_ENTITIES_CONTINGENCY {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasources", "entities", "_contingency");
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

  DATASOURCE_TABLE_VARIABLES_ATTRIBUTE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "variables", "_attribute");
    }
  },

  DATASOURCE_TABLE_VARIABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "variable", "{}");
    }
  },

  DATASOURCE_TABLE_VALUESET {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "table", "{}", "valueSet", "{}");
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

  DATASOURCES_VARIABLES_SEARCH {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasources", "variables", "_search");
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

  DATASOURCE_VIEW_INIT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource", "{}", "view", "{}", "_init");
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

  SYSTEM_CONF_TAXONOMIES_TAGS_GITHUB {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomies", "tags", "_github");
    }
  },

  SYSTEM_CONF_TAXONOMIES_IMPORT_GITHUB {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomies", "import", "_github");
    }
  },

  SYSTEM_CONF_TAXONOMIES_IMPORT_FILE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomies", "import", "_file");
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

  SYSTEM_CONF_TAXONOMY_VOCABULARY_TERM {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomy", "{}", "vocabulary", "{}", "term", "{}");
    }
  },

  SYSTEM_CONF_TAXONOMY_VOCABULARY_TERMS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomy", "{}", "vocabulary", "{}", "terms");
    }
  },

  SYSTEM_CONF_TAXONOMY {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomy", "{}");
    }
  },

  SYSTEM_CONF_TAXONOMY_COMMITS_INFO {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomy", "{}", "commits");
    }
  },

  SYSTEM_CONF_TAXONOMY_COMMIT_INFO {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomy", "{}", "commit", "{}", "{}");
    }
  },

  SYSTEM_CONF_TAXONOMY_GIT_RESTORE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomy", "{}", "restore", "{}");
    }
  },

  SYSTEM_CONF_TAXONOMY_VOCABULARIES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "conf", "taxonomy", "{}", "vocabularies");
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

  PROJECT_VCF_STORE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "vcf-store");
    }
  },

  PROJECT_VCF_PERMISSIONS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "permissions", "vcf-store");
    }
  },

  PROJECT_VCF_STORE_VCFS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "vcf-store", "vcfs");
    }
  },

  PROJECT_VCF_STORE_IMPORT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_import_vcf");
    }
  },

  PROJECT_VCF_STORE_EXPORT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_export_vcf");
    }
  },

  PROJECT_VCF_STORE_VCF {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "vcf-store", "vcf", "{}");
    }
  },

  PROJECT_VCF_STORE_VCF_EXPORT_STATS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "vcf-store", "vcf", "{}", "_statistics");
    }
  },

  PROJECT_VCF_STORE_VCF_DOWNLOAD {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "vcf-store", "vcf", "{}", "_download");
    }
  },

  PROJECT_VCF_STORE_SAMPLES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "vcf-store", "samples");
    }
  },

  PROJECT_TABLE_ANALYSES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "table", "{}", "analyses");
    }

  },

  PROJECT_TABLE_DOWNLOAD_ANALYSES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "table", "{}", "analyses", "_export");
    }

  },

  PROJECT_TABLE_ANALYSIS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "table", "{}", "analysis", "{}");
    }
  },

  PROJECT_TABLE_ANALYSIS_RESULT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "table", "{}", "analysis", "{}", "result", "{}");
    }
  },

  PROJECT_TABLE_ANALYSIS_RESULT_EXPORT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "table", "{}", "analysis", "{}", "result", "{}", "_export");
    }
  },

  PROJECT_TABLE_ANALYSIS_RESULT_REPORT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "table", "{}", "analysis", "{}", "result", "{}", "_report");
    }
  },

  PROJECT_ANALYSE_COMMAND {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_analyse");
    }
  },

  AUTH_SESSIONS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("auth", "sessions");
    }
  },

  AUTH_SESSION_CURRENT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("auth", "session", "_current");
    }
  },

  AUTH_SESSION_CURRENT_USERNAME {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("auth", "session", "_current", "username");
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

  SUBJECT_PROFILES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-profiles");
    }
  },

  SUBJECT_PROFILE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-profile", "{}");
    }
  },

  SUBJECT_PROFILE_OTP {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-profile", "{}", "otp");
    }
  },

  SUBJECT_PROFILE_OTP_QR {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-profile", "{}", "otp", "qr");
    }
  },

  CURRENT_SUBJECT_TOKENS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-token", "_current", "tokens");
    }
  },

  CURRENT_SUBJECT_TOKEN {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-token", "_current", "token", "{}");
    }
  },

  CURRENT_SUBJECT_TOKEN_RENEW {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "subject-token", "_current", "token", "{}", "_renew");
    }
  },

  ID_PROVIDERS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "idproviders");
    }
  },

  ID_PROVIDER {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "idprovider", "{}");
    }
  },

  ID_PROVIDER_ENABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "idprovider", "{}", "_enable");
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
      return UriBuilder.create().segment("system", "databases").query("usage", "import").query("settings", "true");
    }
  },

  DATABASES_FOR_EXPORT {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("system", "databases").query("usage", "export").query("settings", "true");
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

  AUTH_PROVIDERS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("auth", "providers");
    }
  },

  APPS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("apps");
    }
  },

  APPS_CONFIG {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("apps", "config");
    }
  },

  APP {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("app", "{}");
    }
  },

  PLUGINS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("plugins");
    }
  },

  PLUGINS_AVAILABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("plugins", "_available");
    }
  },

  PLUGINS_UPDATES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("plugins", "_updates");
    }
  },

  PLUGINS_ANALYSIS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("analysis-plugins");
    }
  },

  RESOURCE_PROVIDERS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("resource-providers");
    }
  },

  RESOURCE_PROVIDERS_STATUS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("resource-providers", "_status");
    }
  },

  PLUGIN {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("plugin", "{}");
    }
  },

  PLUGIN_CONFIG {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("plugin", "{}", "cfg");
    }
  },

  PLUGIN_SERVICE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("plugin", "{}", "service");
    }
  },

  DS_PLUGINS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource-plugins");
    }
  },

  DS_PLUGIN_FORM {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datasource-plugin", "{}", "form");
    }
  },

  IDENTIFIERS_MAPPINGS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("identifiers", "mappings");
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

  SERVICE_R {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r");
    }
  },

  SERVICE_R_CLUSTERS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r", "clusters");
    }
  },

  SERVICE_R_CLUSTER {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r", "cluster", "{}");
    }
  },

  SERVICE_R_CLUSTER_PACKAGES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r", "cluster", "{}", "packages");
    }
  },

  SERVICE_R_CLUSTER_PACKAGE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r", "cluster", "{}", "package", "{}");
    }
  },

  SERVICE_R_CLUSTER_PACKAGES_UPDATE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r", "cluster", "{}", "commands", "_update");
    }
  },

  SERVICE_R_CLUSTER_PACKAGE_INSTALL {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r", "cluster", "{}", "commands", "_install");
    }
  },

  SERVICE_R_CLUSTER_LOG {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r", "cluster", "{}", "_log");
    }
  },

  SERVICE_R_CLUSTER_SERVER {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r", "cluster", "{}", "server", "{}");
    }
  },

  SERVICE_R_CLUSTER_SERVER_LOG {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("service", "r", "cluster", "{}", "server", "{}", "_log");
    }
  },

  R_SESSIONS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("r", "sessions");
    }
  },

  R_SESSIONS_TEST {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("r", "sessions", "_test");
    }
  },

  DATASHIELD_PROFILES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "profiles");
    }
  },

  DATASHIELD_PROFILE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "profile", "{}");
    }
  },

  DATASHIELD_PROFILE_ENABLE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "profile", "{}", "_enable");
    }
  },

  DATASHIELD_PROFILE_ACCESS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "profile", "{}", "_access");
    }
  },

  DATASHIELD_PACKAGES {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "packages");
    }
  },

  DATASHIELD_PACKAGES_PUBLISH {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "packages", "_publish");
    }
  },

  DATASHIELD_PACKAGE {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "package", "{}");
    }
  },

  DATASHIELD_PACKAGE_PUBLISH {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "package", "{}", "_publish");
    }
  },

  DATASHIELD_PACKAGE_METHODS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("datashield", "package", "{}", "methods");
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
