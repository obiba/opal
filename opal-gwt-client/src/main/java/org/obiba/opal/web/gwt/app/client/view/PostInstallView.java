/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.PostInstallPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.PostInstallUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Dropdown;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class PostInstallView extends ViewWithUiHandlers<PostInstallUiHandlers> implements PostInstallPresenter.Display {

  interface Binder extends UiBinder<Widget, PostInstallView> {}

  @UiField
  Dropdown username;

  @UiField
  Panel notification;

  @UiField
  Panel idsPanel;

  @UiField
  Panel dataPanel;

  @UiField
  Button gotoMain;

  @Inject
  public PostInstallView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == PostInstallPresenter.Slot.IDENTIFIERS) {
      idsPanel.clear();
      idsPanel.add(content);
    } else if(slot == PostInstallPresenter.Slot.DATA) {
      dataPanel.clear();
      dataPanel.add(content);
    } else if(slot == PostInstallPresenter.Slot.NOTIFICATION) {
      notification.clear();
      notification.add(content);
    }
  }

  @Override
  public void setUsername(String username) {
    this.username.setText(username);
  }

  @Override
  public void enablePageExit(boolean value) {
    gotoMain.setVisible(value);
  }

  @UiHandler("quitItem")
  void onQuit(ClickEvent event) {
    getUiHandlers().onQuit();
  }

  @UiHandler("gotoMain")
  void onGotToMain(ClickEvent event) {
    getUiHandlers().onGoToMain();
  }
}
