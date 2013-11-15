/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SubjectAclService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.orientechnologies.orient.core.metadata.schema.OType;

import static com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;

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
  public void deleteNodePermissions(final String node) {
    Set<SubjectAclService.Subject> subjects = Sets.newTreeSet();

    // temp workaround waiting for https://github.com/orientechnologies/orientdb/issues/1824
    Iterable<SubjectAcl> subjectAcls = Iterables
        .filter(orientDbService.list(SubjectAcl.class), new Predicate<SubjectAcl>() {
          @Override
          public boolean apply(SubjectAcl acl) {
            return acl.getNode() != null && (acl.getNode().equals(node) || acl.getNode().startsWith("/%"));
          }
        });
//    Iterable<SubjectAcl> subjectAcls = Sets.newHashSet(Iterables.concat(orientDbService
//        .<SubjectAcl>list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where node = ?",
//            node), orientDbService
//        .<SubjectAcl>list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where node like = ?",
//            node + "/%")));
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
    Set<SubjectAclService.Subject> subjects = Sets.newTreeSet();
    for(SubjectAcl acl : find(domain, node)) {
      subjects.add(acl.getSubject());
      delete(acl);
    }
    notifyListeners(subjects);
  }

  @Override
  public void deleteSubjectPermissions(String domain, String node, SubjectAclService.Subject subject) {
    for(SubjectAcl acl : find(domain, node, subject)) {
      delete(acl);
    }
    notifyListeners(subject);
  }

  @Override
  public void deleteSubjectPermissions(String domain, String node, SubjectAclService.Subject subject,
      String permission) {
    SubjectAcl acl = find(domain, node, subject, permission);
    if(acl != null) {
      delete(acl);
      notifyListeners(subject);
    }
  }

  @Override
  public void addSubjectPermissions(String domain, String node, SubjectAclService.Subject subject,
      Iterable<String> permissions) {
    for(String permission : permissions) {
      addSubjectPermission(domain, node, subject, permission);
    }
  }

  @Override
  public void addSubjectPermission(String domain, String node, @NotNull SubjectAclService.Subject subject,
      @NotNull String permission) {
    Assert.notNull(subject, "subject cannot be null");
    Assert.notNull(permission, "permission cannot be null");
    HasUniqueProperties acl = new SubjectAcl(domain, node, subject, permission);
    orientDbService.save(acl, acl);
    notifyListeners(subject);
  }

  @Override
  public Permissions getSubjectPermissions(@NotNull final String domain, @NotNull final String node,
      @NotNull final SubjectAclService.Subject subject) {
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

  private Iterable<SubjectAcl> find(final SubjectAclService.Subject subject) {
    // temp workaround waiting for https://github.com/orientechnologies/orientdb/issues/1824
    return Iterables.filter(orientDbService.list(SubjectAcl.class), new Predicate<SubjectAcl>() {
      @Override
      public boolean apply(SubjectAcl acl) {
        return Objects.equal(acl.getPrincipal(), subject.getPrincipal()) && //
            Objects.equal(acl.getType(), subject.getType().toString());
      }
    });
//    return orientDbService
//        .list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where principal = ? and type = ?",
//            subject.getPrincipal(), subject.getType().toString());
  }

  private Iterable<SubjectAcl> find(final String domain, final String node, final SubjectType type) {
    // temp workaround waiting for https://github.com/orientechnologies/orientdb/issues/1824
    return Iterables.filter(orientDbService.list(SubjectAcl.class), new Predicate<SubjectAcl>() {
      @Override
      public boolean apply(SubjectAcl acl) {
        return Objects.equal(acl.getDomain(), domain) && //
            Objects.equal(acl.getNode(), node) && //
            Objects.equal(acl.getType(), type.toString());
      }
    });
//    return orientDbService.list(SubjectAcl.class,
//        "select from " + SubjectAcl.class.getSimpleName() + " where domain = ? and node = ? and type = ?", domain, node,
//        type.toString());
  }

  private Iterable<SubjectAcl> find(final String domain, final SubjectType type) {
    // temp workaround waiting for https://github.com/orientechnologies/orientdb/issues/1824
    return Iterables.filter(orientDbService.list(SubjectAcl.class), new Predicate<SubjectAcl>() {
      @Override
      public boolean apply(SubjectAcl acl) {
        return Objects.equal(acl.getDomain(), domain) && //
            Objects.equal(acl.getType(), type.toString());
      }
    });
//    return orientDbService
//        .list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where domain = ? and type = ?",
//            domain, type.toString());
  }

  private Iterable<SubjectAcl> find(final String domain, final String node, final SubjectAclService.Subject subject) {
    // temp workaround waiting for https://github.com/orientechnologies/orientdb/issues/1824
    return Iterables.filter(orientDbService.list(SubjectAcl.class), new Predicate<SubjectAcl>() {
      @Override
      public boolean apply(SubjectAcl acl) {
        return Objects.equal(acl.getDomain(), domain) && //
            Objects.equal(acl.getNode(), node) && //
            Objects.equal(acl.getPrincipal(), subject.getPrincipal()) && //
            Objects.equal(acl.getType(), subject.getType().toString());
      }
    });
//    return orientDbService.list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() +
//        " where domain = ? and node = ? and principal = ? and type = ?", domain, node, subject.getPrincipal(),
//        subject.getType().toString());
  }

  private Iterable<SubjectAcl> find(final String domain, final String node) {
    // temp workaround waiting for https://github.com/orientechnologies/orientdb/issues/1824
    return Iterables.filter(orientDbService.list(SubjectAcl.class), new Predicate<SubjectAcl>() {
      @Override
      public boolean apply(SubjectAcl acl) {
        return Objects.equal(acl.getDomain(), domain) && //
            Objects.equal(acl.getNode(), node);
      }
    });
    //    return orientDbService.list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() +
//        " where domain = ? and node = ?", domain, node);
  }

  private SubjectAcl find(String domain, String node, SubjectAclService.Subject subject, String permission) {
    return orientDbService.findUnique(new SubjectAcl(domain, node, subject, permission));
  }

  @Override
  public Iterable<Permissions> getSubjectPermissions(final SubjectAclService.Subject subject) {
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

    return Iterables.transform(find(domain, node, type), new Function<SubjectAcl, Permissions>() {

      @Override
      public Permissions apply(final SubjectAcl from) {
        return new Permissions() {

          @Override
          public String getNode() {
            return from.getNode();
          }

          @Override
          public String getDomain() {
            return from.getDomain();
          }

          @Override
          public Subject getSubject() {
            return from.getSubject();
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
  public Iterable<SubjectAclService.Subject> getSubjects(String domain, SubjectType type) {
    return FluentIterable.from(find(domain, type)).transform(new Function<SubjectAcl, SubjectAclService.Subject>() {

      @Override
      public SubjectAclService.Subject apply(SubjectAcl from) {
        return from.getSubject();
      }

    }).filter(new Predicate<SubjectAclService.Subject>() {
      final Collection<Subject> set = new TreeSet<SubjectAclService.Subject>();

      @Override
      public boolean apply(SubjectAclService.Subject input) {
        // add returns false if the set already contains the element
        return set.add(input);
      }
    });
  }

  /**
   * @param subjects
   */
  private void notifyListeners(Iterable<Subject> subjects) {
    for(SubjectAclService.Subject s : subjects)
      notifyListeners(s);
  }

  /**
   * @param subject
   */
  private void notifyListeners(SubjectAclService.Subject subject) {
    for(SubjectAclChangeCallback c : callbacks) {
      try {
        c.onSubjectAclChanged(subject);
      } catch(Exception e) {
        log.warn("Ignoring exception during ACL callback", e);
      }
    }
  }

}
