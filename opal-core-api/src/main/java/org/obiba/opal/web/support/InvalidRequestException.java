/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Signals an invalid HTTP request (missing parameter, illegal parameter value, etc.).
 */
public class InvalidRequestException extends RuntimeException {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  private List<String> messageArgs;

  //
  // Constructors
  //

  public InvalidRequestException(String message, String... messageArgs) {
    super(message);

    this.messageArgs = new ArrayList<String>();

    if(messageArgs != null) {
      for(String messageArg : messageArgs) {
        this.messageArgs.add(messageArg);
      }
    }
  }

  //
  // Methods
  //

  public List<String> getMessageArgs() {
    return Collections.unmodifiableList(messageArgs);
  }
}
