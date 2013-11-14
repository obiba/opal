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

  PROJECT_COMMANDS_COPY {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("project", "{}", "commands", "_copy");
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

  USERS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("users");

    }
  },

  GROUPS {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("groups");

    }
  },

  USER {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("user", "{}");

    }
  },

  GROUP {
    @Override
    public UriBuilder create() {
      return UriBuilder.create().segment("group", "{}");

    }
  };

  public abstract UriBuilder create();
}
