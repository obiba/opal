/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.resource;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.security.BackgroundJobServiceAuthToken;
import org.obiba.opal.core.service.ResourceReferenceService;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.*;
import org.obiba.opal.spi.r.datasource.magma.MagmaRRuntimeException;
import org.obiba.opal.spi.r.datasource.magma.RVariableHelper;
import org.obiba.opal.spi.r.resource.IRTabularResourceConnector;
import org.obiba.opal.spi.resource.TabularResourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class RTabularResourceConnector implements TabularResourceConnector, IRTabularResourceConnector {

  private static final Logger log = LoggerFactory.getLogger(RTabularResourceConnector.class);

  private static final String RESOURCE_CLIENT_SYMBOL = ".client";

  private static final String TIBBLE_SYMBOL = "tbl";

  // turns a resource reference to a resource
  private final ResourceReferenceService resourceReferenceService;

  // for creating the managed R session
  private final OpalRSessionManager rSessionManager;

  // the project where the resource reference is defined
  private final String project;

  // the resource reference name
  private final String name;

  // FIXME R server profile, not sure it is needed
  private final String profile;

  // the R session in which the resource is assigned, with its tabular representation
  private RServerSession rSession;

  // column descriptions
  private List<Column> columns;

  public RTabularResourceConnector(ResourceReferenceService resourceReferenceService, OpalRSessionManager rSessionManager, String project, String name, String profile) {
    this.resourceReferenceService = resourceReferenceService;
    this.rSessionManager = rSessionManager;
    this.project = project;
    this.name = name;
    this.profile = profile;
  }

  @Override
  public String getSymbol() {
    return TIBBLE_SYMBOL;
  }

  @Override
  public List<Column> getColumns() {
    if (columns == null) {
      RServerResult columnDescs = execute(String.format("lapply(colnames(%s), function(col) { " +
          "attrs <- attributes(%s[[col]]) ; " +
          "attrs$labels_names <- names(attrs$labels) ; " +
          "list(name=col, class=class(%s[[col]]), type=tibble::type_sum(%s[[col]]), attributes=attrs) " +
          "})", TIBBLE_SYMBOL, TIBBLE_SYMBOL, TIBBLE_SYMBOL, TIBBLE_SYMBOL));
      int i = 0;
      columns = Lists.newArrayList();
      for (RServerResult desc : columnDescs.asList()) {
        columns.add(new ColumnDescription(desc, i));
        i++;
      }
    }
    return columns;
  }

  @Override
  public boolean hasColumn(String name) {
    return getColumns().stream().anyMatch(col -> col.getName().equals(name));
  }

  @Override
  public Column getColumn(String name) {
    return getColumns().stream().filter(col -> col.getName().equals(name)).findFirst().get();
  }

  @Override
  public void dispose() {
    if (rSession != null) {
      rSessionManager.removeRSession(rSession.getId());
      rSession = null;
      columns = null;
    }
  }

  @Override
  public synchronized void initialise() {
    if (rSession != null) return;
    // TODO R server profile
    rSession = rSessionManager.newSubjectRSession(getSubject().getPrincipal().toString(), Strings.isNullOrEmpty(profile) ?
        null :
        new RServerProfile() {
          @Override
          public String getName() {
            return profile;
          }

          @Override
          public String getCluster() {
            return profile;
          }
        });
    rSession.setExecutionContext("View");
    ResourceAssignROperation rop = resourceReferenceService.asAssignOperation(project, name, RESOURCE_CLIENT_SYMBOL);
    rSession.execute(rop);
    ResourceTibbleAssignROperation rop2 = new ResourceTibbleAssignROperation(TIBBLE_SYMBOL, RESOURCE_CLIENT_SYMBOL);
    rSession.execute(rop2);
  }

  private Subject getSubject() {
    if (SecurityUtils.getSubject().isAuthenticated()) return SecurityUtils.getSubject();

    // Login as background task user
    try {
      PrincipalCollection principals = SecurityUtils.getSecurityManager()
          .authenticate(BackgroundJobServiceAuthToken.INSTANCE).getPrincipals();
      return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
    } catch (AuthenticationException e) {
      log.warn("Failed to obtain system user credentials: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public RServerResult execute(String script) {
    return execute(new RScriptROperation(script, false));
  }

  RServerResult execute(ROperationWithResult rop) {
    return doExecute(rop).getResult();
  }

  private ROperationWithResult doExecute(ROperationWithResult rop) {
    try {
      getRSession().execute(rop);
    } catch (Exception e) {
      log.error("R operation failed: {}", e.getMessage(), e);
      throw new MagmaRRuntimeException(e.getMessage());
    }
    return rop;
  }

  private RServerSession getRSession() {
    ensureRSession();
    return rSession;
  }

  private void ensureRSession() {
    if (rSession == null || rSession.isClosed()) {
      initialise();
    }
  }

  private class ColumnDescription implements Column {

    private final RNamedList<RServerResult> desc;

    private final int position;

    public ColumnDescription(RServerResult desc, int position) {
      this.desc = desc.asNamedList();
      this.position = position;
    }

    @Override
    public String getName() {
      return desc.get("name").asStrings()[0];
    }

    @Override
    public int getPosition() {
      return position;
    }

    @Override
    public List<Value> asVector(ValueType valueType) {
      RServerResult vector = execute(String.format("%s$`%s`", TIBBLE_SYMBOL, getName()));
      if (vector.isList()) {
        return vector.asList().stream()
            .map(val -> valueType.valueOf(val.asNativeJavaObject()))
            .collect(Collectors.toList());
      } else {
        return Lists.newArrayList();
      }
    }

    @Override
    public Variable asVariable(String entityType) {
      return RVariableHelper.newVariable(desc, entityType, false, "en", position);
    }

  }
}
