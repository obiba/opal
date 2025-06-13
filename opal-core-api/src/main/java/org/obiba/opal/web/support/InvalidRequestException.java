/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.support;

import com.google.common.collect.Lists;

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

  private final List<String> messageArgs;

  //
  // Constructors
  //

  public InvalidRequestException(String message, String... messageArgs) {
    super(message);

    this.messageArgs = new ArrayList<>();

    if(messageArgs != null) {
      Collections.addAll(this.messageArgs, messageArgs);
    }
  }

  //
  // Methods
  //

  public List<String> getMessageArgs() {
    return Lists.newArrayList(messageArgs);
  }
}
