/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.dashboard.view;

import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.github.gwtbootstrap.client.ui.PageHeader;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DashboardView extends Composite implements DashboardPresenter.Display {

  interface Binder extends UiBinder<Widget, DashboardView> {}

  @UiField
  PageHeader pageTitle;

  @UiField
  IconAnchor exploreVariablesLink;

  @UiField
  IconAnchor exploreFilesLink;

  @UiField
  IconAnchor tasksLink;

  @UiField
  IconAnchor reportsLink;

  @UiField
  IconAnchor identifiersLink;

  @UiField
  Panel datasources;

  @UiField
  Panel identifiers;

  @UiField
  Panel files;

  @UiField
  Panel tasks;

  @UiField
  Panel reports;

  @UiField
  Panel bookmarks;

  @Inject
  public DashboardView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    getDatasourcesLink().setHref("#" + Places.PROJECTS);
    getIdentifiersLink().setHref("#" + Places.ADMINISTRATION + "/" + Places.IDENTIFIERS);
    getReportsLink().setHref("#" + Places.ADMINISTRATION + "/" + Places.REPORT_TEMPLATES);
    getFilesLink().setHref("#" + Places.ADMINISTRATION + "/" + Places.FILES);
    tasksLink.setHref("#" + Places.ADMINISTRATION + "/" + Places.TASKS);
    pageTitle.setText(translations.pageDashboardTitle());

    exploreVariablesLink.setText(translations.exploreVariables());
    exploreFilesLink.setText(translations.manageFiles());
    tasksLink.setText(translations.tasks());
    reportsLink.setText(translations.runReports());
    identifiersLink.setText(translations.manageParticipantIdentifiers());
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
  }

  @Override
  public void removeFromSlot(Object slot, IsWidget content) {
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == DashboardPresenter.BOOKMARKS) {
      bookmarks.clear();
      bookmarks.add(content);
    }
  }

  public IconAnchor getDatasourcesLink() {
    return exploreVariablesLink;
  }

  public IconAnchor getFilesLink() {
    return exploreFilesLink;
  }

  public IconAnchor getReportsLink() {
    return reportsLink;
  }

  public IconAnchor getIdentifiersLink() {
    return identifiersLink;
  }

  @Override
  public HasAuthorization getIdentifiersAuthorizer() {
    return new WidgetAuthorizer(identifiers);
  }

  @Override
  public HasAuthorization getReportsAuthorizer() {
    return new WidgetAuthorizer(reports);
  }

  @Override
  public HasAuthorization getTasksAuthorizer() {
    return new WidgetAuthorizer(tasks);
  }

}
