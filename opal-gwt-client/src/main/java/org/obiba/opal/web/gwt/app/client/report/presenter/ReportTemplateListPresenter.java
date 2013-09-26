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
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ReportTemplateListPresenter extends PresenterWidget<ReportTemplateListPresenter.Display>
    implements ReportTemplateListUiHandlers {

  private String project;

  @Inject
  public ReportTemplateListPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    addHandlers();
  }

  @Override
  protected void onReveal() {
    refreshReportTemplates(null);
  }

  @Override
  public void onSelection(ReportTemplateDto template) {
    fireEvent(new ReportTemplateSelectedEvent(template));
  }

  private void refreshReportTemplates(ReportTemplateDto templateToSelect) {
    String uri;
    if(project == null) {
      uri = UriBuilder.URI_REPORT_TEMPLATES.build();
    } else {
      uri = UriBuilder.URI_PROJECT_REPORT_TEMPLATES.build(project);
    }
    ResourceRequestBuilderFactory.<JsArray<ReportTemplateDto>>newBuilder().forResource(uri).get()
        .withCallback(new ReportTemplatesResourceCallback(templateToSelect)).send();
  }

  private void addHandlers() {
    addRegisteredHandler(ReportTemplateCreatedEvent.getType(), new ReportTemplateCreatedHandler());
    addRegisteredHandler(ReportTemplateDeletedEvent.getType(), new ReportTemplateDeletedHandler());
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
      if(project == null || project.equals(template.getProject())) sortedTemplates.push(template);
    }

    return sortedTemplates;
  }

  public void showProject(String project) {
    this.project = project;
  }

  public interface Display extends View, HasUiHandlers<ReportTemplateListUiHandlers> {

    void setReportTemplates(JsArray<ReportTemplateDto> templates);

  }

  class ReportTemplateCreatedHandler implements ReportTemplateCreatedEvent.Handler {

    @Override
    public void onReportTemplateCreated(ReportTemplateCreatedEvent event) {
      refreshReportTemplates(event.getReportTemplate());
    }
  }

  class ReportTemplateDeletedHandler implements ReportTemplateDeletedEvent.Handler {

    @Override
    public void onReportTemplateDeleted(ReportTemplateDeletedEvent event) {
      refreshReportTemplates(null);
    }
  }

  class ReportTemplatesResourceCallback implements ResourceCallback<JsArray<ReportTemplateDto>> {

    private final ReportTemplateDto templateToSelect;

    ReportTemplatesResourceCallback(ReportTemplateDto templateToSelect) {
      this.templateToSelect = templateToSelect;
    }

    @Override
    public void onResource(Response response, JsArray<ReportTemplateDto> templates) {
      JsArray<ReportTemplateDto> sortedTemplates = sortReportTemplates(JsArrays.toSafeArray(templates));

      getView().setReportTemplates(sortedTemplates);

      if(sortedTemplates.length() == 0) {
        onSelection(null);
      } else if(templateToSelect != null) {
        onSelection(templateToSelect);
      } else if(sortedTemplates.length() > 0) {
        onSelection(sortedTemplates.get(0));
      }
    }
  }
}