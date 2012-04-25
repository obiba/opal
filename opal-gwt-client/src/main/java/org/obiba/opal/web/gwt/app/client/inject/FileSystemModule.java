/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.fs.presenter.CreateFolderDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadDialogPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.fs.view.CreateFolderDialogView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileExplorerView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileSystemTreeView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileUploadDialogView;
import org.obiba.opal.web.gwt.app.client.fs.view.FolderDetailsView;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.FileSelectionView;
import org.obiba.opal.web.gwt.app.client.widgets.view.FileSelectorView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
public class FileSystemModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(FileExplorerPresenter.class, FileExplorerPresenter.Display.class, FileExplorerView.class, FileExplorerPresenter.Proxy.class);
    bindPresenterWidget(FolderDetailsPresenter.class, FolderDetailsPresenter.Display.class, FolderDetailsView.class);
    bindPresenterWidget(FileSystemTreePresenter.class, FileSystemTreePresenter.Display.class, FileSystemTreeView.class);
    bindPresenterWidget(CreateFolderDialogPresenter.class, CreateFolderDialogPresenter.Display.class, CreateFolderDialogView.class);
    bindPresenterWidget(FileUploadDialogPresenter.class, FileUploadDialogPresenter.Display.class, FileUploadDialogView.class);
    bindPresenterWidget(FileSelectorPresenter.class, FileSelectorPresenter.Display.class, FileSelectorView.class);

    bind(FileSelectionPresenter.Display.class).to(FileSelectionView.class);
  }
}
