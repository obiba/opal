/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidatablePresenterWidget;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;

public class CsvDatasourceFormPresenter extends ValidatablePresenterWidget<CsvDatasourceFormPresenter.Display> implements DatasourceFormPresenter {

  public static class Subscriber extends DatasourceFormPresenterSubscriber {

    @Inject
    public Subscriber(com.google.gwt.event.shared.EventBus eventBus, CsvDatasourceFormPresenter presenter) {
      super(eventBus, presenter);
    }

  }

  private static final String DEFAULT_TABLE_NAME = "table";

  //
  // Instance Variables
  //

  private final FileSelectionPresenter csvFileSelectionPresenter;

  private final List<String> availableCharsets = new ArrayList<String>();

  private HasText selectedFile;

  private HasValue<Boolean> isSpecificCharsetAvailable;

  //
  // Constructors
  //

  @Inject
  public CsvDatasourceFormPresenter(final Display display, final EventBus eventBus, FileSelectionPresenter csvFileSelectionPresenter) {
    super(eventBus, display);
    this.csvFileSelectionPresenter = csvFileSelectionPresenter;

    addValidator(new RequiredTextValidator(getSelectedFile(), "NoDataFileSelected"));
    addValidator(new RegExValidator(getView().getRowText(), "^[1-9]\\d*$", "RowMustBePositiveInteger"));
    addValidator(new ConditionalValidator(getView().isCharsetSpecify(), new RequiredTextValidator(getView().getCharsetSpecifyText(), "SpecificCharsetNotIndicated")));
    addValidator(new ConditionalValidator(getView().isCharsetSpecify(), new ConditionValidator(isSpecificCharsetAvailable(), "CharsetNotAvailable")));
  }

  @Override
  public PresenterWidget<? extends org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceFormPresenter.Display> getPresenter() {
    return this;
  }

  @Override
  protected void onBind() {
    csvFileSelectionPresenter.bind();
    csvFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);

    getView().setCsvFileSelectorWidgetDisplay(csvFileSelectionPresenter.getDisplay());

    getDefaultCharset();
    getAvailableCharsets();
  }

  @Override
  protected void onUnbind() {
    csvFileSelectionPresenter.unbind();
  }

  public DatasourceFactoryDto getDatasourceFactory() {
    CsvDatasourceFactoryDto extensionDto = createCsvDatasourceFactoryDto();

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(CsvDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

    return dto;
  }

  public boolean isForType(String type) {
    return type.equalsIgnoreCase("csv");
  }

  private CsvDatasourceFactoryDto createCsvDatasourceFactoryDto() {
    CsvDatasourceFactoryDto extensionDto = CsvDatasourceFactoryDto.create();

    if(getView().getRowText().getText().trim().length() != 0) {
      extensionDto.setFirstRow(Integer.parseInt(getView().getRowText().getText()));
    }

    extensionDto.setSeparator(getView().getFieldSeparator());
    extensionDto.setQuote(getView().getQuote());

    String charset = getCharset();
    if(charset != null) {
      extensionDto.setCharacterSet(charset);
    }

    extensionDto.setTablesArray(createCsvDatasourceTableBundleDtoArray());

    return extensionDto;
  }

  @SuppressWarnings("unchecked")
  private JsArray<CsvDatasourceTableBundleDto> createCsvDatasourceTableBundleDtoArray() {
    JsArray<CsvDatasourceTableBundleDto> tableBundleDtoArray = (JsArray<CsvDatasourceTableBundleDto>) JsArray.createArray();

    CsvDatasourceTableBundleDto csvDatasourceTableBundleDto = CsvDatasourceTableBundleDto.create();
    csvDatasourceTableBundleDto.setName(DEFAULT_TABLE_NAME);
    csvDatasourceTableBundleDto.setData(csvFileSelectionPresenter.getSelectedFile());

    tableBundleDtoArray.push(csvDatasourceTableBundleDto);

    return tableBundleDtoArray;
  }

  private String getCharset() {
    String charset = null;

    if(!getView().isDefaultCharacterSet().getValue()) {
      if(getView().isCharsetCommonList().getValue()) {
        charset = getView().getCharsetCommonList();
      } else if(getView().isCharsetSpecify().getValue()) {
        charset = getView().getCharsetSpecifyText().getText();
      }
    }

    return charset;
  }

  private void getDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString> newBuilder().forResource("/files/charsets/default").get().withCallback(new ResourceCallback<JsArrayString>() {

      @Override
      public void onResource(Response response, JsArrayString resource) {
        String charset = resource.get(0);
        getView().setDefaultCharset(charset);
      }
    }).send();

  }

  private void getAvailableCharsets() {
    ResourceRequestBuilderFactory.<JsArrayString> newBuilder().forResource("/files/charsets/available").get().withCallback(new ResourceCallback<JsArrayString>() {
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

        public String getText() {
          return csvFileSelectionPresenter.getSelectedFile();
        }

        public void setText(String text) {
          // do nothing
        }
      };
    }
    return selectedFile;
  }

  private HasValue<Boolean> isSpecificCharsetAvailable() {
    if(isSpecificCharsetAvailable == null) {
      isSpecificCharsetAvailable = new HasValue<Boolean>() {

        public Boolean getValue() {
          return availableCharsets.contains(getView().getCharsetSpecifyText().getText());
        }

        public void setValue(Boolean arg0) {
        }

        public void setValue(Boolean arg0, boolean arg1) {
        }

        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> arg0) {
          return null;
        }

        public void fireEvent(GwtEvent<?> arg0) {
        }
      };
    }
    return isSpecificCharsetAvailable;
  }

  //
  // Interfaces and Inner Classes
  //

  public interface Display extends DatasourceFormPresenter.Display {

    void setCsvFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    HasText getRowText();

    void setDefaultCharset(String charset);

    String getQuote();

    String getFieldSeparator();

    HasValue<Boolean> isDefaultCharacterSet();

    HasValue<Boolean> isCharsetCommonList();

    String getCharsetCommonList();

    HasValue<Boolean> isCharsetSpecify();

    HasText getCharsetSpecifyText();

    void resetQuote();

    void resetFieldSeparator();

    void resetCommonCharset();

    void clearForm();
  }

  public boolean validateFormData() {
    return validate();
  }

  @Override
  public void clearForm() {
    getView().clearForm();
  }
}
