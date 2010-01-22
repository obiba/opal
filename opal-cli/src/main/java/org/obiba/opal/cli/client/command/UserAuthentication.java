/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import java.util.Arrays;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.obiba.opal.cli.client.command.options.AuthenticationOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commands that require user authentication may call the authentication method of this class. If the --username and
 * --password were provided on the commandline they will be used. If not the user will be prompted for their username
 * and password.
 */
public class UserAuthentication {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(UserAuthentication.class);

  private AuthenticationOptions options;

  private String username;

  private char[] password;

  UserAuthentication(AuthenticationOptions options) {
    this.options = options;
    initShiro();
  }

  void authenticate() {
    promptForUsername();
    promptForPassword();
    // String foo = new String(password);
    // System.console().printf("username [%s] password [%s]\n", username, foo);
    // get the currently executing user:
    Subject currentUser = SecurityUtils.getSubject();
    if(!currentUser.isAuthenticated()) {
      UsernamePasswordToken token = new UsernamePasswordToken(username, password);
      token.setRememberMe(true);
      try {
        currentUser.login(token);
      } catch(AuthenticationException ae) {
        // Do nothing here. Check for authentication outside this block.
      } finally {
        Arrays.fill(password, ' '); // Zero password to remove it from memory.
      }
    }
    if(!currentUser.isAuthenticated()) {
      System.err.printf("%s", "Authentication failed.\n");
      System.exit(2);
    }
  }

  private void promptForUsername() {
    if(!options.isUsername()) {
      username = System.console().readLine("username: ");
    } else {
      username = options.getUsername();
    }
  }

  private void promptForPassword() {
    if(!options.isPassword()) {
      password = System.console().readPassword("password: ");
    } else {
      password = options.getPassword().toCharArray();
    }

  }

  public void initShiro() {
    // Loads the ini file from the classpath.
    Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
    SecurityManager securityManager = factory.getInstance();

    // Make the securityManager accessible as a singleton.
    // This would be done by Spring when using the spring support packages.
    SecurityUtils.setSecurityManager(securityManager);
  }

}
