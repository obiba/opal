/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.rest.client.RequestCredentials;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

@Singleton
public class LoggedInGatekeeper implements Gatekeeper {

  private final RequestCredentials credentials;

  @Inject
  public LoggedInGatekeeper(RequestCredentials credentials) {
    this.credentials = credentials;
  }

  @Override
  public boolean canReveal() {
    return credentials.hasCredentials();
  }
}