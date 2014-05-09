/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRequestEvent;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FilePlacesPresenter extends PresenterWidget<FilePlacesPresenter.Display> implements FilePlacesUiHandler {

  private final RequestCredentials credentials;

  private String project;

  @Inject
  public FilePlacesPresenter(Display display, EventBus eventBus, RequestCredentials credentials) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.credentials = credentials;
    getView().showProjectHome(false);
  }

  @Override
  public void onUserHomeSelection() {
    fireEvent(new FolderRequestEvent(FileDtos.user(credentials.getUsername())));
  }

  @Override
  public void onProjectHomeSelection() {
    if(project != null) {
      fireEvent(new FolderRequestEvent(FileDtos.project(project)));
    }
  }

  @Override
  public void onFileSystemSelection() {
    fireEvent(new FolderRequestEvent(FileDtos.create()));
  }

  @Override
  public void onUsersSelection() {
    fireEvent(new FolderRequestEvent(FileDtos.users()));
  }

  @Override
  public void onProjectsSelection() {
    fireEvent(new FolderRequestEvent(FileDtos.projects()));
  }

  @Override
  public void onReportsSelection() {
    fireEvent(new FolderRequestEvent(FileDtos.reports()));
  }

  public void showProject(@SuppressWarnings("ParameterHidesMemberVariable") String project) {
    this.project = project;
    getView().showProjectHome(project != null);
  }

  public interface Display extends View, HasUiHandlers<FilePlacesUiHandler> {

    void showProjectHome(boolean visible);

  }
}
