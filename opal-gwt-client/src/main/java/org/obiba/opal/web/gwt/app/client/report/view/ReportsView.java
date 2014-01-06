/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportsPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportsUiHandlers;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.NavHeader;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavList;
import com.github.gwtbootstrap.client.ui.NavWidget;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ReportsView extends ViewWithUiHandlers<ReportsUiHandlers> implements ReportsPresenter.Display {

  private final Translations translations;

  @UiField
  Button add;

  @UiField
  ScrollPanel reportTemplateDetailsPanel;

  @UiField
  NavList reportList;

  private NavLink currentLink;

  interface Binder extends UiBinder<Widget, ReportsView> {}

  @Inject
  public ReportsView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    reportTemplateDetailsPanel.clear();
    reportTemplateDetailsPanel.add(content);
  }

  @Override
  public void setReportTemplates(JsArray<ReportTemplateDto> templates) {
    reportList.clear();

    // group templates by project
    Multimap<String, ReportTemplateDto> templateMap = ArrayListMultimap.create();
    for(ReportTemplateDto template : JsArrays.toIterable(JsArrays.toSafeArray(templates))) {
      templateMap.get(template.getProject()).add(template);
    }

    for(String project : templateMap.keySet()) {
      reportList.add(new NavHeader(TranslationsUtils.replaceArguments(translations.reportTemplatesHeader(), project)));
      addReportTemplateLinks(templateMap.get(project));
    }
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("add")
  public void onAdd(ClickEvent event) {
    getUiHandlers().onAdd();
  }

  @Override
  public HasAuthorization getAddReportTemplateAuthorizer() {
    return new WidgetAuthorizer(add);
  }

  @Override
  public void setCurrentReportTemplate(ReportTemplateDto reportTemplateDto) {
    String reportName = reportTemplateDto.getName();
    for(Widget w : reportList) {
      if(w instanceof NavLink) {
        NavLink link = (NavLink) w;
        if(link.getText().trim().equals(reportName)) {
          if(currentLink != null) currentLink.setActive(false);
          link.setActive(true);
          currentLink = link;
          break;
        }
      }
    }
  }

  //
  // Private methods
  //

  private void addReportTemplateLinks(Iterable<ReportTemplateDto> templates) {
    for(ReportTemplateDto template : templates) {
      NavLink link = new NavLink(template.getName());
      link.addClickHandler(new ReportTemplateClickHandler(template, link));
      reportList.add(link);
    }
  }

  private class ReportTemplateClickHandler implements ClickHandler {

    private final ReportTemplateDto template;

    private final NavLink link;

    ReportTemplateClickHandler(ReportTemplateDto template, NavLink link) {
      this.template = template;
      this.link = link;
    }

    @Override
    public void onClick(ClickEvent event) {
      unActivateLinks();
      link.setActive(true);
      getUiHandlers().onSelection(template);
    }

    private void unActivateLinks() {
      for(int i = 0; i < reportList.getWidgetCount(); i++) {
        if(reportList.getWidget(i) instanceof NavLink) {
          ((NavWidget) reportList.getWidget(i)).setActive(false);
        }
      }
    }
  }
}
