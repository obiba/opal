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
import org.obiba.magma.*;
import org.obiba.opal.core.security.BackgroundJobServiceAuthToken;
import org.obiba.opal.core.service.ResourceReferenceService;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.*;
import org.obiba.opal.spi.r.datasource.magma.MagmaRRuntimeException;
import org.obiba.opal.spi.r.datasource.magma.RVariableEntity;
import org.obiba.opal.spi.r.datasource.magma.RVariableHelper;
import org.obiba.opal.spi.r.resource.IRTabularResourceConnector;
import org.obiba.opal.spi.resource.TabularResourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RTabularResourceConnector implements TabularResourceConnector, IRTabularResourceConnector {

  private static final Logger log = LoggerFactory.getLogger(RTabularResourceConnector.class);

  private static final String RESOURCE_UTILS_SCRIPT = ".resource.R";

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

  // R columns descriptions
  private List<Column> columns;

  protected final Lock lock = new ReentrantLock();

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
      RServerResult columnDescs = execute(String.format(".resource.get_columns(`%s`)", getSymbol()));
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
  public boolean isMultilines(String idColumn) {
    String cmd = String.format(".resource.is_multilines(%s, '%s')", getSymbol(), idColumn);
    RServerResult result = execute(cmd);
    return result.asLogical();
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
  public void initialise() {
    if (rSession != null) return;

    lock.lock();
    try {
      RServerProfile rServerProfile;
      if (Strings.isNullOrEmpty(profile)) {
        String profileName = resourceReferenceService.getProfile(project, name);
        rServerProfile = new RServerProfile() {
          @Override
          public String getName() {
            return profileName;
          }

          @Override
          public String getCluster() {
            return profileName;
          }
        };
      } else {
        rServerProfile = new RServerProfile() {
          @Override
          public String getName() {
            return profile;
          }

          @Override
          public String getCluster() {
            return profile;
          }
        };
      }
      rSession = rSessionManager.newSubjectRSession(getSubject().getPrincipal().toString(), rServerProfile);
      rSession.setExecutionContext(String.format("View [%s.%s]", project, name));
      // prepare R env with util functions
      rSession.execute(new SourceROperation(RESOURCE_UTILS_SCRIPT));
      // assign resource
      rSession.execute(resourceReferenceService.asAssignOperation(project, name, RESOURCE_CLIENT_SYMBOL));
      rSession.execute(new ResourceTibbleAssignROperation(TIBBLE_SYMBOL, RESOURCE_CLIENT_SYMBOL));
    } finally {
      lock.unlock();
    }
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

  //
  // Private methods
  //

  private ROperationWithResult doExecute(ROperationWithResult rop) {
    lock.lock();
    try {
      getRSession().execute(rop);
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.error("Resource R operation failed: {}", e.getMessage(), e);
      else
        log.error("Resource R operation failed: {}", e.getMessage());
      throw new MagmaRRuntimeException(e.getMessage());
    } finally {
      lock.unlock();
    }
    return rop;
  }

  private RServerSession getRSession() {
    ensureRSession();
    return rSession;
  }

  private void ensureRSession() {
    if (rSession == null || !isAlive()) {
      if (rSession != null) {
        dispose();
      }
      rSession = null;
      initialise();
    }
  }

  /**
   * Check the R session is functional: the connection can break because of many reasons, then executing a basic R
   * command is the safer approach.
   *
   * @return
   */
  private boolean isAlive() {
    try {
      ROperationWithResult rop = new RScriptROperation("base::ls()", false);
      rSession.execute(rop);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  //
  // Private classes
  //

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
    public int getLength(boolean distinct) {
      try {
        RServerResult result = execute(String.format("%s %%>%% distinct(`%s`) %%>%% summarise(n = n()) %%>%% pull()", TIBBLE_SYMBOL, getName()));
        return result.asIntegers()[0];
      } catch (Exception e) {
        log.warn("Resource vector length failed", e);
        return 0;
      }
    }

    @Override
    public List<Value> asVector(ValueType valueType, boolean distinct, int offset, int limit) {
      String cmd = distinct ? String.format("%s %%>%% distinct(`%s`)", TIBBLE_SYMBOL, getName()) : TIBBLE_SYMBOL;
      if (offset == 0 && limit < 0) {
        cmd = String.format("%s %%>%% pull(`%s`)", cmd, getName());
      } else {
        cmd = String.format("%s %%>%% filter(between(row_number(), %s, %s)) %%>%% pull(`%s`)",
            cmd, offset + 1, limit < 0 ? "n()" : offset + limit, getName());
      }
      RServerResult vector = execute(cmd);
      if (vector.isList()) {
        return vector.asList().stream()
            .map(val -> valueType.valueOf(val.asNativeJavaObject()))
            .collect(Collectors.toList());
      } else {
        return Lists.newArrayList();
      }
    }

    @Override
    public List<Value> asVector(ValueType valueType, String idColumn, Iterable<VariableEntity> entities) {
      String idsSymbol = "ids_" + Math.abs(new Random().nextInt());
      assignIds(entities, idsSymbol);
      String cmd = String.format("%s %%>%% filter(`%s` %%in%% %s) %%>%% pull(`%s`)", TIBBLE_SYMBOL, idColumn, idsSymbol, getName());
      RServerResult vector = execute(cmd);
      rmIds(idsSymbol);
      if (vector.isList()) {
        return vector.asList().stream()
            .map(val -> valueType.valueOf(val.asNativeJavaObject()))
            .collect(Collectors.toList());
      } else {
        return Lists.newArrayList();
      }
    }

    @Override
    public Variable asVariable(String entityType, boolean multilines) {
      Variable variable = RVariableHelper.newVariable(desc, entityType, multilines, "en", position);
      return Variable.Builder.sameAs(variable)
          .addAttribute(Attribute.Builder.newAttribute("column").withNamespace("opal").withValue(getName()).build())
          .build();
    }

    //
    // Private methods
    //

    private void assignIds(Iterable<VariableEntity> entities, String idsSymbol) {
      String idsVector = StreamSupport.stream(entities.spliterator(), false)
          .map(e -> (RVariableEntity) e)
          .map(e -> e.isNumeric() ? e.getRIdentifier() : String.format("\"%s\"", e.getRIdentifier()))
          .collect(Collectors.joining(","));
      idsVector = String.format("c(%s)", idsVector);
      execute(String.format("base::assign(\"%s\", %s)", idsSymbol, idsVector));
    }

    private void rmIds(String idsSymbol) {
      execute(String.format("base::rm(\"%s\")", idsSymbol));
    }

  }
}
