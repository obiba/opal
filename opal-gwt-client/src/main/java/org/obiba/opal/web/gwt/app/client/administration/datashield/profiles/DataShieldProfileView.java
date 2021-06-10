/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Icon;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

public class DataShieldProfileView extends ViewWithUiHandlers<DataShieldProfileUiHandlers> implements DataShieldProfilePresenter.Display {

  interface Binder extends UiBinder<Widget, DataShieldProfileView> {
  }

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Alert missingClusterNotice;

  @UiField
  Icon statusIcon;

  @UiField
  InlineLabel statusLabel;

  @UiField
  Button enableProfile;

  @UiField
  Button disableProfile;

  @UiField
  Icon permsIcon;

  @UiField
  InlineLabel permsLabel;

  @UiField
  Button restrictProfile;

  @UiField
  Button unrestrictProfile;

  @UiField
  SimplePanel permissionsPanel;

  @UiField
  SimplePanel aggregatePanel;

  @UiField
  SimplePanel assignPanel;

  @UiField
  SimplePanel optionsPanel;

  @Inject
  public DataShieldProfileView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void renderProfile(DataShieldProfileDto profile, RServerClusterDto cluster) {
    missingClusterNotice.setVisible(cluster == null);
    if (profile.getEnabled()) {
      statusIcon.removeStyleName("status-error");
      statusIcon.addStyleName("status-success");
      statusLabel.setText(translations.dataShieldProfileEnabledLabel());
      enableProfile.setVisible(false);
      disableProfile.setVisible(true);
    } else {
      statusIcon.removeStyleName("status-success");
      statusIcon.addStyleName("status-error");
      statusLabel.setText(translations.dataShieldProfileDisabledLabel());
      enableProfile.setVisible(cluster != null);
      disableProfile.setVisible(false);
    }
    if (profile.getRestrictedAccess()) {
      permsIcon.removeStyleName("status-success");
      permsIcon.addStyleName("status-warning");
      permsLabel.setText(translations.dataShieldProfileRestrictedLabel());
      restrictProfile.setVisible(false);
      unrestrictProfile.setVisible(true);
      permissionsPanel.setVisible(true);
    } else {
      permsIcon.removeStyleName("status-warning");
      permsIcon.addStyleName("status-success");
      permsLabel.setText(translations.dataShieldProfileUnrestrictedLabel());
      restrictProfile.setVisible(true);
      unrestrictProfile.setVisible(false);
      permissionsPanel.setVisible(false);
    }
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
    if (slot == DataShieldProfilePresenter.AggregateEnvironmentSlot) {
      aggregatePanel.clear();
      aggregatePanel.setWidget(content);
    } else if (slot == DataShieldProfilePresenter.AssignEnvironmentSlot) {
      assignPanel.clear();
      assignPanel.setWidget(content);
    } else if (slot == DataShieldProfilePresenter.OptionsSlot) {
      optionsPanel.clear();
      optionsPanel.setWidget(content);
    } else if (slot == DataShieldProfilePresenter.PermissionsSlot) {
      permissionsPanel.clear();
      permissionsPanel.setWidget(content);
    }
  }

  @UiHandler("resetProfile")
  void onProfileReset(ClickEvent event) {
    getUiHandlers().onProfileReset();
  }

  @UiHandler("enableProfile")
  void onProfileEnable(ClickEvent event) {
    getUiHandlers().onProfileEnable(true);
  }

  @UiHandler("disableProfile")
  void onProfileDisable(ClickEvent event) {
    getUiHandlers().onProfileEnable(false);
  }

  @UiHandler("restrictProfile")
  void onProfileRestrict(ClickEvent event) {
    getUiHandlers().onProfileRestrictAccess(true);
  }

  @UiHandler("unrestrictProfile")
  void onProfileUnrestrict(ClickEvent event) {
    getUiHandlers().onProfileRestrictAccess(false);
  }

}
