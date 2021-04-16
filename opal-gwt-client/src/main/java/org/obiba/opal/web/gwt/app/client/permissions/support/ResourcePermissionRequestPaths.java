/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.support;

import org.obiba.opal.web.gwt.rest.client.UriBuilder;

public final class ResourcePermissionRequestPaths {

  private ResourcePermissionRequestPaths() {}

  public static final String PRINCIPAL_QUERY_PARAM = "principal";

  public static final String TYPE_QUERY_PARAM = "type";

  public static final String PERMISSION_QUERY_PARAM = "permission";

  public enum UriBuilders {

    SYSTEM_PERMISSIONS_ADMINISTRATION {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("system", "permissions", "administration");
      }
    },

    SYSTEM_PERMISSIONS_R {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("system", "permissions", "r");
      }
    },

    SYSTEM_PERMISSIONS_DATASHIELD {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("system", "permissions", "datashield");
      }
    },

    PROJECT_PERMISSIONS_DATASOURCE {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "datasource");
      }
    },

    PROJECT_PERMISSIONS_PROJECT {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "project");
      }
    },

    PROJECT_PERMISSIONS_REPORTTEMPLATE {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "report-template", "{}");
      }
    },

    PROJECT_PERMISSIONS_SUBJECTS {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "subjects");
      }
    },

    PROJECT_PERMISSIONS_SUBJECT {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "subject", "{}");
      }
    },

    PROJECT_PERMISSIONS_TABLE {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "table", "{}");
      }
    },

    PROJECT_PERMISSIONS_TABLE_VARIABLE {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "table", "{}", "variable", "{}");
      }
    },

    PROJECT_VCF_PERMISSIONS_STORE {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "vcf-store");
      }
    },

    PROJECT_PERMISSIONS_RESOURCES {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "resources");
      }
    },

    PROJECT_PERMISSIONS_RESOURCE {
      @Override
      public UriBuilder create() {
        return UriBuilder.create().segment("project", "{}", "permissions", "resource", "{}");
      }
    };

    public abstract UriBuilder create();
  }

}
