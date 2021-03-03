/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rserve;

import com.google.common.base.Joiner;
import org.obiba.opal.spi.r.RServerException;

import java.util.List;

public class RserveClientException extends RServerException {

  private final List<String> rMessages;

  public RserveClientException(String msg, List<String> rMessages) {
    super(msg);
    this.rMessages = rMessages;
  }

  @Override
  public boolean isClientError() {
    return true;
  }

  @Override
  public String getMessage() {
    return rMessages == null || rMessages.isEmpty() ? super.getMessage() : Joiner.on(" ; ").join(rMessages);
  }
}
