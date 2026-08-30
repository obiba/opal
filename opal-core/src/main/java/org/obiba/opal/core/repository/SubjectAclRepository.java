/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.repository;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Read on every access check, which is why its columns are indexed individually as well as together.
 */
public interface SubjectAclRepository extends JpaRepository<SubjectAcl, Long> {

  Optional<SubjectAcl> findByDomainAndNodeAndPrincipalAndTypeAndPermission(
      String domain, String node, String principal, SubjectType type, String permission);

  List<SubjectAcl> findByPrincipalAndType(String principal, SubjectType type);

  List<SubjectAcl> findByDomainAndNodeAndType(String domain, String node, SubjectType type);

  List<SubjectAcl> findByDomainAndNodeLikeAndType(String domain, String node, SubjectType type);

  List<SubjectAcl> findByDomainAndType(String domain, SubjectType type);

  List<SubjectAcl> findByDomainAndNodeAndPrincipalAndType(String domain, String node, String principal,
                                                          SubjectType type);

  List<SubjectAcl> findByDomainAndNodeLikeAndPrincipalAndType(String domain, String node, String principal,
                                                              SubjectType type);

  List<SubjectAcl> findByDomainAndNode(String domain, String node);

  List<SubjectAcl> findByDomainAndNodeLike(String domain, String node);

  List<SubjectAcl> findByNodeOrNodeLike(String node, String nodePattern);

  List<SubjectAcl> findByPermission(String permission);

  /**
   * Delete the stored SubjectAcl identified by its natural key, whether or not the object handed in is the one that was
   * loaded. {@code delete} alone would not: given an object built by a caller, it has no primary key to delete by and
   * silently does nothing, where the document store this replaces always resolved the unique properties first.
   */
  default void deleteByKey(SubjectAcl acl) {
    findByDomainAndNodeAndPrincipalAndTypeAndPermission(
        acl.getDomain(), acl.getNode(), acl.getPrincipal(), acl.getType(), acl.getPermission()).ifPresent(this::delete);
  }

  default SubjectAcl upsert(SubjectAcl acl) {
    return save(EntityKeys.reuseKey(findByDomainAndNodeAndPrincipalAndTypeAndPermission(
        acl.getDomain(), acl.getNode(), acl.getPrincipal(), acl.getType(), acl.getPermission()).orElse(null), acl));
  }
}
