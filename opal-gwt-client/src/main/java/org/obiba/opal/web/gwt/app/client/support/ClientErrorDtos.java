/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.support;

import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;

/**
 * Client-side utilities for ClientErrorDtos.
 */
public class ClientErrorDtos {

  /**
   * Given a ClientErrorDto JSON string, returns the ClientErrorDto's <code>status</code> field (i.e., the error message
   * key).
   *
   * @param jsonClientErrorDto ClientErrorDto JSON string
   * @return status field of the ClientErrorDto
   */
  public static String getStatus(String jsonClientErrorDto) {
    String errorStatus = null;
    if(jsonClientErrorDto != null && jsonClientErrorDto.length() != 0) {
      try {
        ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(jsonClientErrorDto);
        errorStatus = errorDto.getStatus();
      } catch(Exception ex) {
        // Should never get here!
        errorStatus = "InternalError";
      }
    } else {
      errorStatus = "UnknownError";
    }

    return errorStatus;
  }
}
