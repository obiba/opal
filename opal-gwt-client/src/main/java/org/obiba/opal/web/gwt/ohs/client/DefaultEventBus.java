/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.ohs.client;

import org.obiba.opal.web.gwt.rest.client.event.RequestEventBus;

import com.google.gwt.event.shared.HandlerManager;

/**
 *
 */
public class DefaultEventBus extends HandlerManager implements RequestEventBus {

  public DefaultEventBus() {
    super(null);
  }

}