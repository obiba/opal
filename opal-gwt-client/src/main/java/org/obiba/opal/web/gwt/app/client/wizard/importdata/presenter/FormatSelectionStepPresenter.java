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

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

public class FormatSelectionStepPresenter extends WidgetPresenter<FormatSelectionStepPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    ImportFormat getImportFormat();
  }

  @Inject
  private CsvFormatStepPresenter csvFormatStepPresenter;

  @Inject
  private XmlFormatStepPresenter xmlFormatStepPresenter;

  @Inject
  private ImportData importData;

  @Inject
  public FormatSelectionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
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
      importData.clear();
      importData.setFormat(display.getImportFormat());
      if(display.getImportFormat().equals(ImportFormat.CSV)) {
        eventBus.fireEvent(new WorkbenchChangeEvent(csvFormatStepPresenter));
      }
      if(display.getImportFormat().equals(ImportFormat.XML)) {
        eventBus.fireEvent(new WorkbenchChangeEvent(xmlFormatStepPresenter));
      }
    }
  }

}
