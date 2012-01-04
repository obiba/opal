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

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileDownloadPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.fs.view.FileDownloadView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileExplorerView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileSystemTreeView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileUploadDialogView;
import org.obiba.opal.web.gwt.app.client.fs.view.FolderDetailsView;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
public class FileSystemModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(FileExplorerPresenter.class, FileExplorerPresenter.Display.class, FileExplorerView.class, FileExplorerPresenter.Proxy.class);

    bind(FolderDetailsPresenter.Display.class).to(FolderDetailsView.class);
    bind(FileSystemTreePresenter.Display.class).to(FileSystemTreeView.class);
    bind(FileUploadDialogPresenter.Display.class).to(FileUploadDialogView.class);
    bind(FileDownloadPresenter.Display.class).to(FileDownloadView.class).in(Singleton.class);
  }
}
