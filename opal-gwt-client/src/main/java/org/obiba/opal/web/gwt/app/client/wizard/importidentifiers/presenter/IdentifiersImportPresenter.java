/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.Wizard;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class IdentifiersImportPresenter extends WidgetPresenter<IdentifiersImportPresenter.Display> implements Wizard {

  @Inject
  public IdentifiersImportPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCloseClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousClickHandler(ClickHandler handler);

    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    void setCsvOptionsFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    HasText getSelectedFile();

    void setStepValidator(ValidationHandler handler);

    boolean isIdentifiersOnly();

    boolean isIdentifiersPlusData();

    void setUnits(JsArray<FunctionalUnitDto> units);

    String getSelectedUnit();

    ImportFormat getImportFormat();

    boolean isCsvFormatOptionsStep();

    /** Display no format options in the Format Options Step. The format chosen has no options. */
    void setNoFormatOptions();

    /** Display the CSV format options in the Format Options Step. */
    void setCsvFormatOptions();

  }

  @Inject
  private FileSelectionPresenter fileSelectionPresenter;

  @Inject
  private FileSelectionPresenter csvOptionsFileSelectionPresenter;

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
  }

  @Override
  public void revealDisplay() {
    initUnits();
    getDisplay().showDialog();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  protected void onBind() {
    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    fileSelectionPresenter.bind();
    getDisplay().setFileSelectorWidgetDisplay(fileSelectionPresenter.getDisplay());

    csvOptionsFileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    csvOptionsFileSelectionPresenter.bind();
    getDisplay().setCsvOptionsFileSelectorWidgetDisplay(csvOptionsFileSelectionPresenter.getDisplay());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addEventHandlers() {
    getDisplay().setStepValidator(new FileValidator());
    super.registerHandler(getDisplay().addCancelClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getDisplay().hideDialog();
      }
    }));
    super.registerHandler(getDisplay().addCloseClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getDisplay().hideDialog();
      }
    }));
    super.registerHandler(getDisplay().addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        update();
      }
    }));
  }

  private void update() {
    if(fileSelectionPresenter.getSelectedFile() != null && !fileSelectionPresenter.getSelectedFile().equals("")) {
      csvOptionsFileSelectionPresenter.setSelectedFile(fileSelectionPresenter.getSelectedFile());
    }
    if(getDisplay().getImportFormat().equals(ImportFormat.CSV)) {
      getDisplay().setCsvFormatOptions();
    } else {
      getDisplay().setNoFormatOptions();
    }
  }

  class FileValidator extends AbstractValidationHandler {

    public FileValidator() {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getDisplay().getSelectedFile(), "NoFileSelected"));
      return validators;
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
