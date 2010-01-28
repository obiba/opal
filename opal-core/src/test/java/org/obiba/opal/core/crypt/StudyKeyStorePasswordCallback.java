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

/**
 * Test password callback used my junit tests only.
 */
public class StudyKeyStorePasswordCallback extends PasswordCallback {

  private static final long serialVersionUID = 1L;

  public StudyKeyStorePasswordCallback(String prompt, boolean echoOn) {
    super(prompt, echoOn);
  }

}
