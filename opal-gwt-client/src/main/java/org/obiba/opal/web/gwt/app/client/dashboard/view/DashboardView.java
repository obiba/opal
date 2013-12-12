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

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.PageHeader;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DashboardView extends Composite implements DashboardPresenter.Display {

  interface Binder extends UiBinder<Widget, DashboardView> {}

  @UiField
  PageHeader pageTitle;

  @UiField
  NavLink exploreVariablesLink;

  @UiField
  NavLink exploreFilesLink;

  @UiField
  NavLink reportsLink;

  @UiField
  NavLink identifiersLink;

  @UiField
  Panel datasources;

  @UiField
  Panel identifiers;

  @UiField
  Panel files;

  @UiField
  Panel reports;

  @Inject
  public DashboardView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    getDatasourcesLink().setHref("#" + Places.PROJECTS);
    getIdentifiersLink().setHref("#" + Places.IDENTIFIERS);
    getReportsLink().setHref("#" + Places.REPORT_TEMPLATES);
    getFilesLink().setHref("#" + Places.FILES);
    pageTitle.setText(translations.pageDashboardTitle());
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
  }

  @Override
  public void removeFromSlot(Object slot, IsWidget content) {
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
  }

  public NavLink getDatasourcesLink() {
    return exploreVariablesLink;
  }

  public NavLink getFilesLink() {
    return exploreFilesLink;
  }

  public NavLink getReportsLink() {
    return reportsLink;
  }

  public NavLink getIdentifiersLink() {
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

}
