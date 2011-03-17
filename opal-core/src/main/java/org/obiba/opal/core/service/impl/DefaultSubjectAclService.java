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

import org.obiba.core.service.PersistenceManager;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.SubjectAclService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Component
public class DefaultSubjectAclService implements SubjectAclService {

  private static final Logger log = LoggerFactory.getLogger(DefaultSubjectAclService.class);

  private final PersistenceManager persistenceManager;

  private final Set<SubjectAclChangeCallback> callbacks = Sets.newHashSet();

  @Autowired
  public DefaultSubjectAclService(@Qualifier("opal-data") PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  @Override
  public void addListener(SubjectAclChangeCallback callback) {
    if(callback != null) {
      callbacks.add(callback);
    }
  }

  @Override
  public void deleteNodePermissions(String domain, String node) {
    Set<SubjectAclService.Subject> subjects = Sets.newTreeSet();
    for(SubjectAcl acl : persistenceManager.match(new SubjectAcl(domain, node))) {
      subjects.add(acl.getSubject());
      persistenceManager.delete(acl);
    }
    notifyListeners(subjects);
  }

  @Override
  public void deleteSubjectPermissions(String domain, String node, SubjectAclService.Subject subject) {
    for(SubjectAcl acl : persistenceManager.match(new SubjectAcl(domain, node, subject))) {
      persistenceManager.delete(acl);
    }
    notifyListeners(subject);
  }

  @Override
  public void addSubjectPermissions(String domain, String node, SubjectAclService.Subject subject, Iterable<String> permissions) {
    for(String permission : permissions) {
      addSubjectPermission(domain, node, subject, permission);
    }
  }

  @Override
  public void addSubjectPermission(String domain, String node, SubjectAclService.Subject subject, String permission) {
    if(subject == null) throw new IllegalArgumentException("subject cannot be null");
    if(permission == null) throw new IllegalArgumentException("permission cannot be null");
    persistenceManager.save(new SubjectAcl(domain, node, subject, permission));
    notifyListeners(subject);
  }

  @Override
  public Permissions getSubjectPermissions(final String domain, final String node, final SubjectAclService.Subject subject) {
    if(node == null) throw new IllegalArgumentException("node cannot be null");
    if(subject == null) throw new IllegalArgumentException("subject cannot be null");

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
        return mergePermissions(new SubjectAcl(domain, node, subject));
      }
    };
  }

  @Override
  public Iterable<Permissions> getSubjectPermissions(final SubjectAclService.Subject subject) {

    SubjectAcl template = new SubjectAcl(subject);
    return Iterables.transform(persistenceManager.match(template), new Function<SubjectAcl, Permissions>() {

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
            return mergePermissions(new SubjectAcl(from.getDomain(), getNode(), from.getSubject()));
          }

        };
      }

    });
  }

  @Override
  public Iterable<Permissions> getNodePermissions(final String domain, final String node, final SubjectType type) {

    SubjectAcl template = new SubjectAcl(domain, node, type);
    return Iterables.transform(persistenceManager.match(template), new Function<SubjectAcl, Permissions>() {

      @Override
      public Permissions apply(final SubjectAcl from) {
        return new Permissions() {

          @Override
          public String getNode() {
            return node;
          }

          @Override
          public String getDomain() {
            return domain;
          }

          @Override
          public Subject getSubject() {
            return from.getSubject();
          }

          @Override
          public Iterable<String> getPermissions() {
            return mergePermissions(new SubjectAcl(from.getDomain(), node, from.getSubject()));
          }

        };
      }

    });
  }

  @Override
  public Iterable<SubjectAclService.Subject> getSubjects(final String domain, final SubjectType type) {
    SubjectAcl template = new SubjectAcl(domain, null, type);

    return Iterables.filter(Iterables.transform(persistenceManager.match(template), new Function<SubjectAcl, SubjectAclService.Subject>() {

      @Override
      public SubjectAclService.Subject apply(SubjectAcl from) {
        return from.getSubject();
      }

    }), new Predicate<SubjectAclService.Subject>() {

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
  private void notifyListeners(Set<SubjectAclService.Subject> subjects) {
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

  private Iterable<String> mergePermissions(SubjectAcl template) {

    return Iterables.transform(persistenceManager.match(template), new Function<SubjectAcl, String>() {

      @Override
      public String apply(SubjectAcl from) {
        return from.getPermission();
      }

    });

  }

}
