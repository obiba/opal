/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client.authorization;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;

import com.google.web.bindery.event.shared.EventBus;

/**
 * Dummy helper class with default implementation.
 */
public abstract class Authorizer implements HasAuthorization {

  private final EventBus eventBus;

  public Authorizer(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void beforeAuthorization() {
  }

  @Override
  public void unauthorized() {
    fireUnauthorizedOperationNotification();
  }

  protected void fireUnauthorizedOperationNotification() {
    eventBus.fireEvent(NotificationEvent.newBuilder().error("UnauthorizedOperation").build());
  }

}
