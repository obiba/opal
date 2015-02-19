/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.r;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 *
 */
public class RAdministrationView extends ViewWithUiHandlers<RAdministrationUiHandlers>
    implements RAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, RAdministrationView> {}

  private final Translations translations;

  @UiField
  Button startStopButton;

  @UiField
  Button rTestButton;

  @UiField
  Panel permissionsPanel;

  @UiField
  Panel rSessions;

  @UiField
  Panel permissions;

  @UiField
  Panel breadcrumbs;

  private Status status;

  //
  // Constructors
  //

  @Inject
  public RAdministrationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("startStopButton")
  public void onStartStop(ClickEvent event) {
    if(Status.Startable.equals(status)) {
      getUiHandlers().start();
    } else {
      getUiHandlers().stop();
    }
  }

  @UiHandler("rTestButton")
  public void onTest(ClickEvent event) {
    getUiHandlers().test();
  }

  @Override
  public void setServiceStatus(Status status) {
    this.status = status;
    switch(status) {
      case Startable:
        startStopButton.setText(translations.startLabel());
        startStopButton.setEnabled(true);
        rTestButton.setEnabled(true);
        break;
      case Stoppable:
        startStopButton.setText(translations.stopLabel());
        startStopButton.setEnabled(true);
        rTestButton.setEnabled(true);
        break;
      case Pending:
        startStopButton.setEnabled(false);
        rTestButton.setEnabled(false);
        break;
    }
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == Slots.RSessions) {
      rSessions.clear();
      rSessions.add(content);
    }

    if(slot == Slots.Permissions) {
      permissions.clear();
      permissions.add(content);
    }
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

}
