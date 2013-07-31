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

import org.obiba.opal.web.gwt.app.client.fs.presenter.CreateFolderModalPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FilePathPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSystemTreePresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileUploadModalPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FolderDetailsPresenter;
import org.obiba.opal.web.gwt.app.client.fs.view.CreateFolderModalView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileExplorerView;
import org.obiba.opal.web.gwt.app.client.fs.view.FilePathView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileSystemTreeView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileUploadModalView;
import org.obiba.opal.web.gwt.app.client.fs.view.FolderDetailsView;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.fs.view.FileSelectionView;
import org.obiba.opal.web.gwt.app.client.fs.view.FileSelectorView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
public class FileSystemModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenterWidget(FileExplorerPresenter.class, FileExplorerPresenter.Display.class, FileExplorerView.class);
    bindPresenterWidget(FolderDetailsPresenter.class, FolderDetailsPresenter.Display.class, FolderDetailsView.class);
    bindPresenterWidget(FileSystemTreePresenter.class, FileSystemTreePresenter.Display.class, FileSystemTreeView.class);
    bindPresenterWidget(FilePathPresenter.class, FilePathPresenter.Display.class, FilePathView.class);
    bindPresenterWidget(CreateFolderModalPresenter.class, CreateFolderModalPresenter.Display.class,
        CreateFolderModalView.class);
    bindPresenterWidget(FileUploadModalPresenter.class, FileUploadModalPresenter.Display.class,
        FileUploadModalView.class);
    bindPresenterWidget(FileSelectorPresenter.class, FileSelectorPresenter.Display.class, FileSelectorView.class);

    bind(FileSelectionPresenter.Display.class).to(FileSelectionView.class);
  }
}
