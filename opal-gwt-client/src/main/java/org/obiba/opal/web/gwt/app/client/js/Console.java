/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.js;

import com.google.gwt.core.client.JavaScriptObject;

public class Console {

  public static native void log(JavaScriptObject jso)
  /*-{
     console.log(jso);
   }-*/;

  public static native void log(String str)
  /*-{
     console.log(str);
   }-*/;
}
