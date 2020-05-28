/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.view;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FilePlacesPresenter.Display;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FilePlacesUiHandler;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class FilePlacesView extends ViewWithUiHandlers<FilePlacesUiHandler> implements Display {

  interface Binder extends UiBinder<Widget, FilePlacesView> {}

  @UiField
  NavLink projectHome;

  @Inject
  public FilePlacesView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void showProjectHome(boolean visible) {
    projectHome.setVisible(visible);
  }

  @UiHandler("userHome")
  public void onUserHomeSelection(ClickEvent event) {
    getUiHandlers().onUserHomeSelection();
  }

  @UiHandler("projectHome")
  public void onProjectHomeSelection(ClickEvent event) {
    getUiHandlers().onProjectHomeSelection();
  }

  @UiHandler("root")
  public void onRootSelection(ClickEvent event) {
    getUiHandlers().onFileSystemSelection();
  }

  @UiHandler("users")
  public void onUsersSelection(ClickEvent event) {
    getUiHandlers().onUsersSelection();
  }

  @UiHandler("projects")
  public void onProjectsSelection(ClickEvent event) {
    getUiHandlers().onProjectsSelection();
  }

  @UiHandler("reports")
  public void onReportsSelection(ClickEvent event) {
    getUiHandlers().onReportsSelection();
  }
}
