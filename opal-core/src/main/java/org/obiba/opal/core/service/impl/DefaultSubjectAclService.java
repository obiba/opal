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

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.SubjectAclService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Function;
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
  private OrientDbDocumentService orientDbDocumentService;

  @Override
  @PostConstruct
  public void start() {
    orientDbDocumentService.createIndex(SubjectAcl.class, "domain", INDEX_TYPE.NOTUNIQUE, OType.STRING);
    orientDbDocumentService.createIndex(SubjectAcl.class, "node", INDEX_TYPE.NOTUNIQUE, OType.STRING);
    orientDbDocumentService.createIndex(SubjectAcl.class, "principal", INDEX_TYPE.NOTUNIQUE, OType.STRING);
    orientDbDocumentService.createIndex(SubjectAcl.class, "type", INDEX_TYPE.NOTUNIQUE, OType.STRING);
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
    Set<SubjectAclService.Subject> subjects = Sets.newTreeSet();
    Iterable<SubjectAcl> subjectAcls = Sets.newHashSet(Iterables.concat(orientDbDocumentService
        .<SubjectAcl>list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where node = ?",
            node), orientDbDocumentService
        .<SubjectAcl>list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where node like = ?",
            node + "/%")));
    for(SubjectAcl acl : subjectAcls) {
      subjects.add(acl.getSubject());
      delete(acl);
    }
    notifyListeners(subjects);
  }

  private void delete(SubjectAcl acl) {
    orientDbDocumentService.delete("select from " + SubjectAcl.class.getSimpleName() +
        " where domain = ? and node = ? and principal = ? and type = ? and permission = ?", acl.getDomain(),
        acl.getNode(), acl.getPrincipal(), acl.getType(), acl.getPermission());
  }

  @Override
  public void deleteNodePermissions(String domain, String node) {
    Set<SubjectAclService.Subject> subjects = Sets.newTreeSet();
    Iterable<SubjectAcl> list = orientDbDocumentService
        .list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where domain = ? and node = ?",
            domain, node);
    for(SubjectAcl acl : list) {
      subjects.add(acl.getSubject());
      delete(acl);
    }
    notifyListeners(subjects);
  }

  @Override
  public void deleteSubjectPermissions(String domain, String node, SubjectAclService.Subject subject) {

    for(SubjectAcl acl : orientDbDocumentService.list(SubjectAcl.class,
        "select from " + SubjectAcl.class.getSimpleName() + " where domain = ? and node = ? and subject = ?", domain,
        node, subject)) {
      delete(acl);
    }
    notifyListeners(subject);
  }

  @Override
  public void deleteSubjectPermissions(String domain, String node, SubjectAclService.Subject subject,
      String permission) {

    for(SubjectAcl acl : orientDbDocumentService
        .list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() +
            " where domain = ? and node = ? and subject = ? and permission = ?", domain, node, subject, permission)) {
      delete(acl);
    }
    notifyListeners(subject);
  }

  @Override
  public void addSubjectPermissions(String domain, String node, SubjectAclService.Subject subject,
      Iterable<String> permissions) {
    for(String permission : permissions) {
      addSubjectPermission(domain, node, subject, permission);
    }
  }

  @Override
  public void addSubjectPermission(String domain, String node, @Nonnull SubjectAclService.Subject subject,
      @Nonnull String permission) {
    Assert.notNull(subject, "subject cannot be null");
    Assert.notNull(permission, "permission cannot be null");
    orientDbDocumentService.save(new SubjectAcl(domain, node, subject, permission));
    notifyListeners(subject);
  }

  @Override
  public Permissions getSubjectPermissions(@Nonnull final String domain, @Nonnull final String node,
      @Nonnull final SubjectAclService.Subject subject) {
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

  private Iterable<SubjectAcl> find(SubjectAclService.Subject subject) {
    return orientDbDocumentService
        .list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where principal = ? and type = ?",
            subject.getPrincipal(), subject.getType().toString());
  }

  private Iterable<SubjectAcl> find(String domain, String node, SubjectType type) {
    return orientDbDocumentService.list(SubjectAcl.class,
        "select from " + SubjectAcl.class.getSimpleName() + " where domain = ? and node = ? and type = ?", domain, node,
        type.toString());
  }

  private Iterable<SubjectAcl> find(String domain, SubjectType type) {
    return orientDbDocumentService
        .list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() + " where domain = ? and type = ?",
            domain, type.toString());
  }

  private Iterable<SubjectAcl> find(String domain, String node, SubjectAclService.Subject subject) {
    return orientDbDocumentService.list(SubjectAcl.class, "select from " + SubjectAcl.class.getSimpleName() +
        " where domain = ? and node = ? and principal = ? and type = ?", domain, node, subject.getPrincipal(),
        subject.getType().toString());
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

      final TreeSet<SubjectAclService.Subject> set = new TreeSet<SubjectAclService.Subject>();

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
