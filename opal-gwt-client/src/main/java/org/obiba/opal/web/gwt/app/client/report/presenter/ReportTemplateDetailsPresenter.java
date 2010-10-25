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

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class ReportTemplateDetailsPresenter extends WidgetPresenter<ReportTemplateDetailsPresenter.Display> {

  public interface Display extends WidgetDisplay {
    void setProducedReports(JsArray<FileDto> reports);
  }

  @Inject
  public ReportTemplateDetailsPresenter(final Display display, final EventBus eventBus) {
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
    ResourceRequestBuilderFactory.<JsArray<FileDto>> newBuilder().forResource("/files/meta/reports").get().withCallback(new ProducedReportsResourceCallback()).send();
  }

  private void addHandlers() {

  }

  private class ProducedReportsResourceCallback implements ResourceCallback<JsArray<FileDto>> {

    @Override
    public void onResource(Response response, JsArray<FileDto> reports) {
      GWT.log("reports size " + reports.length());
      getDisplay().setProducedReports(reports);
    }

  }

}
