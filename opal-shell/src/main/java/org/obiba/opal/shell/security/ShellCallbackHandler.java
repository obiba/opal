/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.obiba.crypt.CacheablePasswordCallback;
import org.obiba.crypt.CachingCallbackHandler;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.OpalShellHolder;

/**
 * <p>
 * OpalShell-based <code>CallbackHandler</code>.
 * </p>
 * <p/>
 * <p>
 * Supports
 * <ul>
 * <li>TextInputCallback</li>
 * <li>PasswordCallback</li>
 * </ul>
 * </p>
 */
public class ShellCallbackHandler implements CachingCallbackHandler {
  //
  // Instance Variables
  //

  private final OpalShellHolder opalShellHolder;

  private final Map<String, char[]> passwordCache;

  //
  // Constructors
  //

  public ShellCallbackHandler(OpalShellHolder opalShellHolder) {
    this.opalShellHolder = opalShellHolder;
    passwordCache = new HashMap<>();
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
  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    OpalShell shell = getCurrentShell();
    for(Callback c : callbacks) {
      if(c instanceof TextInputCallback) {
        TextInputCallback textCallback = (TextInputCallback) c;
        textCallback.setText(shell.prompt(textCallback.getPrompt()));
      } else if(c instanceof PasswordCallback) {
        PasswordCallback passwordCallback = (PasswordCallback) c;
        if(passwordCallback instanceof CacheablePasswordCallback) {
          handleCacheablePasswordCallback((CacheablePasswordCallback) passwordCallback);
        } else {
          passwordCallback.setPassword(shell.passwordPrompt(" %s", passwordCallback.getPrompt()));
        }
      } else {
        throw new UnsupportedCallbackException(c);
      }
    }
  }

  private void handleCacheablePasswordCallback(CacheablePasswordCallback cacheablePasswordCallback) throws IOException {
    String passwordKey = cacheablePasswordCallback.getPasswordKey();

    if(passwordCache.containsKey(passwordKey)) {
      cacheablePasswordCallback.setPassword(passwordCache.get(passwordKey));
    } else {
      if(cacheablePasswordCallback.isConfirmationPrompt()) {
        cacheablePasswordCallback.setPassword(promptAndConfirmPassword(cacheablePasswordCallback));
      } else {
        cacheablePasswordCallback
            .setPassword(getCurrentShell().passwordPrompt(" %s", cacheablePasswordCallback.getPrompt()));
      }
      passwordCache.put(passwordKey, cacheablePasswordCallback.getPassword());
    }
  }

  private char[] promptAndConfirmPassword(CacheablePasswordCallback callback) {
    OpalShell shell = getCurrentShell();

    char[] passwordOne;
    char[] passwordTwo;
    boolean firstTime = true;
    do {
      if(!firstTime) shell.printf("Passwords do not match. Try again:\n");
      if(firstTime) firstTime = false;
      passwordOne = shell.passwordPrompt(" %s", callback.getPrompt());
      passwordTwo = shell.passwordPrompt(" %s", callback.getConfirmationPrompt());
    } while(!Arrays.equals(passwordOne, passwordTwo));
    return passwordOne;
  }

  @Override
  public void cacheCallbackResult(Callback callback) {
    if(callback instanceof CacheablePasswordCallback) {
      CacheablePasswordCallback cacheableCallback = (CacheablePasswordCallback) callback;
      passwordCache.put(cacheableCallback.getPasswordKey(), cacheableCallback.getPassword());
    }
  }

  @Override
  public void clearPasswordCache(String alias) {
    passwordCache.remove(alias);
  }

  private OpalShell getCurrentShell() {
    return opalShellHolder.getCurrentShell();
  }
}
