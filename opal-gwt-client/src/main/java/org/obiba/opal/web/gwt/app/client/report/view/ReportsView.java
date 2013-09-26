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

import org.obiba.opal.web.gwt.app.client.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportsPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportsUiHandlers;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ReportsView extends ViewWithUiHandlers<ReportsUiHandlers> implements ReportsPresenter.Display {

  @UiField
  Button add;

  @UiField
  Button edit;

  @UiField
  Button remove;

  @UiField
  Button download;

  @UiField
  Button execute;

  @UiField
  ScrollPanel reportTemplateDetailsPanel;

  @UiField
  ScrollPanel reportTemplateListPanel;

  interface Binder extends UiBinder<Widget, ReportsView> {}

  @Inject
  public ReportsView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    HasWidgets panel = null;
    SplitPaneWorkbenchPresenter.Slot splitSlot = (SplitPaneWorkbenchPresenter.Slot) slot;
    switch(splitSlot) {
      case LEFT:
        panel = reportTemplateListPanel;
        break;
      case CENTER:
        panel = reportTemplateDetailsPanel;
        break;
    }
    if(panel != null) {
      panel.clear();
      if(content != null) {
        panel.add(content.asWidget());
      }
    }
  }

  @UiHandler("add")
  public void onAdd(ClickEvent event) {
    getUiHandlers().onAdd();
  }

  @UiHandler("edit")
  public void onEdit(ClickEvent event) {
    getUiHandlers().onEdit();
  }

  @UiHandler("remove")
  public void onDelete(ClickEvent event) {
    getUiHandlers().onDelete();
  }

  @UiHandler("download")
  public void onDownload(ClickEvent event) {
    getUiHandlers().onDownload();
  }

  @UiHandler("execute")
  public void onExecute(ClickEvent event) {
    getUiHandlers().onExecute();
  }

  @Override
  public HasAuthorization getAddReportTemplateAuthorizer() {
    return new WidgetAuthorizer(add);
  }

  @Override
  public HasAuthorization getExecuteReportAuthorizer() {
    return new WidgetAuthorizer(execute);
  }

  @Override
  public HasAuthorization getDownloadReportDesignAuthorizer() {
    return new WidgetAuthorizer(download);
  }

  @Override
  public HasAuthorization getRemoveReportTemplateAuthorizer() {
    return new WidgetAuthorizer(remove);
  }

  @Override
  public HasAuthorization getUpdateReportTemplateAuthorizer() {
    return new WidgetAuthorizer(edit);
  }
}
