/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ArchiveStepPresenter extends PresenterWidget<ArchiveStepPresenter.Display> {

  private final FileSelectionPresenter archiveFolderSelectionPresenter;

  @Inject
  public ArchiveStepPresenter(final EventBus eventBus, final Display display,
      FileSelectionPresenter archiveFolderSelectionPresenter) {
    super(eventBus, display);
    this.archiveFolderSelectionPresenter = archiveFolderSelectionPresenter;
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();

    archiveFolderSelectionPresenter.setFileSelectionType(FileSelectionType.FOLDER);
    archiveFolderSelectionPresenter.bind();
    getView().setArchiveWidgetDisplay(archiveFolderSelectionPresenter.getDisplay());
  }

  protected void addEventHandlers() {
    super.registerHandler(getView().addArchiveLeaveClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        archiveFolderSelectionPresenter.getDisplay().setEnabled(false);
      }
    }));
    super.registerHandler(getView().addArchiveMoveClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        archiveFolderSelectionPresenter.getDisplay().setEnabled(true);
      }
    }));
  }

  public void updateImportData(ImportConfig importConfig) {
    importConfig.setArchiveLeave(getView().isArchiveLeave());
    importConfig.setArchiveMove(getView().isArchiveMove());
    importConfig.setArchiveDirectory(getView().getArchiveDirectory());
  }

  public interface Display extends View {

    boolean isArchiveLeave();

    boolean isArchiveMove();

    void setArchiveWidgetDisplay(FileSelectionPresenter.Display display);

    String getArchiveDirectory();

    HandlerRegistration addArchiveLeaveClickHandler(ClickHandler handler);

    HandlerRegistration addArchiveMoveClickHandler(ClickHandler handler);
  }

}
