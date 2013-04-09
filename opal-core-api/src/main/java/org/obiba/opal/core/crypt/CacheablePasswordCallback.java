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

import javax.security.auth.callback.PasswordCallback;

import org.springframework.util.Assert;

public class CacheablePasswordCallback extends PasswordCallback {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  private final String passwordKey;

  private String confirmationPrompt;

  //
  // Constructors
  //

  public void setConfirmationPrompt(String confirmationPrompt) {
    this.confirmationPrompt = confirmationPrompt;
  }

  public boolean isConfirmationPrompt() {
    return confirmationPrompt != null && !"".equals(confirmationPrompt);
  }

  private CacheablePasswordCallback(String passwordKey, String prompt, boolean echoOn) {
    super(prompt, echoOn);

    this.passwordKey = passwordKey;
  }

  //
  // Methods
  //

  /**
   * The password's "key" (i.e., the key used to cache it).
   *
   * @return password key
   */
  public String getPasswordKey() {
    return passwordKey;
  }

  /**
   * @return
   */
  public String getConfirmationPrompt() {
    return confirmationPrompt;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private String passwordKey;

    private String prompt;

    private String confirmationPrompt;

    private boolean echoOn = false;

    public static Builder newCallback() {
      return new Builder();
    }

    /**
     * Key used to cache password.
     */
    public Builder key(String Key) {
      passwordKey = Key;
      return this;
    }

    /**
     * Password prompt.
     */
    public Builder prompt(String prompt) {
      this.prompt = prompt;
      return this;
    }

    /**
     * Password confirmation prompt. (optional)
     */
    public Builder confirmation(String confirmationPrompt) {
      this.confirmationPrompt = confirmationPrompt;
      return this;
    }

    /**
     * Turns password echoing on. Echoing is off by default.
     */
    public Builder echoOn() {
      echoOn = true;
      return this;
    }

    public CacheablePasswordCallback build() {
      Assert.hasText(passwordKey, "key must not be null or empty");
      Assert.hasText(prompt, "prompt must not be null or empty");
      CacheablePasswordCallback cacheablePasswordCallback = new CacheablePasswordCallback(passwordKey, prompt, echoOn);
      cacheablePasswordCallback.setConfirmationPrompt(confirmationPrompt);
      return cacheablePasswordCallback;
    }
  }
}
