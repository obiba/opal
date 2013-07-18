/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.HasUrl;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 *
 */
public class ApplicationView extends ViewWithUiHandlers<ApplicationUiHandlers> implements ApplicationPresenter.Display {

  interface Binder extends UiBinder<Widget, ApplicationView> {}

  @UiField
  NavLink administrationItem;

  @UiField
  NavLink username;

  @UiField
  Label version;

  @UiField
  NavLink projectsItem;

  @UiField
  Panel workbench;

  @UiField
  Frame frame;

  MenuItem currentSelection;

  @Inject
  public ApplicationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(ApplicationPresenter.WORKBENCH == slot) {
      workbench.clear();
      workbench.add(content.asWidget());
    }
  }

  @Override
  public HasUrl getDownloader() {
    return new HasUrl() {

      @Override
      public void setUrl(String url) {
        frame.setUrl(url);
      }
    };
  }

  @Override
  public void setCurrentSelection(MenuItem selection) {
    if(currentSelection != null) {
      currentSelection.removeStyleName("selected");
    }
    if(selection != null) {
      selection.addStyleName("selected");
    }
    currentSelection = selection;
  }

  @Override
  public void clearSelection() {
    setCurrentSelection(null);
  }

  @Override
  public HasAuthorization getAdministrationAuthorizer() {
    return new UIObjectAuthorizer(administrationItem) {
      @Override
      public void authorized() {
        super.authorized();
      }

      @Override
      public void unauthorized() {
        super.unauthorized();
      }
    };
  }

  @Override
  public void setUsername(String username) {
    this.username.setText(username);
  }

  @Override
  public void setVersion(String version) {
    this.version.setText(version);
  }

  @Override
  public HasAuthorization getProjectsAutorizer() {
    return new UIObjectAuthorizer(projectsItem);
  }

  @UiHandler("dashboardItem")
  void onDashboard(ClickEvent event) {
    getUiHandlers().onDashboard();
  }

  @UiHandler("projectsItem")
  void onProjects(ClickEvent event) {
    getUiHandlers().onProjects();
  }

  @UiHandler("administrationItem")
  void onAdministration(ClickEvent event) {
    getUiHandlers().onAdministration();
  }

  @UiHandler("helpItem")
  void onHelp(ClickEvent event) {
    getUiHandlers().onHelp();
  }

  @UiHandler("quitItem")
  void onQuit(ClickEvent event) {
    getUiHandlers().onQuit();
  }

}
