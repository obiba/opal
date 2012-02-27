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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

public class ArchiveStepPresenter extends WidgetPresenter<ArchiveStepPresenter.Display> {

  public interface Display extends WidgetDisplay, WizardStepDisplay {

    boolean isArchiveLeave();

    boolean isArchiveMove();

    void setArchiveWidgetDisplay(FileSelectionPresenter.Display display);

    String getArchiveDirectory();

    HandlerRegistration addArchiveLeaveClickHandler(ClickHandler handler);

    HandlerRegistration addArchiveMoveClickHandler(ClickHandler handler);
  }

  @Inject
  private FileSelectionPresenter archiveFolderSelectionPresenter;

  @Inject
  public ArchiveStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();

    archiveFolderSelectionPresenter.setFileSelectionType(FileSelectionType.FOLDER);
    archiveFolderSelectionPresenter.bind();
    getDisplay().setArchiveWidgetDisplay(archiveFolderSelectionPresenter.getDisplay());
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addArchiveLeaveClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        archiveFolderSelectionPresenter.getDisplay().setEnabled(false);
      }
    }));
    super.registerHandler(getDisplay().addArchiveMoveClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        archiveFolderSelectionPresenter.getDisplay().setEnabled(true);
      }
    }));
  }

  public void updateImportData(ImportData importData) {
    importData.setArchiveLeave(getDisplay().isArchiveLeave());
    importData.setArchiveMove(getDisplay().isArchiveMove());
    importData.setArchiveDirectory(getDisplay().getArchiveDirectory());
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

}
