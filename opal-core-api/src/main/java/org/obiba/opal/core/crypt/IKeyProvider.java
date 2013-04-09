/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.crypt;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;

/**
 * Interface for key providers.
 * <p/>
 * This is simply an abstraction of entities that can provide cryptographic keys (public or private) upon demand. (For
 * example, a keystore may serve as a key provider.)
 */
public interface IKeyProvider {

  /**
   * Initializes the <code>IKeyProvider</code> with the specified argument string.
   *
   * @param keyProviderArgs key provider arguments (map contents are provider-dependent)
   * @throws KeyProviderInitializationException if for any reason the <code>IKeyProvider</code> could not be
   * initialized
   */
  void init(Map<String, String> keyProviderArgs) throws KeyProviderInitializationException;

  /**
   * Returns the key pair with the specified alias.
   *
   * @param alias the <code>KeyPair</code>'s alias
   * @param KeyPairNotFoundException if the requested <code>KeyPair</code> was not found
   * @param KeyProviderSecurityException if access to the <code>KeyPair</code> was forbidden
   * @return the <code>KeyPair</code> (<code>null</code> if not found)
   */
  KeyPair getKeyPair(String alias) throws KeyPairNotFoundException, KeyProviderSecurityException;

  /**
   * Returns the <code>KeyPair</code> for the specified public key.
   *
   * @param publicKey a public key
   * @param KeyPairNotFoundException if the requested <code>KeyPair</code> was not found
   * @param KeyProviderSecurityException if access to the <code>KeyPair</code> was forbidden
   * @return the corresponding <code>KeyPair</code> (<code>null</code> if not found)
   */
  KeyPair getKeyPair(PublicKey publicKey) throws KeyPairNotFoundException, KeyProviderSecurityException;
}
