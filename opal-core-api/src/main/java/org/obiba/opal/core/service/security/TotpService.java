/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import org.obiba.opal.core.service.SystemService;

/**
 * Two-factor authentication using time-based one-time password.
 */
public interface TotpService {

  /**
   * Generate a secret key.
   *
   * @return
   */
  String generateSecret();

  /**
   * Generate the TOTP QR code.
   *
   * @param label
   * @param secret
   * @return
   */
  String getQrImageDataUri(String label, String secret);

  /**
   * Validate the code for the secret.
   *
   * @param code
   * @param secret
   * @return
   */
  boolean validateCode(String code, String secret);

}
