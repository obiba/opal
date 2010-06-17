/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.inject.client;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.fs.view.FileExplorerView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileSystemTreeView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileUploadDialogView;
import org.obiba.opal.web.gwt.app.client.fs.view.FolderDetailsView;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 *
 */
public class FileSystemModule extends AbstractGinModule {

  @Override
  protected void configure() {
    // Bind concrete implementations to interfaces
    bind(FolderDetailsPresenter.Display.class).to(FolderDetailsView.class);
    bind(FileSystemTreePresenter.Display.class).to(FileSystemTreeView.class);
    bind(FileExplorerPresenter.Display.class).to(FileExplorerView.class).in(Singleton.class);
    bind(FileUploadDialogPresenter.Display.class).to(FileUploadDialogView.class);
  }

}
