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
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class IdentityArchiveStepPresenter extends WidgetPresenter<IdentityArchiveStepPresenter.Display> {

  public interface Display extends WidgetDisplay, DataImportPresenter.DataImportStepDisplay {

    boolean isIdentifierAsIs();

    void setIdentifierAsIs(boolean checked);

    boolean isIdentifierSharedWithUnit();

    void setIdentifierSharedWithUnit(boolean checked);

    void setUnits(JsArray<FunctionalUnitDto> units);

    String getSelectedUnit();

    void setSelectedUnit(String unit);

    HandlerRegistration addIdentifierAsIsClickHandler(ClickHandler handler);

    HandlerRegistration addIdentifierSharedWithUnitClickHandler(ClickHandler handler);

    boolean isArchiveLeave();

    boolean isArchiveMove();

    void setArchiveWidgetDisplay(FileSelectionPresenter.Display display);

    String getArchiveDirectory();

    HandlerRegistration addArchiveLeaveClickHandler(ClickHandler handler);

    HandlerRegistration addArchiveMoveClickHandler(ClickHandler handler);

    void setUnitEnabled(boolean enabled);

    /** Allows the identity (unit) section of the form to be enabled and disabled. */
    void setIdentityEnabled(boolean enabled);

  }

  @Inject
  private ImportData importData;

  @Inject
  private FileSelectionPresenter archiveFolderSelectionPresenter;

  @Inject
  private ConclusionStepPresenter conclusionStepPresenter;

  @Inject
  public IdentityArchiveStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    initUnits();

    archiveFolderSelectionPresenter.setFileSelectionType(FileSelectionType.FOLDER);
    archiveFolderSelectionPresenter.bind();
    getDisplay().setArchiveWidgetDisplay(archiveFolderSelectionPresenter.getDisplay());
  }

  protected void addEventHandlers() {
    addIdentifierEventHandlers();
    addArchiveEventHandlers();
  }

  public void updateImportData(ImportData importData) {
    importData.setIdentifierAsIs(getDisplay().isIdentifierAsIs());
    importData.setIdentifierSharedWithUnit(getDisplay().isIdentifierSharedWithUnit());
    importData.setUnit(getDisplay().getSelectedUnit());

    importData.setArchiveLeave(getDisplay().isArchiveLeave());
    importData.setArchiveMove(getDisplay().isArchiveMove());
    importData.setArchiveDirectory(getDisplay().getArchiveDirectory());
  }

  private void addIdentifierEventHandlers() {
    super.registerHandler(getDisplay().addIdentifierAsIsClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getDisplay().setUnitEnabled(false);
      }
    }));
    super.registerHandler(getDisplay().addIdentifierSharedWithUnitClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getDisplay().setUnitEnabled(true);
      }
    }));
  }

  private boolean isIdentifierAlreadyProvided() {
    return importData.isIdentifierAsIs() || importData.isIdentifierSharedWithUnit();
  }

  private void addArchiveEventHandlers() {
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

  public void initUnits() {
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>> newBuilder().forResource("/functional-units").get().withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
      @Override
      public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
        getDisplay().setUnits(units);
      }
    }).send();
  }

}
