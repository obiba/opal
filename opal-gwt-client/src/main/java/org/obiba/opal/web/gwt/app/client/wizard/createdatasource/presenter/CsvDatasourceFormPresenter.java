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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

/**
 *
 */
public class CsvDatasourceFormPresenter extends WidgetPresenter<CsvDatasourceFormPresenter.Display> implements DatasourceFormPresenter {
  //
  // Constants
  //

  private static final String DEFAULT_TABLE_NAME = "table";

  //
  // Instance Variables
  //

  @Inject
  private FileSelectionPresenter csvFileSelectionPresenter;

  private HasText selectedFile;

  private List<String> availableCharsets = new ArrayList<String>();

  private Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

  //
  // Constructors
  //

  @Inject
  public CsvDatasourceFormPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);

    validators.add(new RequiredTextValidator(getSelectedFile(), "NoDataFileSelected"));
    validators.add(new RegExValidator(getDisplay().getRowText(), "^[1-9]\\d*$", "RowMustBePositiveInteger"));
    validators.add(new ConditionalValidator(getDisplay().isCharsetSpecify(), new RequiredTextValidator(getDisplay().getCharsetSpecifyText(), "SpecificCharsetNotIndicated")));
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    csvFileSelectionPresenter.bind();
    csvFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);

    getDisplay().setCsvFileSelectorWidgetDisplay(csvFileSelectionPresenter.getDisplay());

    getDefaultCharset();
    getAvailableCharsets();
  }

  @Override
  protected void onUnbind() {
    csvFileSelectionPresenter.unbind();
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // DatasourceFormPresenter Methods
  //

  public DatasourceFactoryDto getDatasourceFactory() {
    CsvDatasourceFactoryDto extensionDto = createCsvDatasourceFactoryDto();

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(CsvDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

    return dto;
  }

  public boolean isForType(String type) {
    return type.equalsIgnoreCase("CSV");
  }

  //
  // Methods
  //

  private CsvDatasourceFactoryDto createCsvDatasourceFactoryDto() {
    CsvDatasourceFactoryDto extensionDto = CsvDatasourceFactoryDto.create();

    if(getDisplay().getRowText().getText().trim().length() != 0) {
      extensionDto.setFirstRow(Integer.parseInt(getDisplay().getRowText().getText()));
    }

    extensionDto.setSeparator(getDisplay().getFieldSeparator());
    extensionDto.setQuote(getDisplay().getQuote());

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

    if(!getDisplay().isDefaultCharacterSet().getValue()) {
      if(getDisplay().isCharsetCommonList().getValue()) {
        charset = getDisplay().getCharsetCommonList();
      } else if(getDisplay().isCharsetSpecify().getValue()) {
        charset = getDisplay().getCharsetSpecifyText().getText();
      }
    }

    return charset;
  }

  private void getDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString> newBuilder().forResource("/files/charsets/default").get().withCallback(new ResourceCallback<JsArrayString>() {

      @Override
      public void onResource(Response response, JsArrayString resource) {
        String charset = resource.get(0);
        getDisplay().setDefaultCharset(charset);
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
  }

  @Override
  public boolean validate() {
    for(FieldValidator validator : validators) {
      String error = validator.validate();
      if(error != null) {
        fireErrorEvent(error);
        return false;
      }
    }

    return true;
  }

  private void fireErrorEvent(String error) {
    eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, error, null));
  }
}
