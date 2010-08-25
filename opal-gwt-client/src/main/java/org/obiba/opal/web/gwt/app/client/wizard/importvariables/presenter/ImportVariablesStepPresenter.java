/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.ResourceRequestView;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

public class ImportVariablesStepPresenter extends WidgetPresenter<ImportVariablesStepPresenter.Display> {
  //
  // Instance Variables
  //

  //
  // Constructors
  //

  @Inject
  public ImportVariablesStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addReturnClickHandler(new ReturnClickHandler()));
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
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

  public void clearResourceRequests() {
    getDisplay().clearResourceRequests();
  }

  public <T extends JavaScriptObject> void addResourceRequest(String resourceName, ResourceRequestBuilder<T> requestBuilder) {
    ResourceRequestPresenter<T> resourceRequestPresenter = new ResourceRequestPresenter<T>(new ResourceRequestView(), eventBus, requestBuilder);
    resourceRequestPresenter.getDisplay().setResourceName(resourceName);
    resourceRequestPresenter.setSuccessCodes(200, 201);
    resourceRequestPresenter.setErrorCodes(400, 500);

    getDisplay().addResourceRequest(resourceRequestPresenter.getDisplay());

    resourceRequestPresenter.sendRequest();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void clearResourceRequests();

    void addResourceRequest(ResourceRequestPresenter.Display resourceRequestDisplay);

    HandlerRegistration addReturnClickHandler(ClickHandler handler);
  }

  static class ReturnClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      // TODO Auto-generated method stub
    }
  }
}
