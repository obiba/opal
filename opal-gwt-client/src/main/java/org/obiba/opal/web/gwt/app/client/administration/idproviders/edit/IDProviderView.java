/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.idproviders.edit;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.*;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.validator.ConstrainedModal;
import org.obiba.opal.web.model.client.opal.IDProviderDto;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class IDProviderView extends ModalPopupViewWithUiHandlers<IDProviderUiHandlers>
    implements IDProviderPresenter.Display {

  public static final int COMMA_KEY = 188;

  @UiTemplate("IDProviderView.ui.xml")
  interface Binder extends UiBinder<Widget, IDProviderView> {}

  @UiField
  Modal modal;

  @UiField
  TextBox usernameClaim;

  @UiField
  Chooser groupsMapping;

  @UiField
  TextBox groups;

  @UiField
  ControlGroup groupsClaimGroup;

  @UiField
  TextBox groupsClaim;

  @UiField
  ControlGroup groupsScriptGroup;

  @UiField
  TextArea groupsScript;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox name;

  @UiField
  ControlGroup clientIdGroup;

  @UiField
  TextBox clientId;

  @UiField
  ControlGroup secretGroup;

  @UiField
  TextBox secret;

  @UiField
  ControlGroup discoveryUriGroup;

  @UiField
  TextBox discoveryUri;

  @UiField
  TextBox scope;

  @UiField
  ControlGroup providerUrlGroup;

  @UiField
  TextBox providerUrl;

  @UiField
  TextBox label;

  @UiField
  CheckBox useNonce;

  @UiField
  NumericTextBox connectTimeout;

  @UiField
  NumericTextBox readTimeout;

  @UiField
  TextBox callbackUrl;

  private IDProviderDto provider;

  @Inject
  public IDProviderView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    initConstrainedModal(translations);
    groupsScript.setPlaceholder("// input: userInfo\n// output: an array of strings\n\n// example:\nuserInfo.some.property.map(x => x.split(':')[0])");
    groupsScript.setVisibleLines(6);
    groupsScript.setWidth("500px");
    groupsMapping.addItem("By claim value", "claim");
    groupsMapping.addItem("By javascript", "js");
    groupsMapping.setSelectedValue("claim");
    groupsMapping.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent chosenChangeEvent) {
        if ("claim".equals(groupsMapping.getSelectedValue())) {
          groupsClaimGroup.setVisible(true);
          groupsScriptGroup.setVisible(false);
        } else {
          groupsClaimGroup.setVisible(false);
          groupsScriptGroup.setVisible(true);
        }
      }
    });
  }

  /**
   * Used to support ConstraintViolation exceptions
   */
  private void initConstrainedModal(Translations translations) {
    ConstrainedModal constrainedModal = new ConstrainedModal(modal);
    constrainedModal.registerWidget("name", translations.nameLabel(), nameGroup);
  }

  @Override
  public void setTitle(String title) {
    modal.setTitle(title);
  }

  @Override
  public void setIDProvider(IDProviderDto provider, IDProviderPresenter.Mode dialogMode) {
    this.provider = provider;
    name.setValue(provider.getName());
    clientId.setValue(provider.getClientId());
    secret.setValue(provider.getSecret());
    scope.setValue(provider.getScope());
    discoveryUri.setValue(provider.getDiscoveryURI());
    useNonce.setValue(provider.getUseNonce());
    label.setValue(provider.getLabel());
    providerUrl.setValue(provider.getProviderUrl());
    usernameClaim.setValue(provider.getUsernameClaim());
    groups.setValue(provider.getGroups());
    groupsClaim.setValue(provider.getGroupsClaim());
    groupsScript.setValue(provider.getGroupsScript());
    if (Strings.isNullOrEmpty(provider.getGroupsScript())) {
      groupsClaimGroup.setVisible(true);
      groupsScriptGroup.setVisible(false);
      groupsMapping.setSelectedValue("claim");
    } else {
      groupsClaimGroup.setVisible(false);
      groupsScriptGroup.setVisible(true);
      groupsMapping.setSelectedValue("js");
    }
    connectTimeout.setValue(provider.getConnectTimeout());
    readTimeout.setValue(provider.getReadTimeout());
    callbackUrl.setValue(provider.getCallbackURL());
    if (IDProviderPresenter.Mode.UPDATE.equals(dialogMode)) {
      name.setEnabled(false);
    } else {
      name.setValue("");
      provider.setEnabled(false);
    }
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public TextBox getClientId() {
    return clientId;
  }

  @Override
  public TextBox getSecret() {
    return secret;
  }

  @Override
  public TextBox getDiscoveryUri() {
    return discoveryUri;
  }

  @Override
  public TextBox getProviderUrl() {
    return providerUrl;
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    if (provider == null) {
      provider = IDProviderDto.create();
      provider.setEnabled(false);
    }
    provider.setName(name.getValue());
    provider.setClientId(clientId.getValue());
    provider.setSecret(secret.getValue());
    provider.setDiscoveryURI(discoveryUri.getValue());
    provider.setScope(Strings.isNullOrEmpty(scope.getValue()) ? "openid" : scope.getValue());
    if (Strings.isNullOrEmpty(groups.getText().trim())) {
      provider.clearGroups();
    } else {
      provider.setGroups(groups.getText());
    }
    if (Strings.isNullOrEmpty(usernameClaim.getText().trim())) {
      provider.clearUsernameClaim();
    } else {
      provider.setUsernameClaim(usernameClaim.getText());
    }
    provider.clearGroupsClaim();
    provider.clearGroupsScript();
    if ("claim".equals(groupsMapping.getSelectedValue())) {
      provider.setGroupsClaim(groupsClaim.getText());
    } else {
      provider.setGroupsScript(groupsScript.getText());
    }
    provider.setProviderUrl(providerUrl.getValue());
    provider.setLabel(label.getValue());
    provider.setUseNonce(useNonce.getValue());
    provider.setConnectTimeout(connectTimeout.hasValue() ? connectTimeout.getNumberValue().intValue() : 0);
    provider.setReadTimeout(readTimeout.hasValue() ? readTimeout.getNumberValue().intValue() : 0);
    provider.setCallbackURL(callbackUrl.getText());
    getUiHandlers().save(provider);
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAME:
          group = nameGroup;
          break;
        case CLIENT_ID:
          group = clientIdGroup;
          break;
        case SECRET:
          group = secretGroup;
          break;
        case DISCOVERY_URI:
          group = discoveryUriGroup;
          break;
        case PROVIDER_URL:
          group = providerUrlGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  @Override
  public void clearErrors() {
    modal.clearAlert();
  }
}
