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
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.NotificationCloseHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.FileSelectionUpdateEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.CsvOptionsDisplay;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class CsvFormatStepPresenter extends WidgetPresenter<CsvFormatStepPresenter.Display> {

  public interface Display extends CsvOptionsDisplay {

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    void setNextEnabled(boolean enabled);
  }

  private List<String> errors = new ArrayList<String>();

  private List<String> availableCharsets = new ArrayList<String>();

  private String defaultCharset;

  private static Translations translations = GWT.create(Translations.class);

  @Inject
  private ImportData importData;

  @Inject
  private DestinationSelectionStepPresenter destinationSelectionStepPresenter;

  @Inject
  private FileSelectionPresenter csvFileSelectionPresenter;

  @Inject
  public CsvFormatStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();

    getDefaultCharset();
    getAvailableCharsets();

    csvFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    csvFileSelectionPresenter.bind();
    getDisplay().setCsvFileSelectorWidgetDisplay(csvFileSelectionPresenter.getDisplay());
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
      int row = getValidatedRow();
      validateCharacterSet(getSelectedCharacterSet());

      if(errors.isEmpty()) {
        importData.setCsvFile(csvFileSelectionPresenter.getSelectedFile());
        importData.setRow(row);
        importData.setField(getDisplay().getFieldSeparator());
        importData.setQuote(getDisplay().getQuote());
        importData.setCharacterSet(getSelectedCharacterSet());
        eventBus.fireEvent(new WorkbenchChangeEvent(destinationSelectionStepPresenter));
      } else {
        eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, errors, null, new NotificationCloseHandler() {

          @Override
          public void onClose(CloseEvent<?> event) {
            errors.clear();
          }
        }));
      }
    }

  }

  private String getSelectedCharacterSet() {
    String charset = null;
    if(getDisplay().isDefaultCharacterSet().getValue()) {
      charset = defaultCharset;
    } else if(getDisplay().isCharsetCommonList().getValue()) {
      charset = getDisplay().getCharsetCommonList();
    } else if(getDisplay().isCharsetSpecify().getValue()) {
      charset = getDisplay().getCharsetSpecifyText().getText();
    }
    return charset;
  }

  private void validateCharacterSet(String charset) {
    if(charset == null || charset.equals("")) {
      errors.add(translations.charsetMustNotBeNullMessage());

    } else if(!charsetExistsInAvailableCharsets(charset)) {
      errors.add(translations.charsetDoesNotExistMessage());
    }
  }

  private boolean charsetExistsInAvailableCharsets(String charset) {
    for(String availableCharset : availableCharsets) {
      if(charset.equals(availableCharset)) return true;
    }
    return false;
  }

  private int getValidatedRow() {
    int row = getRow();
    if(row < 1) errors.add(translations.rowMustBePositiveMessage());
    return row;
  }

  private int getRow() {
    int row = 1;
    try {
      row = Integer.parseInt(getDisplay().getRowText().getText());
    } catch(NumberFormatException e) {
      errors.add(translations.rowMustBeIntegerMessage());
    }
    return row;
  }

  public void getDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString> newBuilder().forResource("/files/charsets/default").get().withCallback(new ResourceCallback<JsArrayString>() {

      @Override
      public void onResource(Response response, JsArrayString resource) {
        String charset = resource.get(0);
        getDisplay().setDefaultCharset(charset);
        CsvFormatStepPresenter.this.defaultCharset = charset;
      }
    }).send();

  }

  public void getAvailableCharsets() {
    ResourceRequestBuilderFactory.<JsArrayString> newBuilder().forResource("/files/charsets/available").get().withCallback(new ResourceCallback<JsArrayString>() {
      @Override
      public void onResource(Response response, JsArrayString datasources) {
        for(int i = 0; i < datasources.length(); i++) {
          availableCharsets.add(datasources.get(i));
        }
      }
    }).send();
  }

  private void enableImport() {
    getDisplay().setNextEnabled(csvFileSelectionPresenter.getSelectedFile().length() > 0);
  }

  class FileSelectionUpdateHandler implements FileSelectionUpdateEvent.Handler {
    @Override
    public void onFileSelectionUpdate(FileSelectionUpdateEvent event) {
      enableImport();
    }
  }

}
