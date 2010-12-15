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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
  //
  // Constructors
  //

  @Inject
  public ReportTemplateListPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    refreshReportTemplates(null);
    addHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
    refreshReportTemplates(getDisplay().getSelectedReportTemplate());
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

  private void refreshReportTemplates(ReportTemplateDto templateToSelect) {
    ResourceRequestBuilderFactory.<JsArray<ReportTemplateDto>> newBuilder().forResource("/report-templates").get().withCallback(new ReportTemplatesResourceCallback(templateToSelect)).send();
  }

  private void addHandlers() {
    super.registerHandler(eventBus.addHandler(ReportTemplateCreatedEvent.getType(), new ReportTemplateCreatedHandler()));
    super.registerHandler(eventBus.addHandler(ReportTemplateDeletedEvent.getType(), new ReportTemplateDeletedHandler()));
    super.registerHandler(getDisplay().addSelectReportTemplateHandler(new ReportTemplateSelectionChangeHandler()));
  }

  private JsArray<ReportTemplateDto> sortReportTemplates(JsArray<ReportTemplateDto> templates) {
    List<ComparableReportTemplateDto> templateList = new ArrayList<ComparableReportTemplateDto>();
    for(int i = 0; i < templates.length(); i++) {
      templateList.add(new ComparableReportTemplateDto(templates.get(i)));
    }
    Collections.sort(templateList);

    @SuppressWarnings("unchecked")
    JsArray<ReportTemplateDto> sortedTemplates = (JsArray<ReportTemplateDto>) JsArray.createArray();
    for(ComparableReportTemplateDto c : templateList) {
      sortedTemplates.push(c.getWrappedTemplate());
    }

    return sortedTemplates;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setReportTemplates(JsArray<ReportTemplateDto> templates);

    void select(ReportTemplateDto reportTemplateDto);

    ReportTemplateDto getSelectedReportTemplate();

    HandlerRegistration addSelectReportTemplateHandler(SelectionChangeHandler handler);
  }

  class ComparableReportTemplateDto implements Comparable<ComparableReportTemplateDto> {
    //
    // Instance Variables
    //

    private ReportTemplateDto template;

    //
    // Constructors
    //

    ComparableReportTemplateDto(ReportTemplateDto template) {
      this.template = template;
    }

    //
    // Comparable Methods
    //

    @Override
    public int compareTo(ComparableReportTemplateDto o) {
      return template.getName().compareTo(o.getWrappedTemplate().getName());
    }

    //
    // Methods
    //

    ReportTemplateDto getWrappedTemplate() {
      return template;
    }
  }

  class ReportTemplateCreatedHandler implements ReportTemplateCreatedEvent.Handler {

    @Override
    public void onReportTemplateCreated(final ReportTemplateCreatedEvent event) {
      refreshReportTemplates(event.getReportTemplate());
    }
  }

  class ReportTemplateDeletedHandler implements ReportTemplateDeletedEvent.Handler {

    @Override
    public void onReportTemplateDeleted(ReportTemplateDeletedEvent event) {
      refreshReportTemplates(null);
    }
  }

  class ReportTemplateSelectionChangeHandler implements SelectionChangeHandler {

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
      eventBus.fireEvent(new ReportTemplateSelectedEvent(getDisplay().getSelectedReportTemplate()));
    }
  }

  class ReportTemplatesResourceCallback implements ResourceCallback<JsArray<ReportTemplateDto>> {

    private ReportTemplateDto templateToSelect;

    ReportTemplatesResourceCallback(ReportTemplateDto templateToSelect) {
      this.templateToSelect = templateToSelect;
    }

    @Override
    public void onResource(Response response, JsArray<ReportTemplateDto> templates) {
      JsArray<ReportTemplateDto> nonNullTemplates = JsArrays.toSafeArray(templates);

      getDisplay().setReportTemplates(sortReportTemplates(nonNullTemplates));

      if(templateToSelect != null) {
        getDisplay().select(templateToSelect);
      }

      eventBus.fireEvent(new ReportTemplateListReceivedEvent(nonNullTemplates));
    }
  }
}