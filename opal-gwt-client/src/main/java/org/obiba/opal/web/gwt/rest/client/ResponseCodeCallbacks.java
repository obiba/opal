/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

public final class ResponseCodeCallbacks {

  private final static ResponseCodeCallback noOp = new ResponseCodeCallback() {
    @Override
    public void onResponseCode(Request request, Response response) {

    }
  };

  public static final ResponseCodeCallback noOp() {
    return noOp;
  }

}
