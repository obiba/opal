/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class AdministrationView extends ViewImpl implements AdministrationPresenter.Display {


  @UiTemplate("AdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, AdministrationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget widget;

  @UiField
  Anchor usersGroupsPlace;

  @UiField
  Anchor unitsPlace;

  @UiField
  Anchor databasesPlace;

  @UiField
  Anchor mongoDbPlace;

  @UiField
  Anchor esPlace;

  @UiField
  Anchor indexPlace;

  @UiField
  Anchor rPlace;

  @UiField
  Anchor dataShieldPlace;

  @UiField
  Anchor pluginsPlace;

  @UiField
  Anchor reportsPlace;

  @UiField
  Anchor filesPlace;

  @UiField
  Anchor tasksPlace;

  @UiField
  Anchor javaPlace;

  @UiField
  Anchor serverPlace;

  @UiField
  Panel breadcrumbs;

  public AdministrationView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public Anchor getUsersGroupsPlace() {
    return usersGroupsPlace;
  }

  @Override
  public Anchor getUnitsPlace() {
    return unitsPlace;
  }

  @Override
  public Anchor getDatabasesPlace() {
    return databasesPlace;
  }

  @Override
  public Anchor getMongoDbPlace() {
    return mongoDbPlace;
  }

  @Override
  public Anchor getEsPlace() {
    return esPlace;
  }

  @Override
  public Anchor getIndexPlace() {
    return indexPlace;
  }

  @Override
  public Anchor getRPlace() {
    return rPlace;
  }

  @Override
  public Anchor getDataShieldPlace() {
    return dataShieldPlace;
  }

  @Override
  public Anchor getPluginsPlace() {
    return pluginsPlace;
  }

  @Override
  public Anchor getReportsPlace() {
    return reportsPlace;
  }

  @Override
  public Anchor getFilesPlace() {
    return filesPlace;
  }

  @Override
  public Anchor getTasksPlace() {
    return tasksPlace;
  }

  @Override
  public Anchor getJavaPlace() {
    return javaPlace;
  }

  @Override
  public Anchor getServerPlace() {
    return serverPlace;
  }

  @Override
  public void setBreadcrumbItems(List<BreadcrumbsBuilder.Item> items) {
    breadcrumbs.add(new BreadcrumbsBuilder().setItems(items).build());
  }

}
