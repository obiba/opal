/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.crypt;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class OpalSecretKeyPasswordCallbackHandler implements CallbackHandler {

  @Autowired
  private OpalConfigurationService configService;

  @Value("${org.obiba.opal.keystore.password}")
  private String customPassword;

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    if(callbacks == null || callbacks.length < 1) return;
    Callback callback = callbacks[0];
    if(callback instanceof PasswordCallback) {
      ((PasswordCallback) callback).setPassword(getPassword());
      return;
    }
    throw new UnsupportedCallbackException(callback);
  }

  private char[] getPassword() {
    return Strings.isNullOrEmpty(customPassword) || "KEYSTORE_PASSWORD_NOT_SPECIFIED".equals(customPassword)
        ? configService.getOpalConfiguration().getSecretKey().toCharArray()
        : customPassword.toCharArray();
  }

}
