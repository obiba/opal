/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.apps.rock;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.administration.apps.event.RockAppConfigAddEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.model.client.opal.AppCredentialsDto;
import org.obiba.opal.web.model.client.opal.RockAppConfigDto;

import javax.annotation.Nullable;

public class RockAppConfigModalPresenter extends ModalPresenterWidget<RockAppConfigModalPresenter.Display> implements RockAppConfigModalUiHandlers {

  @Inject
  public RockAppConfigModalPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  public void setConfig(RockAppConfigDto rockConfig) {
    getView().renderConfig(rockConfig);
  }

  @Override
  public void onSave(String url) {
    if (Strings.isNullOrEmpty(url))
      getView().showError(Display.FormField.HOST, "URL of the Rock server is required.");
    else {
      RockAppConfigDto dto = RockAppConfigDto.create();
      dto.setHost(url);
      fireEvent(new RockAppConfigAddEvent(dto));
      getView().hide();
    }
  }

  @Override
  public void onSave(String url, String administratorUsername, String administratorPassword) {
    boolean error = false;
    if (Strings.isNullOrEmpty(url)) {
      getView().showError(Display.FormField.HOST, "URL of the Rock server is required.");
      error = true;
    }
    if (Strings.isNullOrEmpty(administratorUsername)) {
      getView().showError(Display.FormField.ADMINISTRATOR_NAME, "Administrator name is required.");
      error = true;
    }
    if (Strings.isNullOrEmpty(administratorPassword)) {
      getView().showError(Display.FormField.ADMINISTRATOR_PASSWORD, "Administrator password is required.");
      error = true;
    }
    if (error) return;
    RockAppConfigDto dto = RockAppConfigDto.create();
    dto.setHost(url);
    AppCredentialsDto credentials = AppCredentialsDto.create();
    credentials.setName(administratorUsername);
    credentials.setPassword(administratorPassword);
    dto.setAdministratorCredentials(credentials);
    fireEvent(new RockAppConfigAddEvent(dto));
    getView().hide();
  }

  @Override
  public void onSave(String url, String managerUsername, String managerPassword, String userUsername, String userPassword) {
    boolean error = false;
    if (Strings.isNullOrEmpty(url)) {
      getView().showError(Display.FormField.HOST, "URL of the Rock server is required.");
      error = true;
    }
    if (Strings.isNullOrEmpty(managerUsername)) {
      getView().showError(Display.FormField.MANAGER_NAME, "Manager name is required.");
      error = true;
    }
    if (Strings.isNullOrEmpty(managerPassword)) {
      getView().showError(Display.FormField.MANAGER_PASSWORD, "Manager password is required.");
      error = true;
    }
    if (Strings.isNullOrEmpty(userUsername)) {
      getView().showError(Display.FormField.USER_NAME, "User name is required.");
      error = true;
    }
    if (Strings.isNullOrEmpty(userPassword)) {
      getView().showError(Display.FormField.USER_PASSWORD, "User password is required.");
      error = true;
    }
    if (error) return;
    RockAppConfigDto dto = RockAppConfigDto.create();
    dto.setHost(url);
    AppCredentialsDto credentials = AppCredentialsDto.create();
    credentials.setName(managerUsername);
    credentials.setPassword(managerPassword);
    dto.setManagerCredentials(credentials);
    credentials = AppCredentialsDto.create();
    credentials.setName(userUsername);
    credentials.setPassword(userPassword);
    dto.setUserCredentials(credentials);
    fireEvent(new RockAppConfigAddEvent(dto));
    getView().hide();
  }

  public interface Display extends PopupView, HasUiHandlers<RockAppConfigModalUiHandlers> {

    enum FormField {
      HOST,
      ADMINISTRATOR_NAME, ADMINISTRATOR_PASSWORD,
      MANAGER_NAME, MANAGER_PASSWORD,
      USER_NAME, USER_PASSWORD
    }

    void renderConfig(RockAppConfigDto rockConfig);

    void showError(@Nullable FormField formField, String message);
  }
}
