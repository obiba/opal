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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This test callbackhandler always returns the password supplied to the constructor. Use this for unit testing only.
 */
public class StudyKeyStoreCallbackHandler implements CallbackHandler {

  private char[] password;

  StudyKeyStoreCallbackHandler(char[] password) {
    super();
    this.password = password;
  }

  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for(Callback callback : callbacks) {
      if(callback instanceof StudyKeyStorePasswordCallback) {
        StudyKeyStorePasswordCallback testPasswordCallback = (StudyKeyStorePasswordCallback) callback;
        testPasswordCallback.setPassword(password);
      } else if(callback instanceof PasswordCallback) {
        PasswordCallback passwordCallback = (PasswordCallback) callback;
        passwordCallback.setPassword(password);
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }
}
