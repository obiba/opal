/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.util.List;
import java.util.Set;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.domain.security.SubjectProfile;

/**
 * Service to handle the profile of subjects.
 */
public interface SubjectProfileService extends SystemService {

  /**
   * Whether the subject's principal has a system artifact.
   *
   * @param principal
   * @return
   */
  boolean supportProfile(@Nullable Object principal);

  /**
   * Add or check profile of the subject: check will fail if a subject from a different realm has already a profile entry.
   *
   * @param principal
   * @param realm
   */
  void ensureProfile(@NotNull String principal, @NotNull String realm);

  /**
   * Add or check profile of the subject: check will fail if a subject from a different realm has already a profile entry.
   *
   * @param principalCollection
   */
  void ensureProfile(@NotNull PrincipalCollection principalCollection);

  /**
   * Associate observed groups to the profile (for reuse with API token).
   *
   * @param principal
   * @param groups
   */
  void applyProfileGroups(String principal, Set<String> groups);

  /**
   * Delete profile.
   *
   * @param principal
   */
  void deleteProfile(@NotNull String principal);

  /**
   * Get profile by principal.
   *
   * @param principal
   * @return
   */
  @NotNull
  SubjectProfile getProfile(@Nullable String principal) throws NoSuchSubjectProfileException;

  /**
   * Update profile timestamp.
   *
   * @param principal
   */
  void updateProfile(@NotNull String principal) throws NoSuchSubjectProfileException;

  /**
   * Set (generate or use the temporary secret) or remove profile's secret key, for 2FA.
   *
   * @param principal
   * @param enable
   */
  void updateProfileSecret(@NotNull String principal, boolean enable);

  /**
   * Set or remove the profile's temporary secret key, for 2FA.
   * @param principal
   * @param enable
   */
  void updateProfileTmpSecret(@NotNull String principal, boolean enable);

  /**
   * Get all subject profiles.
   *
   * @return
   */
  Iterable<SubjectProfile> getProfiles();

  /**
   * Add a bookmark to principal's profile.
   *
   * @param principal
   * @param resources
   * @throws NoSuchSubjectProfileException
   */
  void addBookmarks(String principal, List<String> resources) throws NoSuchSubjectProfileException;

  /**
   * Delete principal's bookmark (if it exists).
   *
   * @param principal
   * @param path
   * @throws NoSuchSubjectProfileException
   */
  void deleteBookmark(String principal, String path) throws NoSuchSubjectProfileException;

  /**
   * Delete all bookmarks (which resource is equal to or starts with) from all profiles.
   *
   * @param path
   * @throws NoSuchSubjectProfileException
   */
  void deleteBookmarks(String path) throws NoSuchSubjectProfileException;
}
