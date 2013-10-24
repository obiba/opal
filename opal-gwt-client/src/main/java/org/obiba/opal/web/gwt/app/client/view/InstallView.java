/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.InstallPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.InstallUiHandlers;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class InstallView extends ViewWithUiHandlers<InstallUiHandlers> implements InstallPresenter.Display {

  interface Binder extends UiBinder<Widget, InstallView> {}

  private final Translations translations;

  @UiField
  NavLink username;

  @UiField
  Panel notification;

  @UiField
  Panel idsPanel;

  @UiField
  Panel dataPanel;

  @Inject
  public InstallView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == InstallPresenter.Slot.IDENTIFIERS) {
      idsPanel.clear();
      idsPanel.add(content);
    } else if(slot == InstallPresenter.Slot.DATA) {
      dataPanel.clear();
      dataPanel.add(content);
    } else if(slot == InstallPresenter.Slot.NOTIFICATION) {
      notification.add(content);
    }
  }

  @Override
  public void setUsername(String username) {
    this.username.setText(username);
  }

  @UiHandler("helpItem")
  void onHelp(ClickEvent event) {
    getUiHandlers().onHelp();
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
