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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;

import org.obiba.opal.cli.client.command.options.AuthenticationOptions;

/**
 * CLI utility methods.
 */
public class CliUtil {

  /**
   * Prompts the user for his user name and password.
   * 
   * The information is captured and stored in the <code>options</code> argument.
   * 
   * @param options authentication options (for storing the user name and password)
   */
  public static void promptForPassword(AuthenticationOptions options) {
    // TODO
  }

  /**
   * Prompts the user for a password.
   * 
   * @param prompt the password prompt
   * @return the user-entered password (<code>null</code> if nothing entered)
   */
  public static String promptForPassword(String prompt) {
    String password = null;

    PasswordCallback passwordCallback = new PasswordCallback(prompt, false);
    CallbackHandler handler = new ConsoleCallbackHandler();

    try {
      handler.handle(new Callback[] { passwordCallback });
      if(passwordCallback.getPassword() != null) {
        password = new String(passwordCallback.getPassword());

        if(password.length() == 0) {
          password = null;
        }
      }
    } catch(Exception ex) {
      // nothing to do
    } finally {
      passwordCallback.clearPassword();
    }

    return password;
  }
}
