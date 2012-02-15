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

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.ImportCommandOptionsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.inject.Inject;

public class ConclusionStepPresenter extends WidgetPresenter<ConclusionStepPresenter.Display> {

  private static Translations translations = GWT.create(Translations.class);

  @Inject
  private ImportData importData;

  private DatasourceCreatedCallback transientDatasourceCreatedCallback;

  @Inject
  public ConclusionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    getDisplay().showJobId(importData.getJobId());
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addJobLinkClickHandler(new JobLinkClickHandler()));
  }

  public void setTransientDatasourceCreatedCallback(DatasourceCreatedCallback callback) {
    this.transientDatasourceCreatedCallback = callback;
  }

  public void reset() {
    getDisplay().hideErrors();
  }

  public void launchImport(ImportData importData) {
    this.importData = importData;

    if(importData.getImportFormat().equals(ImportFormat.XML)) {
      submitJob(createXmlImportCommandOptionsDto());
    } else if(importData.getImportFormat().equals(ImportFormat.CSV)) {
      submitJob(createCsvImportCommandOptionsDto());
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
      eventBus.fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      importData.setJobId(jobId);
      getDisplay().showJobId(jobId);
    }
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

  //
  // Inner classes
  //

  class JobLinkClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(new PlaceChangeEvent(Places.jobsPlace));
    }

  }

  public static class TableCompareError {

    private final String column;

    private final String errorMessageKey;

    public TableCompareError(String column, String errorMessageKey) {
      super();
      this.column = column;
      this.errorMessageKey = errorMessageKey;
    }

    public String getColumn() {
      return column;
    }

    public String getErrorMessageKey() {
      return errorMessageKey;
    }

  }

  //
  // Interfaces
  //

  public interface Display extends WidgetDisplay {

    HandlerRegistration addJobLinkClickHandler(ClickHandler handler);

    void showJobId(String text);

    public void showTableCompareErrors(final List<TableCompareError> errors);

    public void showDatasourceParsingErrors(ClientErrorDto errorDto);

    public void hideErrors();

  }

}
