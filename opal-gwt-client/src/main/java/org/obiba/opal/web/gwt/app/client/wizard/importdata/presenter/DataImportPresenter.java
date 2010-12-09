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

import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.Wizard;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DataImportPresenter extends WidgetPresenter<DataImportPresenter.Display> implements Wizard {

  @Inject
  private CsvFormatStepPresenter csvFormatStepPresenter;

  @Inject
  private XmlFormatStepPresenter xmlFormatStepPresenter;

  @Inject
  private DestinationSelectionStepPresenter destinationSelectionStepPresenter;

  @Inject
  private IdentityArchiveStepPresenter identityArchiveStepPresenter;

  @Inject
  private ConclusionStepPresenter conclusionStepPresenter;

  private DataImportFormatStepPresenter formatStepPresenter;

  private String datasourceName;

  @Inject
  public DataImportPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    if(datasourceName != null) {
      destinationSelectionStepPresenter.setDestinationDatasource(datasourceName);
    }

    csvFormatStepPresenter.bind();
    xmlFormatStepPresenter.bind();
    destinationSelectionStepPresenter.bind();
    identityArchiveStepPresenter.bind();
    conclusionStepPresenter.bind();

    getDisplay().setDestinationSelectionDisplay(destinationSelectionStepPresenter.getDisplay());
    getDisplay().setIdentityArchiveStepDisplay(identityArchiveStepPresenter.getDisplay());

    addEventHandlers();
  }

  protected void addEventHandlers() {
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
    super.registerHandler(getDisplay().addImportClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        ImportData importData = formatStepPresenter.getImportData();
        identityArchiveStepPresenter.updateImportData(importData);
        destinationSelectionStepPresenter.updateImportData(importData);
        conclusionStepPresenter.launchImport(importData);
        getDisplay().renderConclusion(conclusionStepPresenter);
      }
    }));

    super.registerHandler(getDisplay().addFormatChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent evt) {
        updateFormatStepDisplay();
      }
    }));
    getDisplay().setFormatStepValidator(new ValidationHandler() {

      @Override
      public boolean validate() {
        return formatStepPresenter.validate();
      }
    });

  }

  private void updateFormatStepDisplay() {
    destinationSelectionStepPresenter.setImportFormat(getDisplay().getImportFormat());
    if(getDisplay().getImportFormat().equals(ImportFormat.CSV)) {
      csvFormatStepPresenter.clear();
      this.formatStepPresenter = csvFormatStepPresenter;
      getDisplay().setFormatStepDisplay(csvFormatStepPresenter.getDisplay());
    } else if(getDisplay().getImportFormat().equals(ImportFormat.XML)) {
      this.formatStepPresenter = xmlFormatStepPresenter;
      getDisplay().setFormatStepDisplay(xmlFormatStepPresenter.getDisplay());
    } else {
      this.formatStepPresenter = null;
      throw new IllegalStateException("Unknown format: " + getDisplay().getImportFormat());
    }
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    csvFormatStepPresenter.unbind();
    xmlFormatStepPresenter.unbind();
    destinationSelectionStepPresenter.unbind();
    datasourceName = null;
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    destinationSelectionStepPresenter.refreshDisplay(); // to refresh the datasources
    updateFormatStepDisplay();
    getDisplay().showDialog();
  }

  //
  // Wizard Methods
  //

  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 0) {
      if(event.getEventParameters()[0] instanceof String) {
        datasourceName = (String) event.getEventParameters()[0];
      } else {
        throw new IllegalArgumentException("unexpected event parameter type (expected String)");
      }
    }
  }

  //
  // Inner classes
  //

  //
  // Interfaces
  //

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    ImportFormat getImportFormat();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCloseClickHandler(ClickHandler clickHandler);

    //
    // Format selection step
    //

    HandlerRegistration addFormatChangeHandler(ChangeHandler handler);

    //
    // Format step
    //

    void setFormatStepDisplay(DataImportStepDisplay display);

    void setFormatStepValidator(ValidationHandler handler);

    //
    // Destination selection step
    //

    void setDestinationSelectionDisplay(DataImportStepDisplay display);

    //
    // Identity / Archive step
    //

    void setIdentityArchiveStepDisplay(DataImportStepDisplay display);

    //
    // Conclusion step
    //

    public void renderConclusion(ConclusionStepPresenter presenter);

    HandlerRegistration addImportClickHandler(ClickHandler handler);

  }

  public interface DataImportFormatStepPresenter {

    /**
     * Get the import data as collected.
     * @return
     */
    public ImportData getImportData();

    /**
     * Validate the import data were correctly provided, and send notification error messages if any.
     * @return
     */
    public boolean validate();

  }

  public interface DataImportStepDisplay {

    public Widget asWidget();

    public Widget getStepHelp();

  }

}
