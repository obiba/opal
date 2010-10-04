/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;

import com.google.inject.Inject;

public class ConfigureViewStepPresenter extends WidgetPresenter<ConfigureViewStepPresenter.Display> {
  //
  // Instance Variables
  //

  //
  // Constructors
  //

  @Inject
  public ConfigureViewStepPresenter(final Display display, final EventBus eventBus) {
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

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(ViewConfigurationRequiredEvent.getType(), new ViewConfigurationRequiredHandler()));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {
  }

  class ViewConfigurationRequiredHandler implements ViewConfigurationRequiredEvent.Handler {

    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      eventBus.fireEvent(new WorkbenchChangeEvent(ConfigureViewStepPresenter.this));
    }
  }
}