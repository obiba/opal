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

import org.obiba.opal.spi.r.RServerException;

public class RserveServerException extends RServerException {

  public RserveServerException(Throwable throwable) {
    super(throwable);
  }

  @Override
  public boolean isClientError() {
    return false;
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }
}
