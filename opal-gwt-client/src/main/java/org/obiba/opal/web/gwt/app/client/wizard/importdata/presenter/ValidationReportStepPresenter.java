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

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DestinationSelectionStepPresenter.CsvValidationError;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ValidationReportStepPresenter extends WidgetPresenter<ValidationReportStepPresenter.Display> {
  //
  // Instance Variables
  //

  // Provider used here to break a circular dependency.
  @Inject
  private Provider<DataImportPresenter> formatSelectionStepPresenter;

  //
  // Constructors
  //

  @Inject
  public ValidationReportStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
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
  // Methods
  //

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    void setParsingErrors(List<DatasourceParsingErrorDto> errors);

    void setValidationErrors(List<CsvValidationError> errors);
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new WorkbenchChangeEvent(formatSelectionStepPresenter.get()));
    }
  }
}
