/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.presenter;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.rest.client.UriBuilder;

public final class PermissionResources {

  private PermissionResources() {}

  public static String projectPermissions(@Nonnull String project) {
    return baseUri(project).segment("project").build();
  }

  public static String datasourcePermissions(@Nonnull String project) {
    return baseUri(project).segment("datasource").build();
  }

  public static String tablePermissions(@Nonnull String project, @Nonnull String table) {
    return baseUri(project).segment("table", table).build();
  }

  public static UriBuilder variablePermissions(@Nonnull String project, @Nonnull String table,
      @Nonnull String variable) {
    return UriBuilder.create().fromPath(tablePermissions(project, table)).segment("variable", variable);
  }

  public static String reportTemplatePermissions(@Nonnull String project, @Nonnull String report) {
    return baseUri(project).segment("report-template", report).build();
  }

  private static UriBuilder baseUri(String project) {
    return UriBuilder.create().segment("project", project, "permissions");
  }

}
