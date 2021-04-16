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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConflictingRequestException extends RuntimeException {

  private final List<String> messageArgs;

  public ConflictingRequestException(String message, String... messageArgs) {
    super(message);

    this.messageArgs = new ArrayList<>();

    if(messageArgs != null) {
      Collections.addAll(this.messageArgs, messageArgs);
    }
  }

  public List<String> getMessageArgs() {
    return Collections.unmodifiableList(messageArgs);
  }
}
