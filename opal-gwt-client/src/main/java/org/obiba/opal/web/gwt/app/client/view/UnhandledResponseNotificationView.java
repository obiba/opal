/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.UnhandledResponseNotificationPresenter;

import com.github.gwtbootstrap.client.ui.Alert;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class UnhandledResponseNotificationView extends ViewImpl
    implements UnhandledResponseNotificationPresenter.Display {

  interface Binder extends UiBinder<Widget, UnhandledResponseNotificationView> {}

  @UiField
  FlowPanel alertsPanel;

  @UiField
  Alert alert;

  @UiField
  Label errorMessage;

  @Inject
  public UnhandledResponseNotificationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void clearErrorMessages() {
    alertsPanel.clear();
  }

  @Override
  public void setErrorMessage(String title, String message) {
    alert.setHeading(title);
    errorMessage.getElement().setInnerHTML(message);
    alertsPanel.add(alert);
  }

}
