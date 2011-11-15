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

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.RadioGroup;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.datashield.DataShieldConfigDto;
import org.obiba.opal.web.model.client.datashield.DataShieldConfigDto.Level;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasValue;
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

  RadioGroup<DataShieldConfigDto.Level> radioGroup;

  public DataShieldConfigView() {
    super();
    uiWidget = uiBinder.createAndBindUi(this);
    radioGroup = new RadioGroup<DataShieldConfigDto.Level>(new Comparator<Level>() {

      @Override
      public int compare(Level o1, Level o2) {
        return o1.getName().compareTo(o2.getName());
      }

    });

    // TODO: determine if we're leaking handlers here.
    radioGroup.addButton(restricted, DataShieldConfigDto.Level.RESTRICTED);
    radioGroup.addButton(unrestricted, DataShieldConfigDto.Level.UNRESTRICTED);
    radioGroup.addValueChangeHandler(new ValueChangeHandler<DataShieldConfigDto.Level>() {

      @Override
      public void onValueChange(ValueChangeEvent<Level> event) {
        boolean isRestricted = event.getValue() == DataShieldConfigDto.Level.RESTRICTED;
        environments.setVisible(isRestricted);

      }
    });
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
  public HasValue<Level> levelSelector() {
    return radioGroup;
  }

  @Override
  public void addEnvironmentDisplay(String name, WidgetDisplay display) {
    environments.add(display.asWidget(), translations.dataShieldLabelsMap().get(name));
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
