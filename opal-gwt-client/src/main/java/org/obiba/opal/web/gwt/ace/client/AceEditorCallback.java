/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.ace.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Note that the argument the callback receives is a JavaScriptObject, so you will probably need to use JSNI to make use of it.
 */
public interface AceEditorCallback {

  /**
   * Callback method.
   *
   * @param obj the event object: for example, an onChange event if this callback is receiving onChange events
   */
  void invokeAceCallback(JavaScriptObject obj);

}

