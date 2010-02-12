/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.util;

import java.io.Console;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.obiba.opal.core.crypt.CacheablePasswordCallback;
import org.obiba.opal.core.crypt.CachingCallbackHandler;

/**
 * <p>
 * Console-based <code>CallbackHandler</code>.
 * </p>
 * 
 * <p>
 * Supports
 * <ul>
 * <li>TextInputCallback</li>
 * <li>PasswordCallback</li>
 * </ul>
 * </p>
 */
public class ConsoleCallbackHandler implements CachingCallbackHandler {
  //
  // Instance Variables
  //

  private Map<String, char[]> passwordCache;

  //
  // Constructors
  //

  public ConsoleCallbackHandler() {
    passwordCache = new HashMap<String, char[]>();
  }

  //
  // CallbackHandler Methods
  //

  /**
   * Handles the specified callbacks.
   * 
   * @param callbacks the callbacks to handle
   * @throws IOException if a console is not available
   * @throws UnsupportedCallbackException if a callback is of a type not supported (only <code>TextInputCallback</code>
   * and <code>PasswordCallback</code> are supported)
   */
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    Console console = System.console();
    if(console == null) {
      throw new IOException("No console");
    }

    for(Callback c : callbacks) {
      if(c instanceof TextInputCallback) {
        TextInputCallback textCallback = (TextInputCallback) c;
        textCallback.setText(console.readLine("%s", textCallback.getPrompt()));
      } else if(c instanceof PasswordCallback) {
        PasswordCallback passwordCallback = (PasswordCallback) c;

        if(passwordCallback instanceof CacheablePasswordCallback) {
          String passwordKey = ((CacheablePasswordCallback) passwordCallback).getPasswordKey();

          if(passwordCache.containsKey(passwordKey)) {
            passwordCallback.setPassword(passwordCache.get(passwordKey));
          } else {
            passwordCallback.setPassword(console.readPassword("%s", passwordCallback.getPrompt()));
            passwordCache.put(passwordKey, passwordCallback.getPassword());
          }
        } else {
          passwordCallback.setPassword(console.readPassword("%s", passwordCallback.getPrompt()));
        }
      } else {
        throw new UnsupportedCallbackException(c);
      }
    }
  }

  public void cacheCallbackResult(Callback callback) {
    if(callback instanceof CacheablePasswordCallback) {
      CacheablePasswordCallback cacheableCallback = (CacheablePasswordCallback) callback;
      passwordCache.put(cacheableCallback.getPasswordKey(), cacheableCallback.getPassword());
    }
  }

  public void clearPasswordCache(String alias) {
    passwordCache.remove(alias);
  }
}
