/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiHandler;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.NavPillsPanel;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class DataShieldConfigView extends ViewWithUiHandlers<DataShieldConfigUiHandlers> implements DataShieldConfigPresenter.Display {

  interface Binder extends UiBinder<Widget, DataShieldConfigView> {}

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Panel packages;

  @UiField
  NavPillsPanel environments;

  @UiField
  Panel permissionsPanel;

  @UiField
  Panel permissions;

  @UiField
  Panel breadcrumbs;

  @UiField
  SimplePanel options;

  @Inject
  public DataShieldConfigView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
    if(slot == DataShieldConfigPresenter.AggregateEnvironmentSlot) {
      environments.add(content.asWidget(), translations.dataShieldLabelsMap().get("Aggregate"));
    }
    if(slot == DataShieldConfigPresenter.AssignEnvironmentSlot) {
      environments.add(content.asWidget(), translations.dataShieldLabelsMap().get("Assign"));
    }
    if(slot == DataShieldConfigPresenter.PackageSlot) {
      packages.clear();
      packages.add(content);
    }
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == DataShieldConfigPresenter.PermissionSlot) {
      permissions.clear();
      permissions.add(content);
    } else if(slot == DataShieldConfigPresenter.OptionsSlot) {
      options.clear();
      options.add(content);
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

  @UiHandler("downloadLogs")
  void onDownloadLogs(ClickEvent event) {
    getUiHandlers().onDownloadLogs();
  }


}
