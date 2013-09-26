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
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateListPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplateListUiHandlers;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.github.gwtbootstrap.client.ui.NavHeader;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavList;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ReportTemplateListView extends ViewWithUiHandlers<ReportTemplateListUiHandlers>
    implements ReportTemplateListPresenter.Display {

  interface Binder extends UiBinder<Widget, ReportTemplateListView> {}

  private final Translations translations;

  @UiField
  NavList reportList;

  @Inject
  public ReportTemplateListView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public void setReportTemplates(JsArray<ReportTemplateDto> templates) {
    reportList.clear();

    // group templates by project
    Multimap<String, ReportTemplateDto> templateMap = ArrayListMultimap.create();
    for(final ReportTemplateDto template : JsArrays.toIterable(JsArrays.toSafeArray(templates))) {
      templateMap.get(template.getProject()).add(template);
    }

    for(String project : templateMap.keySet()) {
      reportList.add(new NavHeader(TranslationsUtils.replaceArguments(translations.reportTemplatesHeader(), project)));

      for(final ReportTemplateDto template : templateMap.get(project)) {
        NavLink link = new NavLink(template.getName());
        link.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            getUiHandlers().onSelection(template);
          }
        });
        reportList.add(link);
      }
    }
  }

}
