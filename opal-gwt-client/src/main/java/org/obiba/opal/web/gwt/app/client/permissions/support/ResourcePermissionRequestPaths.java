/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.support;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.rest.client.UriBuilder;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class ResourcePermissionRequestPaths {

  private ResourcePermissionRequestPaths() {}

  public static final String PRINCIPAL_QUERY_PARAM = "principal";

  public static final String TYPE_QUERY_PARAM = "type";

  public static final String PERMISSION_QUERY_PARAM = "permission";

  public static String projectPermissions(@Nonnull String project) {
    return baseUri(project).segment("project").build();
  }

  public static String projectSubjects(@Nonnull String project) {
    return baseUri(project).segment("subjects").build();
  }

  public static String projectSubject(@Nonnull String project, @Nonnull String subject) {
    return baseUri(project).segment("subject", subject).build();
  }

  public static String projectNode(ResourcePermissionType type, @Nonnull String project, @Nonnull String nodePath) {
    return baseUri(project).fromPath(normalizeNodePath(type, nodePath)).build();
  }

  public static String datasourcePermissions(@Nonnull String project) {
    return baseUri(project).segment("datasource").build();
  }

  public static String tablePermissions(@Nonnull String project, @Nonnull String table) {
    return baseUri(project).segment("table", table).build();
  }

  public static String variablePermissions(@Nonnull String project, @Nonnull String table, @Nonnull String variable) {
    return UriBuilder.create().fromPath(tablePermissions(project, table)).segment("variable", variable).build();
  }

  public static String reportTemplatePermissions(@Nonnull String project, @Nonnull String report) {
    return baseUri(project).segment("report-template", report).build();
  }

  private static UriBuilder baseUri(String project) {
    return UriBuilder.create().segment("project", project, "permissions");
  }

  private static String normalizeNodePath(ResourcePermissionType type, String nodePath) {
    switch(type) {
      case PROJECT:
        return "/project";
      case DATASOURCE:
        return "/datasource";
      case VARIABLE:
      case TABLE: {
        int i = nodePath.indexOf("/table");
        return i == -1 ? nodePath : nodePath.substring(i);
      }
      case REPORT_TEMPLATE:
        int i = nodePath.indexOf("/report-template");
        return i == -1 ? nodePath : nodePath.substring(i);
      default:
        return nodePath;
    }
  }

}
