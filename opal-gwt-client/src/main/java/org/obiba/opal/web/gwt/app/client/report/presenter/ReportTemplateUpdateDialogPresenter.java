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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateCreatedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateUpdatedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.inject.Inject;

public class ReportTemplateUpdateDialogPresenter extends WidgetPresenter<ReportTemplateUpdateDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    HasClickHandlers getUpdateReportTemplateButton();

    HasClickHandlers getCancelButton();

    HasCloseHandlers<DialogBox> getDialog();

    void setDialogTitle(String title);

    String getName();

    String getDesign();

    String getFormat();

    String getShedule();

  }

  private Translations translations = GWT.create(Translations.class);

  @Inject
  public ReportTemplateUpdateDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    addEventHandlers();
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
    initDisplayComponents();
    getDisplay().showDialog();
  }

  protected void initDisplayComponents() {
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getUpdateReportTemplateButton().addClickHandler(new UpdateReportTemplateClickHandler()));

    super.registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    });

  }

  public class UpdateReportTemplateClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      createOrUpdateReportTemplate();
    }

  }

  private void createOrUpdateReportTemplate() {
    ReportTemplateDto reportTemplate = ReportTemplateDto.create();
    reportTemplate.setName(getDisplay().getName());
    reportTemplate.setCron(getDisplay().getShedule());
    reportTemplate.setFormat(getDisplay().getFormat());
    reportTemplate.setDesign(getDisplay().getDesign());

    CreateOrUpdateReportTemplateCallBack callbackHandler = new CreateOrUpdateReportTemplateCallBack();
    ResourceRequestBuilderFactory.newBuilder().forResource("/report-template/" + getDisplay().getName()).put().withResourceBody(ReportTemplateDto.stringify(reportTemplate)).withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private class CreateOrUpdateReportTemplateCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_OK) {
        eventBus.fireEvent(new ReportTemplateUpdatedEvent(null));
      } else if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new ReportTemplateCreatedEvent(null));
      } else {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
      }

    }
  }

}
