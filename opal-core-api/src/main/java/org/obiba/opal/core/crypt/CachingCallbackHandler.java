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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

public interface CachingCallbackHandler extends CallbackHandler {

  void cacheCallbackResult(Callback callback);

  /**
   * Removes password specified by the alias from the password cache.
   *
   * @param alias the alias of the password to be removed from the cache.
   */
  void clearPasswordCache(String alias);
}
