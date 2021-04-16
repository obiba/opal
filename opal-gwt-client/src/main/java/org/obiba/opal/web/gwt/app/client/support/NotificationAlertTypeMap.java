/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import com.github.gwtbootstrap.client.ui.constants.AlertType;

import static org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;

public final class NotificationAlertTypeMap {

  private NotificationAlertTypeMap() {}

  public static AlertType getAlertType(NotificationType type) {
    switch(type) {
      case ERROR:
        return AlertType.ERROR;
      case WARNING:
        return AlertType.WARNING;
      case INFO:
        return AlertType.INFO;
      case SUCCESS:
        return AlertType.SUCCESS;
    }

    throw new IllegalArgumentException("Invalid NotificationType: " + type);
  }

}
