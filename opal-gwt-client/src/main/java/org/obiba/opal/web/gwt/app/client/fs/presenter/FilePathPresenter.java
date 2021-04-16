/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FilePathPresenter extends PresenterWidget<FilePathPresenter.Display> {

  @Inject
  public FilePathPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(FolderUpdatedEvent.getType(), new FolderUpdatedEvent.FolderUpdatedHandler() {
      @Override
      public void onFolderUpdated(FolderUpdatedEvent event) {
        if(!isVisible()) return;

        getView().setFile(event.getFolder());
      }
    });
  }

  public interface Display extends View {

    void setFile(FileDto file);

  }
}
