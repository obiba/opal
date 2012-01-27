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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateCreatedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateDeletedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateListReceivedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ReportTemplateListPresenter extends PresenterWidget<ReportTemplateListPresenter.Display> {

  @Inject
  public ReportTemplateListPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    refreshReportTemplates(null);
    addHandlers();
  }

  @Override
  public void onReset() {
    refreshReportTemplates(getView().getSelectedReportTemplate());
  }

  private void refreshReportTemplates(ReportTemplateDto templateToSelect) {
    ResourceRequestBuilderFactory.<JsArray<ReportTemplateDto>> newBuilder().forResource("/report-templates").get().withCallback(new ReportTemplatesResourceCallback(templateToSelect)).send();
  }

  private void addHandlers() {
    super.registerHandler(getEventBus().addHandler(ReportTemplateCreatedEvent.getType(), new ReportTemplateCreatedHandler()));
    super.registerHandler(getEventBus().addHandler(ReportTemplateDeletedEvent.getType(), new ReportTemplateDeletedHandler()));
    super.registerHandler(getView().addSelectReportTemplateHandler(new ReportTemplateSelectionChangeHandler()));
  }

  private JsArray<ReportTemplateDto> sortReportTemplates(JsArray<ReportTemplateDto> templates) {
    List<ReportTemplateDto> templateList = JsArrays.toList(templates);

    Collections.sort(templateList, new Comparator<ReportTemplateDto>() {

      @Override
      public int compare(ReportTemplateDto first, ReportTemplateDto second) {
        return first.getName().compareTo(second.getName());
      }
    });

    @SuppressWarnings("unchecked")
    JsArray<ReportTemplateDto> sortedTemplates = (JsArray<ReportTemplateDto>) JsArray.createArray();
    for(ReportTemplateDto template : templateList) {
      sortedTemplates.push(template);
    }

    return sortedTemplates;
  }

  public interface Display extends View {

    void setReportTemplates(JsArray<ReportTemplateDto> templates);

    void select(ReportTemplateDto reportTemplateDto);

    ReportTemplateDto getSelectedReportTemplate();

    HandlerRegistration addSelectReportTemplateHandler(SelectionChangeEvent.Handler handler);
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

  class ReportTemplateSelectionChangeHandler implements SelectionChangeEvent.Handler {

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
      getEventBus().fireEvent(new ReportTemplateSelectedEvent(getView().getSelectedReportTemplate()));
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

      getView().setReportTemplates(sortReportTemplates(nonNullTemplates));

      if(templateToSelect != null) {
        getView().select(templateToSelect);
      }

      getEventBus().fireEvent(new ReportTemplateListReceivedEvent(nonNullTemplates));
    }
  }
}