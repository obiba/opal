/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.OrientDbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.orientechnologies.orient.core.metadata.schema.OType;

import static com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import static org.obiba.opal.core.domain.security.SubjectAcl.Subject;
import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

@Component
public class DefaultSubjectAclService implements SubjectAclService {

  private static final Logger log = LoggerFactory.getLogger(DefaultSubjectAclService.class);

  private final Set<SubjectAclChangeCallback> callbacks = Sets.newHashSet();

  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(SubjectAcl.class);
    orientDbService.createIndex(SubjectAcl.class, INDEX_TYPE.NOTUNIQUE, OType.STRING, "domain");
    orientDbService.createIndex(SubjectAcl.class, INDEX_TYPE.NOTUNIQUE, OType.STRING, "node");
    orientDbService.createIndex(SubjectAcl.class, INDEX_TYPE.NOTUNIQUE, OType.STRING, "principal");
    orientDbService.createIndex(SubjectAcl.class, INDEX_TYPE.NOTUNIQUE, OType.STRING, "type");
  }

  @Override
  public void stop() {

  }

  @Override
  public void addListener(SubjectAclChangeCallback callback) {
    if(callback != null) callbacks.add(callback);
  }

  @Override
  public void deleteNodePermissions(String node) {
    Iterable<SubjectAcl> subjectAcls = orientDbService
        .list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where node = ? or node like ?",
            node, node + "/%");
    Set<Subject> subjects = Sets.newTreeSet();
    for(SubjectAcl acl : subjectAcls) {
      subjects.add(acl.getSubject());
      delete(acl);
    }
    notifyListeners(subjects);
  }

  private void delete(SubjectAcl acl) {
    orientDbService.delete(acl);
  }

  @Override
  public void deleteNodePermissions(String domain, String node) {
    Set<Subject> subjects = Sets.newTreeSet();
    for(SubjectAcl acl : find(domain, node)) {
      subjects.add(acl.getSubject());
      delete(acl);
    }
    notifyListeners(subjects);
  }

  @Override
  public void deleteNodeHierarchyPermissions(String domain, String node) {
    deleteNodePermissions(domain, node);
    Set<Subject> subjects = Sets.newTreeSet();
    for(SubjectAcl acl : Iterables.concat(find(domain, node), findLike(domain, node + "/"))) {
      subjects.add(acl.getSubject());
      delete(acl);
    }
    notifyListeners(subjects);
  }

  @Override
  public void deleteSubjectPermissions(Subject subject) {
    for(SubjectAcl acl : find(subject)) {
      delete(acl);
    }
    notifyListeners(subject);
  }

  @Override
  public void deleteSubjectPermissions(String domain, String node, Subject subject) {
    for(SubjectAcl acl : find(domain, node, subject)) {
      delete(acl);
    }
    notifyListeners(subject);
  }

  @Override
  public void deleteSubjectPermissions(String domain, String node, Subject subject, String permission) {
    SubjectAcl acl = find(domain, node, subject, permission);
    if(acl != null) {
      delete(acl);
      notifyListeners(subject);
    }
  }

  @Override
  public void addSubjectPermissions(String domain, String node, Subject subject, Iterable<String> permissions) {
    for(String permission : permissions) {
      addSubjectPermission(domain, node, subject, permission);
    }
  }

  @Override
  public void addSubjectPermission(String domain, String node, @NotNull Subject subject, @NotNull String permission) {
    Assert.notNull(subject, "subject cannot be null");
    Assert.notNull(permission, "permission cannot be null");
    HasUniqueProperties acl = new SubjectAcl(domain, node, subject, permission);
    orientDbService.save(acl, acl);
    notifyListeners(subject);
  }

  @Override
  public Permissions getSubjectNodePermissions(@NotNull final String domain, @NotNull final String node,
      @NotNull final Subject subject) {
    Assert.notNull(node, "node cannot be null");
    Assert.notNull(subject, "subject cannot be null");

    return new Permissions() {

      @Override
      public String getDomain() {
        return domain;
      }

      @Override
      public String getNode() {
        return node;
      }

      @Override
      public Subject getSubject() {
        return subject;
      }

      @Override
      public Iterable<String> getPermissions() {
        return Iterables.transform(find(domain, node, subject), new Function<SubjectAcl, String>() {
          @Override
          public String apply(SubjectAcl from) {
            return from.getPermission();
          }
        });
      }
    };
  }

  @Override
  public Iterable<Permissions> getSubjectNodeHierarchyPermissions(@NotNull String domain, @NotNull String node,
      @NotNull Subject subject) {
    Map<String, Permissions> entries = Maps.newHashMap();

    for(SubjectAcl acl : Iterables.concat(find(domain, node, subject), findLike(domain, node + "/", subject))) {
      String key = acl.getSubject() + ":" + acl.getNode();
      PermissionsImpl perms = (PermissionsImpl) entries.get(key);
      if(perms == null) {
        perms = new PermissionsImpl(domain, acl.getNode(), acl.getSubject());
        entries.put(key, perms);
      }
      perms.addPermission(acl.getPermission());
    }

    return entries.values();
  }

  private Iterable<SubjectAcl> find(Subject subject) {
    return orientDbService
        .list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where principal = ? and type = ?",
            subject.getPrincipal(), subject.getType());
  }

  private Iterable<SubjectAcl> find(String domain, String node, SubjectType type) {
    return orientDbService.list(SubjectAcl.class,
        "select from " + SubjectAcl.class.getSimpleName() + " where domain = ? and node = ? and type = ?", domain, node,
        type);
  }

  private Iterable<SubjectAcl> findLike(String domain, String node, SubjectType type) {
    return orientDbService.list(SubjectAcl.class,
        "select from " + SubjectAcl.class.getSimpleName() + " where domain = ? and node like ? and type = ?", domain,
        node + "%", type);
  }

  private Iterable<SubjectAcl> find(String domain, SubjectType type) {
    return orientDbService
        .list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where domain = ? and type = ?",
            domain, type);
  }

  private Iterable<SubjectAcl> find(@NotNull String domain, @NotNull String node, @NotNull Subject subject) {
    return orientDbService.list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() +
        " where domain = ? and node = ? and principal = ? and type = ?", domain, node, subject.getPrincipal(),
        subject.getType());
  }

  private Iterable<SubjectAcl> findLike(@NotNull String domain, @NotNull String node, @NotNull Subject subject) {
    return orientDbService.list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() +
        " where domain = ? and node like ? and principal = ? and type = ?", domain, node + "%", subject.getPrincipal(),
        subject.getType());
  }

  private Iterable<SubjectAcl> find(@NotNull String domain, @NotNull String node) {
    return orientDbService.list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() +
        " where domain = ? and node = ?", domain, node);
  }

  private Iterable<SubjectAcl> findLike(@NotNull String domain, @NotNull String node) {
    return orientDbService.list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() +
        " where domain = ? and node like ?", domain, node + "%");
  }

  private SubjectAcl find(String domain, String node, Subject subject, String permission) {
    return orientDbService.findUnique(new SubjectAcl(domain, node, subject, permission));
  }

  @Override
  public Iterable<Permissions> getSubjectPermissions(final Subject subject) {
    return Iterables.transform(find(subject), new Function<SubjectAcl, Permissions>() {

      @Override
      public Permissions apply(final SubjectAcl from) {
        return new Permissions() {

          @Override
          public Subject getSubject() {
            return subject;
          }

          @Override
          public String getDomain() {
            return from.getDomain();
          }

          @Override
          public String getNode() {
            return from.getNode();
          }

          @Override
          public Iterable<String> getPermissions() {
            return Iterables
                .transform(find(from.getDomain(), getNode(), from.getSubject()), new Function<SubjectAcl, String>() {
                  @Override
                  public String apply(SubjectAcl from) {
                    return from.getPermission();
                  }
                });
          }
        };
      }

    });
  }

  @Override
  public Iterable<Permissions> getNodePermissions(String domain, String node, SubjectType type) {
    Map<Subject, Permissions> entries = Maps.newHashMap();

    for(SubjectAcl acl : type == null ? find(domain, node) : find(domain, node, type)) {
      PermissionsImpl perms = (PermissionsImpl) entries.get(acl.getSubject());
      if(perms == null) {
        perms = new PermissionsImpl(domain, node, acl.getSubject());
        entries.put(acl.getSubject(), perms);
      }
      perms.addPermission(acl.getPermission());
    }

    return entries.values();
  }

  @Override
  public Iterable<Permissions> getNodeHierarchyPermissions(String domain, String node, SubjectType type) {
    Map<String, Permissions> entries = Maps.newHashMap();

    Iterable<SubjectAcl> acls = type == null
        ? Iterables.concat(find(domain, node), findLike(domain, node + "/"))
        : Iterables.concat(find(domain, node, type), findLike(domain, node + "/", type));

    for(SubjectAcl acl : acls) {
      String key = acl.getSubject() + ":" + acl.getNode();
      PermissionsImpl perms = (PermissionsImpl) entries.get(key);
      if(perms == null) {
        perms = new PermissionsImpl(domain, acl.getNode(), acl.getSubject());
        entries.put(key, perms);
      }
      perms.addPermission(acl.getPermission());
    }

    return entries.values();
  }

  @Override
  public Iterable<Subject> getSubjects(String domain, SubjectType type) {
    return FluentIterable.from(find(domain, type)).transform(new Function<SubjectAcl, Subject>() {

      @Override
      public Subject apply(SubjectAcl from) {
        return from.getSubject();
      }

    }).filter(new Predicate<Subject>() {
      final Collection<Subject> set = new TreeSet<>();

      @Override
      public boolean apply(Subject input) {
        // add returns false if the set already contains the element
        return set.add(input);
      }
    });
  }

  /**
   * @param subjects
   */
  private void notifyListeners(Iterable<Subject> subjects) {
    for(Subject s : subjects)
      notifyListeners(s);
  }

  /**
   * @param subject
   */
  private void notifyListeners(Subject subject) {
    for(SubjectAclChangeCallback c : callbacks) {
      try {
        c.onSubjectAclChanged(subject);
      } catch(Exception e) {
        log.warn("Ignoring exception during ACL callback", e);
      }
    }
  }

  private static class PermissionsImpl implements Permissions {

    private final String domain;

    private final String node;

    private final Subject subject;

    private final List<String> permissions = Lists.newArrayList();

    private PermissionsImpl(String domain, String node, Subject subject) {
      this.domain = domain;
      this.node = node;
      this.subject = subject;
    }

    @Override
    public String getDomain() {
      return domain;
    }

    @Override
    public String getNode() {
      return node;
    }

    @Override
    public Subject getSubject() {
      return subject;
    }

    @Override
    public Iterable<String> getPermissions() {
      return permissions;
    }

    public void addPermission(String permission) {
      if(!permissions.contains(permission)) {
        permissions.add(permission);
      }
    }
  }

}
