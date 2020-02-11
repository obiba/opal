/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.resources;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.BreadcrumbsTabPanel;

public class ProjectResourcesView extends ViewImpl implements ProjectResourcesPresenter.Display {

  private final PlaceManager placeManager;

  private final Translations translations;

  interface Binder extends UiBinder<Widget, ProjectResourcesView> {
  }

  @UiField
  BreadcrumbsTabPanel tabPanel;

  private Widget resourceListWidget;

  private Widget resourceWidget;

  @Inject
  public ProjectResourcesView(Binder uiBinder, PlaceManager placeManager, Translations translations) {
    this.placeManager = placeManager;
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void renderResourceList(String projectName) {
    tabPanel.clear();
    tabPanel.addAndSelect(resourceListWidget, "All resources");
    tabPanel.setMenuVisible(false);
  }

  @Override
  public void renderResource(String projectName, String resourceName) {
    tabPanel.clear();
    tabPanel.add(resourceListWidget, getResourcesLink(projectName));
    tabPanel.addAndSelect(resourceWidget, resourceName);
    tabPanel.setMenuVisible(true);
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (content != null) {
      if (slot == RESOURCE.getRawSlot()) {
        resourceWidget = content.asWidget();
      } else if (slot == RESOURCES.getRawSlot()) {
        resourceListWidget = content.asWidget();
      }
    }
  }

  private HasClickHandlers getResourcesLink(String projectName) {
    NavLink link = new NavLink(translations.allResourcesLabel());
    link.setHref("#" + placeManager.buildHistoryToken(ProjectPlacesHelper.getResourcesPlace(projectName)));
    link.setIcon(IconType.DOUBLE_ANGLE_LEFT);
    return link;
  }
}
