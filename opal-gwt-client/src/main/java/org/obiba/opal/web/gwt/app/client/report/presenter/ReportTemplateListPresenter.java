/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateCreatedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateDeletedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateListReceivedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionModel.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel.SelectionChangeHandler;
import com.google.inject.Inject;

public class ReportTemplateListPresenter extends WidgetPresenter<ReportTemplateListPresenter.Display> {

  public interface Display extends WidgetDisplay {
    void setReportTemplates(JsArray<ReportTemplateDto> templates);

    void select(ReportTemplateDto reportTemplateDto);

    ReportTemplateDto getSelectedReportTemplate();

    HandlerRegistration addSelectReportTemplateHandler(SelectionChangeHandler handler);

  }

  @Inject
  public ReportTemplateListPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
    initUiComponents();
    addHandlers();
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

  private void initUiComponents() {
    ResourceRequestBuilderFactory.<JsArray<ReportTemplateDto>> newBuilder().forResource("/report-templates").get().withCallback(new ReportTemplatesResourceCallback()).send();
  }

  private void addHandlers() {
    super.registerHandler(getDisplay().addSelectReportTemplateHandler(new ReportTemplateSelectionChangeHandler()));
    super.registerHandler(eventBus.addHandler(ReportTemplateDeletedEvent.getType(), new ReportTemplateDeletedHandler()));
    super.registerHandler(eventBus.addHandler(ReportTemplateCreatedEvent.getType(), new ReportTemplateCreatedHandler()));
    super.registerHandler(eventBus.addHandler(ReportTemplateListReceivedEvent.getType(), new ReportTemplateListReceivedEventHandler()));
  }

  private class ReportTemplateCreatedHandler implements ReportTemplateCreatedEvent.Handler {

    @Override
    public void onReportTemplateCreated(final ReportTemplateCreatedEvent event) {
      ResourceRequestBuilderFactory.<JsArray<ReportTemplateDto>> newBuilder().forResource("/report-templates").get().withCallback(new ResourceCallback<JsArray<ReportTemplateDto>>() {

        @Override
        public void onResource(Response response, JsArray<ReportTemplateDto> templates) {
          getDisplay().setReportTemplates(JsArrays.toSafeArray(templates));
          getDisplay().select(event.getReportTemplate());
        }
      }).send();
    }
  }

  private class ReportTemplateDeletedHandler implements ReportTemplateDeletedEvent.Handler {

    @Override
    public void onReportTemplateDeleted(ReportTemplateDeletedEvent event) {
      initUiComponents();
    }

  }

  private class ReportTemplateSelectionChangeHandler implements SelectionChangeHandler {

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
      eventBus.fireEvent(new ReportTemplateSelectedEvent(getDisplay().getSelectedReportTemplate()));
    }

  }

  private class ReportTemplatesResourceCallback implements ResourceCallback<JsArray<ReportTemplateDto>> {

    @Override
    public void onResource(Response response, JsArray<ReportTemplateDto> templates) {
      eventBus.fireEvent(new ReportTemplateListReceivedEvent(JsArrays.toSafeArray(templates)));
    }
  }

  class ReportTemplateListReceivedEventHandler implements ReportTemplateListReceivedEvent.Handler {

    @Override
    public void onReportTemplateListReceived(ReportTemplateListReceivedEvent event) {
      getDisplay().setReportTemplates(event.getReportTemplates());
    }
  }
}
