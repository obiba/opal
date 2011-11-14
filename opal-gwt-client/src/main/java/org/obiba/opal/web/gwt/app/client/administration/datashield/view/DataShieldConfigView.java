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

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class DataShieldConfigView implements DataShieldConfigPresenter.Display {

  @UiTemplate("DataShieldConfigView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DataShieldConfigView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  @UiField
  RadioButton restricted;

  @UiField
  RadioButton unrestricted;

  @UiField
  HorizontalTabLayout environments;

  @UiField
  Panel permissionsPanel;

  @UiField
  Panel permissions;

  public DataShieldConfigView() {
    super();
    uiWidget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public void startProcessing() {

  }

  @Override
  public void stopProcessing() {

  }

  @Override
  public String getLevel() {
    if(unrestricted.getValue()) {
      return "UNRESTRICTED";
    }
    return "RESTRICTED";
  }

  @Override
  public void setLevel(String level) {
    boolean isUnrestricted = level.equals("UNRESTRICTED");
    unrestricted.setValue(isUnrestricted);
    restricted.setValue(!isUnrestricted);

    environments.setVisible(!isUnrestricted);
  }

  @Override
  public void addEnvironmentDisplay(String name, WidgetDisplay display) {
    environments.add(display.asWidget(), name);
  }

  @Override
  public void setPermissionsDisplay(AuthorizationPresenter.Display display) {
    display.setExplanation(translations.datashieldPermissions());
    permissions.add(display.asWidget());
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

}
