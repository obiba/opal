/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.view;

import java.util.Comparator;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.NavPillsPanel;
import org.obiba.opal.web.gwt.app.client.ui.NavTabsPanel;
import org.obiba.opal.web.gwt.app.client.ui.RadioGroup;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.datashield.DataShieldConfigDto;
import org.obiba.opal.web.model.client.datashield.DataShieldConfigDto.Level;

import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class DataShieldConfigView extends ViewImpl implements DataShieldConfigPresenter.Display {

  interface Binder extends UiBinder<Widget, DataShieldConfigView> {}

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Panel packagesPanel;

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
