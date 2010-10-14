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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionUpdateEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.client.magma.FsDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class XmlFormatStepPresenter extends WidgetPresenter<XmlFormatStepPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    void setXmlFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    String getSelectedFile();

    void setNextEnabled(boolean enabled);

    void setUnits(JsArray<FunctionalUnitDto> units);

    String getSelectedUnit();

  }

  @Inject
  private ImportData importData;

  @Inject
  private ValidationReportStepPresenter validationReportStepPresenter;

  @Inject
  private DestinationSelectionStepPresenter destinationSelectionStepPresenter;

  @Inject
  private FileSelectionPresenter xmlFileSelectionPresenter;

  @Inject
  public XmlFormatStepPresenter(final Display display, final EventBus eventBus) {
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

    xmlFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE_OR_FOLDER);
    xmlFileSelectionPresenter.bind();
    getDisplay().setXmlFileSelectorWidgetDisplay(xmlFileSelectionPresenter.getDisplay());
    getDisplay().setNextEnabled(false);
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
    super.registerHandler(eventBus.addHandler(FileSelectionUpdateEvent.getType(), new FileSelectionUpdateHandler()));
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

  class NextClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      importData.setXmlFile(getDisplay().getSelectedFile());
      setIdentityImportData();
      createTransientDatasource();
    }

    private void setIdentityImportData() {
      if(getDisplay().getSelectedUnit().equals("")) {
        importData.setIdentifierAsIs(true);
        importData.setIdentifierSharedWithUnit(false);
      } else {
        importData.setIdentifierAsIs(false);
        importData.setIdentifierSharedWithUnit(true);
        importData.setUnit(getDisplay().getSelectedUnit());
      }
    }
  }

  public void createTransientDatasource() {

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 201) {
          DatasourceDto datasourceDto = (DatasourceDto) JsonUtils.unsafeEval(response.getText());
          importData.setTransientDatasourceName(datasourceDto.getName());
          eventBus.fireEvent(new WorkbenchChangeEvent(destinationSelectionStepPresenter));
        } else {
          final ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          if(errorDto.getExtension(ClientErrorDtoExtensions.errors) != null) {
            validationReportStepPresenter.getDisplay().setParsingErrors(extractDatasourceParsingErrors(errorDto));
            eventBus.fireEvent(new WorkbenchChangeEvent(validationReportStepPresenter));
          } else {
            eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "fileReadError", null));
          }
        }
      }
    };

    DatasourceFactoryDto dto = createDatasourceFactoryDto();
    ResourceRequestBuilderFactory.<DatasourceFactoryDto> newBuilder().forResource("/datasources").post().withResourceBody(DatasourceFactoryDto.stringify(dto)).withCallback(201, callbackHandler).withCallback(400, callbackHandler).withCallback(500, callbackHandler).send();
  }

  @SuppressWarnings("unchecked")
  private List<DatasourceParsingErrorDto> extractDatasourceParsingErrors(ClientErrorDto dto) {
    List<DatasourceParsingErrorDto> datasourceParsingErrors = new ArrayList<DatasourceParsingErrorDto>();

    JsArray<DatasourceParsingErrorDto> errors = (JsArray<DatasourceParsingErrorDto>) dto.getExtension(ClientErrorDtoExtensions.errors);
    if(errors != null) {
      for(int i = 0; i < errors.length(); i++) {
        datasourceParsingErrors.add(errors.get(i));
      }
    }

    return datasourceParsingErrors;
  }

  private DatasourceFactoryDto createDatasourceFactoryDto() {

    FsDatasourceFactoryDto fsDatasourceFactoryDto = FsDatasourceFactoryDto.create();
    fsDatasourceFactoryDto.setFile(importData.getXmlFile());
    fsDatasourceFactoryDto.setUnit(importData.getUnit());

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(FsDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, fsDatasourceFactoryDto);

    return dto;

  }

  private void enableImport() {
    getDisplay().setNextEnabled(xmlFileSelectionPresenter.getSelectedFile().length() > 0);
  }

  class FileSelectionUpdateHandler implements FileSelectionUpdateEvent.Handler {
    @Override
    public void onFileSelectionUpdate(FileSelectionUpdateEvent event) {
      enableImport();
    }
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
