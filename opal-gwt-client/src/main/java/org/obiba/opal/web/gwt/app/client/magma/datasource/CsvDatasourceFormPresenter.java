/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.datasource;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidatablePresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;

public class CsvDatasourceFormPresenter extends ValidatablePresenterWidget<CsvDatasourceFormPresenter.Display>
    implements DatasourceFormPresenter {

  public static class Subscriber extends DatasourceFormPresenterSubscriber {

    @Inject
    public Subscriber(EventBus eventBus, CsvDatasourceFormPresenter presenter) {
      super(eventBus, presenter);
    }

  }

  private static final String DEFAULT_TABLE_NAME = "table";

  private final FileSelectionPresenter csvFileSelectionPresenter;

  private final List<String> availableCharsets = new ArrayList<String>();

  private HasText selectedFile;

  @Inject
  public CsvDatasourceFormPresenter(Display display, EventBus eventBus,
      FileSelectionPresenter csvFileSelectionPresenter) {
    super(eventBus, display);
    this.csvFileSelectionPresenter = csvFileSelectionPresenter;

    addValidator(new RequiredTextValidator(getSelectedFile(), "NoDataFileSelected"));
    addValidator(new RegExValidator(getView().getRowText(), "^[1-9]\\d*$", "RowMustBePositiveInteger"));
    addValidator(new RequiredTextValidator(getView().getCharsetText(), "CharsetNotAvailable"));
  }

  @Override
  public PresenterWidget<? extends DatasourceFormPresenter.Display> getPresenter() {
    return this;
  }

  @Override
  protected void onBind() {
    csvFileSelectionPresenter.bind();
    csvFileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);

    getView().setCsvFileSelectorWidgetDisplay(csvFileSelectionPresenter.getView());
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    getDefaultCharset();
    getAvailableCharsets();
  }

  @Override
  protected void onUnbind() {
    csvFileSelectionPresenter.unbind();
  }

  @Override
  public DatasourceFactoryDto getDatasourceFactory() {
    CsvDatasourceFactoryDto extensionDto = createCsvDatasourceFactoryDto();

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(CsvDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

    return dto;
  }

  @Override
  public boolean isForType(String type) {
    return "csv".equalsIgnoreCase(type);
  }

  private CsvDatasourceFactoryDto createCsvDatasourceFactoryDto() {
    CsvDatasourceFactoryDto extensionDto = CsvDatasourceFactoryDto.create();

    if(!getView().getRowText().getText().trim().isEmpty()) {
      extensionDto.setFirstRow(Integer.parseInt(getView().getRowText().getText()));
    }

    extensionDto.setSeparator(getView().getFieldSeparator().getText());
    extensionDto.setQuote(getView().getQuote().getText());

    String charset = getCharset();
    if(charset != null) {
      extensionDto.setCharacterSet(charset);
    }

    extensionDto.setTablesArray(createCsvDatasourceTableBundleDtoArray());

    return extensionDto;
  }

  @SuppressWarnings("unchecked")
  private JsArray<CsvDatasourceTableBundleDto> createCsvDatasourceTableBundleDtoArray() {
    JsArray<CsvDatasourceTableBundleDto> tableBundleDtoArray = (JsArray<CsvDatasourceTableBundleDto>) JsArray
        .createArray();

    CsvDatasourceTableBundleDto csvDatasourceTableBundleDto = CsvDatasourceTableBundleDto.create();
    csvDatasourceTableBundleDto.setName(DEFAULT_TABLE_NAME);
    csvDatasourceTableBundleDto.setData(csvFileSelectionPresenter.getSelectedFile());

    tableBundleDtoArray.push(csvDatasourceTableBundleDto);

    return tableBundleDtoArray;
  }

  private String getCharset() {
    return getView().getCharsetText().getText();
  }

  private void getDefaultCharset() {
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder() //
        .forResource(UriBuilders.SYSTEM_CHARSET.create().build()) //
        .accept("text/plain")
        .get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setDefaultCharset(response.getText());
          }
        }, Response.SC_OK).send();
  }

  private void getAvailableCharsets() {
    ResourceRequestBuilderFactory.<JsArrayString>newBuilder().forResource("/files/charsets/available").get()
        .withCallback(new ResourceCallback<JsArrayString>() {
          @Override
          public void onResource(Response response, JsArrayString datasources) {
            for(int i = 0; i < datasources.length(); i++) {
              availableCharsets.add(datasources.get(i));
            }
          }
        }).send();
  }

  private HasText getSelectedFile() {
    if(selectedFile == null) {
      selectedFile = new HasText() {

        @Override
        public String getText() {
          return csvFileSelectionPresenter.getSelectedFile();
        }

        @Override
        public void setText(String text) {
          // do nothing
        }
      };
    }
    return selectedFile;
  }

  //
  // Interfaces and Inner Classes
  //

  public interface Display extends DatasourceFormPresenter.Display {

    void setCsvFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    HasText getRowText();

    HasText getCharsetText();

    void setDefaultCharset(String charset);

    HasText getQuote();

    HasText getFieldSeparator();

    HasText getDefaultValueType();

    void resetQuote();

    void resetFieldSeparator();

    void resetCommonCharset();

    void clearForm();
  }

  @Override
  public boolean validateFormData() {
    return validate();
  }

  @Override
  public void clearForm() {
    getView().clearForm();
  }
}
