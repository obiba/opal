/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.datasource.onyx;

import java.security.KeyPair;

/**
 * Interface for key providers.
 * 
 * This is simply an abstraction of entities that can provide cryptographic keys (public or private) upon demand. (For
 * example, a keystore may serve as a key provider.)
 */
public interface IKeyProvider {

  /**
   * Returns the specified key pair.
   * 
   * @param alias the key pair's alias
   * @return the key pair
   */
  public KeyPair getKeyPair(String alias);
}
