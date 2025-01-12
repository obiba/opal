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

import org.obiba.opal.core.domain.security.SubjectToken;

import java.util.Date;
import java.util.List;

/**
 * Service to handle the personal access API tokens of subjects.
 */
public interface SubjectTokenService extends SystemService {

  /**
   * Save (create or update) the provided token and return the saved token (useful when athe token string has been generated).
   *
   * @param token
   */
  SubjectToken saveToken(SubjectToken token);

  /**
   * Delete the token with the identifier, ignored if it does not exist.
   *
   * @param id
   */
  void deleteToken(String id);

  /**
   * Delete the token with the principal and name, ignored if it does not exist.
   *
   * @param principal
   * @param name name
   */
  void deleteToken(String principal, String name);

  /**
   * Renew an inactive token.
   *
   * @param principal
   * @param name
   */
  void renewToken(String principal, String name);

  /**
   * Get the token object from its identifier.
   *
   * @param id
   * @return
   */
  SubjectToken getToken(String id) throws NoSuchSubjectTokenException;

  /**
   * Update last access date.
   *
   * @param token
   */
  void touchToken(SubjectToken token);

  /**
   * Get token inactive and expire dates.
   *
   * @param token
   * @return
   */
  SubjectTokenTimestamps getTokenTimestamps(SubjectToken token);

  /**
   * Get the token object from its identifier and principal.
   *
   * @param id
   * @param principal
   * @return
   */
  SubjectToken getToken(String id, String principal) throws NoSuchSubjectTokenException;

  /**
   * Check there is a token associated to a subject's principal.
   *
   * @param principal
   * @param name
   * @return
   */
  boolean hasToken(String principal, String name);

  /**
   * Check there is a specific token.
   *
   * @param id
   * @return
   */
  boolean hasToken(String id);

  /**
   * Delete all the tokens associated to a principal.
   *
   * @param principal
   */
  void deleteTokens(String principal);

  /**
   * Get all the tokens associated to a subject's principal.
   *
   * @param principal
   * @return
   */
  List<SubjectToken> getTokens(String principal);

  /**
   * Generate the token value.
   *
   * @return
   */
  String generateToken();

  class SubjectTokenTimestamps {
    private final Date expiresAt;
    private final Date inactiveAt;

    public SubjectTokenTimestamps(Date inactiveAt, Date expiresAt) {
      this.expiresAt = expiresAt;
      this.inactiveAt = inactiveAt;
    }

    public Date getExpiresAt() {
      return expiresAt;
    }

    public Date getInactiveAt() {
      return inactiveAt;
    }

    public boolean isActive() {
      return inactiveAt == null || inactiveAt.after(new Date());
    }
  }

}
