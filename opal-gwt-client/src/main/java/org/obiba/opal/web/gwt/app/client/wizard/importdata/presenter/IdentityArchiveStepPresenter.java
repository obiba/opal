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

import java.util.Arrays;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.ImportCommandOptionsDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class IdentityArchiveStepPresenter extends WidgetPresenter<IdentityArchiveStepPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    boolean isIdentifierAsIs();

    boolean isIdentifierSharedWithUnit();

    void setUnits(JsArray<FunctionalUnitDto> units);

    String getSelectedUnit();

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
  private ErrorDialogPresenter errorDialog;

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
    super.registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
    addIdentifierEventHandlers();
    addArchiveEventHandlers();
  }

  private void addIdentifierEventHandlers() {
    if(isIdentifierAlreadyProvided()) {
      getDisplay().setIdentityEnabled(false);
    } else {
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

  class NextClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      importData.setIdentifierAsIs(getDisplay().isIdentifierAsIs());
      importData.setIdentifierSharedWithUnit(getDisplay().isIdentifierSharedWithUnit());
      importData.setUnit(getDisplay().getSelectedUnit());
      importData.setArchiveLeave(getDisplay().isArchiveLeave());
      importData.setArchiveMove(getDisplay().isArchiveMove());
      importData.setArchiveDirectory(getDisplay().getArchiveDirectory());
      if(importData.getImportFormat().equals(ImportFormat.XML)) {
        submitJob(createXmlImportCommandOptionsDto());
      } else if(importData.getImportFormat().equals(ImportFormat.CSV)) {
        submitJob(createCsvImportCommandOptionsDto());
      }
    }

  }

  private void submitJob(ImportCommandOptionsDto dto) {
    ResourceRequestBuilderFactory.newBuilder().forResource("/shell/import").post() //
    .withResourceBody(ImportCommandOptionsDto.stringify(dto)) //
    .withCallback(400, new ClientFailureResponseCodeCallBack()) //
    .withCallback(201, new SuccessResponseCodeCallBack()).send();
  }

  private ImportCommandOptionsDto createXmlImportCommandOptionsDto() {
    ImportCommandOptionsDto dto = ImportCommandOptionsDto.create();
    dto.setDestination(importData.getDestinationDatasourceName());
    dto.setArchive(importData.getArchiveDirectory());
    JsArrayString selectedFiles = JavaScriptObject.createArray().cast();
    selectedFiles.push(importData.getXmlFile());
    dto.setFilesArray(selectedFiles);
    if(importData.isIdentifierSharedWithUnit()) dto.setUnit(importData.getUnit());
    return dto;
  }

  private ImportCommandOptionsDto createCsvImportCommandOptionsDto() {
    ImportCommandOptionsDto dto = ImportCommandOptionsDto.create();
    dto.setDestination(importData.getDestinationDatasourceName());
    if(importData.isArchiveMove()) {
      dto.setArchive(importData.getArchiveDirectory());
      JsArrayString selectedFiles = JavaScriptObject.createArray().cast();
      selectedFiles.push(importData.getCsvFile());
      dto.setFilesArray(selectedFiles);
    }
    if(importData.isIdentifierSharedWithUnit()) dto.setUnit(importData.getUnit());
    dto.setSource(importData.getTransientDatasourceName());
    return dto;
  }

  class ClientFailureResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      errorDialog.bind();
      errorDialog.setErrors(Arrays.asList(new String[] { response.getText() }));
      errorDialog.revealDisplay();
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      importData.setJobId(jobId);

      eventBus.fireEvent(new WorkbenchChangeEvent(conclusionStepPresenter));
    }
  }

}
