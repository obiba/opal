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

import org.obiba.opal.core.runtime.OpalRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpalSecretKeyPasswordCallbackHandler implements CallbackHandler {

  private final OpalRuntime runtime;

  private final String customPassword;

  @Autowired
  public OpalSecretKeyPasswordCallbackHandler(OpalRuntime runtime, @Value("${org.obiba.opal.keystore.password}") String customPassword) {
    this.runtime = runtime;
    this.customPassword = customPassword;
  }

  private char[] getPassword() {
    if(customPassword == null || customPassword.isEmpty() || customPassword.equals("KEYSTORE_PASSWORD_NOT_SPECIFIED")) {
      return runtime.getOpalConfiguration().getSecretKey().toCharArray();
    } else {
      return customPassword.toCharArray();
    }
  }

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for(int i = 0; i < callbacks.length;) {
      if(callbacks[i] instanceof PasswordCallback) {
        PasswordCallback callback = (PasswordCallback) callbacks[i];
        callback.setPassword(getPassword());
        return;
      }
      throw new UnsupportedCallbackException(callbacks[i]);
    }
  }

}
