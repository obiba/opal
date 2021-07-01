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

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.domain.security.SubjectToken;
import org.obiba.opal.core.service.SubjectTokenService;
import org.obiba.opal.web.model.Opal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A authorizer that applies a posteriori restrictions to permissions granted to a {@link SubjectToken}.
 */
public class OpalModularRealmAuthorizer extends ModularRealmAuthorizer {

  private static final Logger log = LoggerFactory.getLogger(OpalModularRealmAuthorizer.class);

  private static final Collection<String> EDIT_METHODS = Sets.newHashSet("PUT", "DELETE", "POST");

  private static final Collection<String> SQL_ACTIONS = Sets.newHashSet("_sql", "_rsql");

  private static final Collection<String> READ_NO_VALUES_COMMANDS = Sets.newHashSet("analyse", "report");

  private static final Collection<String> READ_COMMANDS = Sets.newHashSet("export", "export_vcf", "analyse", "report", "backup");

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
    if (node.equals("/projects") && EDIT_METHODS.contains(action))
      return "POST".equals(action) && isCreateProjectPermitted(principals);

    if (node.startsWith("/project/") || node.startsWith("/datasource/"))
      return isProjectActionPermitted(principals, node, action);

    // cannot bypass projects to launch a command
    if (node.startsWith("/shell/commands") && EDIT_METHODS.contains(action)) return false;

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
        || node.startsWith("/plugin")) && EDIT_METHODS.contains(action))
      return isSystemAdministrationPermitted(principals, node);

    if (node.startsWith("/files/projects")) {
      return isProjectFilesPermitted(principals, node, action);
    }

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

    if (elems.length == 3 && EDIT_METHODS.contains(action)) {
      // cannot modify datasource directly
      if ("datasource".equals(elems[1])) return false;
      // may modify/delete a project
      switch (action) {
        case "PUT": return isUpdateProjectPermitted(principals);
        case "DELETE": return isDeleteProjectPermitted(principals);
      }
      return false;
    }

    if (elems.length == 4 && "datasource".equals(elems[1]) && SQL_ACTIONS.contains(elems[3]))
      return isUsingSQLPermitted(principals);

    if ((isReadOnlyNoValues(principals) || isReadOnly(principals)) && EDIT_METHODS.contains(action))
      return false;

    if (isReadOnlyNoValues(principals) && "datasource".equals(elems[1]) && (node.endsWith("/valueSets") || node.contains("/valueSet/")))
      return false;

    return true;
  }

  private boolean isProjectFilesPermitted(PrincipalCollection principals, String node, String action) {
    String[] elems = node.split("/");
    if (elems.length < 4) return true;

    String project = elems[3];
    log.trace(" Token project files ={}", project);

    boolean projectAccessible;
    Collection<String> projectRestrictions = getProjectRestrictions(principals);
    if (projectRestrictions.isEmpty())
      projectAccessible = true;
    else
      projectAccessible = projectRestrictions.contains(project);
    if (!projectAccessible) return false;

    if (!hasAccessLimitation(principals))
      return true;
    if (isReadOnly(principals))
      return !EDIT_METHODS.contains(action);
    return !isReadOnlyNoValues(principals);
  }

  private boolean isProjectCommandPermitted(PrincipalCollection principals, String node) {
    Collection<String> projectRestrictions = getProjectRestrictions(principals);
    if (projectRestrictions.isEmpty()) {
      for (String cmd : getFilteredCommands(principals)) {
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

  private boolean isReadOnlyNoValues(PrincipalCollection principals) {
    return Opal.SubjectTokenDto.AccessType.READ_NO_VALUES.name().equals(getToken(principals).getAccess());
  }

  private boolean isReadOnly(PrincipalCollection principals) {
    return Opal.SubjectTokenDto.AccessType.READ.name().equals(getToken(principals).getAccess());
  }

  private boolean hasAccessLimitation(PrincipalCollection principals) {
    return !Strings.isNullOrEmpty(getToken(principals).getAccess());
  }

  private List<String> getProjectCommands(PrincipalCollection principals) {
    List<String> projectCmds = Lists.newArrayList();
    SubjectToken token = getToken(principals);
    Set<String> commands = getFilteredCommands(principals);
    for (String project : token.getProjects()) {
      for (String cmd : commands) {
        projectCmds.add("/project/" + project + "/commands/_" + cmd);
      }
    }
    return projectCmds;
  }

  /**
   * Get selected commands, modulo read access restriction (if defined).
   *
   * @param principals
   * @return
   */
  private Set<String> getFilteredCommands(PrincipalCollection principals) {
    SubjectToken token = getToken(principals);
    boolean readNoValues = isReadOnlyNoValues(principals);
    boolean read = isReadOnly(principals);
    return token.getCommands().stream()
        .filter(cmd -> (!readNoValues && !read) // can read/write
            || (readNoValues && READ_NO_VALUES_COMMANDS.contains(cmd))  // can read without values, not write
            || (read && READ_COMMANDS.contains(cmd))) // can read, not write
        .collect(Collectors.toSet());
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
    return getToken(principals).isUseR() && !isReadOnlyNoValues(principals);
  }

  private boolean isUsingDatashieldPermitted(PrincipalCollection principals) {
    return getToken(principals).isUseDatashield();
  }

  private boolean isUsingSQLPermitted(PrincipalCollection principals) {
    return getToken(principals).isUseSQL() && !isReadOnlyNoValues(principals);
  }

  private boolean isSystemAdministrationPermitted(PrincipalCollection principals, String node) {
    boolean sysAdmin = getToken(principals).isSystemAdmin();
    if (sysAdmin) return true;

    // pattern for managing own settings
    return node.contains("/_current");
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
