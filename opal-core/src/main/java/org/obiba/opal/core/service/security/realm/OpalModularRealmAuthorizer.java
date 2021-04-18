/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.realm;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.domain.security.SubjectToken;
import org.obiba.opal.core.service.SubjectTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * A authorizer that applies a posteriori restrictions to permissions granted to a {@link SubjectToken}.
 */
public class OpalModularRealmAuthorizer extends ModularRealmAuthorizer {

  private static final Logger log = LoggerFactory.getLogger(OpalModularRealmAuthorizer.class);

  private static final Collection<String> EDIT_ACTIONS = Sets.newHashSet("PUT", "DELETE", "POST");

  private final SubjectTokenService subjectTokenService;

  private final Pattern projectCmdPattern = Pattern.compile("^/project/\\w+/commands/_\\w+$");

  private final com.google.common.cache.Cache<String, SubjectToken> tokenCache = CacheBuilder.newBuilder()
      .expireAfterAccess(10, TimeUnit.SECONDS)
      .build();

  public OpalModularRealmAuthorizer(Collection<Realm> realms, SubjectTokenService subjectTokenService) {
    super(realms);
    this.subjectTokenService = subjectTokenService;
  }

  @Override
  public boolean isPermitted(PrincipalCollection principals, String permission) {
    boolean superPerm = super.isPermitted(principals, permission);
    return superPerm && isFromTokenRealm(principals) ? isTokenPermitted(principals, permission) : superPerm;
  }

  private boolean isTokenPermitted(PrincipalCollection principals, String permission) {
    log.trace("Token permission on {}", permission);
    String[] parts = permission.split(":");
    if (parts.length < 3) return true;

    String node = parts[1];
    String action = parts[2];

    if (projectCmdPattern.matcher(node).matches())
      return isProjectCommandPermitted(principals, node);

    if (node.equals("/datasources/_sql") || node.equals("/datasources/_rsql"))
      return isUsingSQLPermitted(principals);

    // cannot create a project using a token
    if (node.equals("/projects") && EDIT_ACTIONS.contains(action))
      return "POST".equals(action) && isCreateProjectPermitted(principals);

    if (node.startsWith("/project/") || node.startsWith("/datasource/"))
      return isProjectActionPermitted(principals, node, action);

    // cannot bypass projects to launch a command
    if (node.startsWith("/shell/commands") && EDIT_ACTIONS.contains(action)) return false;

    if (node.startsWith("/service/r/workspaces"))
      return isUsingRPermitted(principals) || isUsingDatashieldPermitted(principals);

    if (node.startsWith("/r/session"))
      return isUsingRPermitted(principals);

    if (node.startsWith("/datashield/session"))
      return isUsingDatashieldPermitted(principals);

    if ((node.startsWith("/system")
        || node.startsWith("/datashield/option") || node.startsWith("/datashield/package")
        || node.startsWith("/service")
        || node.startsWith("/identifiers/mapping")
        || node.startsWith("/plugin")) && EDIT_ACTIONS.contains(action))
      return isSystemAdministrationPermitted(principals, node);

    return true;
  }

  private boolean isProjectActionPermitted(PrincipalCollection principals, String node, String action) {
    String[] elems = node.split("/");
    if (elems.length < 3) return true;

    String project = elems[2];
    log.trace(" Token project ={} action={}", project, action);

    if ("datasource".equals(elems[1]) && isTransientDatasource(project)) return true;

    boolean projectAccessible;
    Collection<String> projectRestrictions = getProjectRestrictions(principals);
    if (projectRestrictions.isEmpty())
      projectAccessible = true;
    else
      projectAccessible = projectRestrictions.contains(project);
    if (!projectAccessible) return false;

    if (elems.length == 3 && EDIT_ACTIONS.contains(action)) {
      // cannot modify datasource directly
      if ("datasource".equals(elems[1])) return false;
      // may modify/delete a project
      switch (action) {
        case "PUT": return isUpdateProjectPermitted(principals);
        case "DELETE": return isDeleteProjectPermitted(principals);
      }
      return false;
    }

    if (elems.length == 4 && "datasource".equals(elems[1]) && ("_sql".equals(elems[3]) || "_rsql".equals(elems[3])))
      return isUsingSQLPermitted(principals);

    return true;
  }

  private boolean isProjectCommandPermitted(PrincipalCollection principals, String node) {
    Collection<String> projectRestrictions = getProjectRestrictions(principals);
    if (projectRestrictions.isEmpty()) {
      for (String cmd : getToken(principals).getCommands()) {
        if (node.endsWith("/commands/_" + cmd)) return true;
      }
      return false;
    } else
      return getProjectCommands(principals).contains(node);
  }

  private boolean isFromTokenRealm(PrincipalCollection principals) {
    return principals.getRealmNames().contains(OpalTokenRealm.TOKEN_REALM);
  }

  // Try to guess it is a transient datasource based on the UUID pattern.
  private boolean isTransientDatasource(String name) {
    try {
      UUID.fromString(name);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private Collection<String> getProjectRestrictions(PrincipalCollection principals) {
    return getToken(principals).getProjects();
  }

  private List<String> getProjectCommands(PrincipalCollection principals) {
    List<String> projectCmds = Lists.newArrayList();
    SubjectToken token = getToken(principals);
    if (token.getCommands().isEmpty()) return projectCmds;

    for (String project : token.getProjects()) {
      for (String cmd : token.getCommands()) {
        projectCmds.add("/project/" + project + "/commands/_" + cmd);
      }
    }
    return projectCmds;
  }

  private boolean isCreateProjectPermitted(PrincipalCollection principals) {
    return getToken(principals).isCreateProject();
  }

  private boolean isUpdateProjectPermitted(PrincipalCollection principals) {
    return getToken(principals).isUpdateProject();
  }

  private boolean isDeleteProjectPermitted(PrincipalCollection principals) {
    return getToken(principals).isDeleteProject();
  }

  private boolean isUsingRPermitted(PrincipalCollection principals) {
    return getToken(principals).isUseR();
  }

  private boolean isUsingDatashieldPermitted(PrincipalCollection principals) {
    return getToken(principals).isUseDatashield();
  }

  private boolean isUsingSQLPermitted(PrincipalCollection principals) {
    return getToken(principals).isUseSQL();
  }

  private boolean isSystemAdministrationPermitted(PrincipalCollection principals, String node) {
    boolean sysAdmin = getToken(principals).isSystemAdmin();
    if (sysAdmin) return true;

    // pattern for managing own settings
    if (node.contains("/_current")) return true;

    return false;
  }

  private SubjectToken getToken(PrincipalCollection principals) {
    SubjectToken token = tokenCache.getIfPresent(principals.toString());
    if (token == null) {
      String principal = principals.getPrimaryPrincipal().toString();
      String tk = principals.fromRealm(OpalTokenRealm.TOKEN_REALM).iterator().next().toString();
      token = subjectTokenService.getToken(tk, principal);
      tokenCache.put(principals.toString(), token);
    }
    return token;
  }

}
