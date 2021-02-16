/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.spi.r;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Exception thrown when a R try-error statement fails.
 */
public class REvaluationRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(REvaluationRuntimeException.class);

  private final List<String> rMessages;

  public REvaluationRuntimeException(String msg, List<String> rMessages) {
    super(msg);
    this.rMessages = rMessages;
  }

  public List<String> getRMessages() {
    return rMessages;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + " -> " + Joiner.on("; ").join(rMessages);
  }
}
