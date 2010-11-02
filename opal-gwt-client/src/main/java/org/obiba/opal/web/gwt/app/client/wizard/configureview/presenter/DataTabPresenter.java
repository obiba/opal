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

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

public class DataTabPresenter extends WidgetPresenter<DataTabPresenter.Display> implements HasChanged {

  public interface Display extends WidgetDisplay {
    HandlerRegistration addSaveChangesClickHandler(ClickHandler clickHandler);

    void saveChangesEnabled(boolean enabled);

    void setTableSelector(TableListPresenter.Display tableSelector);

    void clear();
  }

  @Inject
  private TableListPresenter tableListPresenter;

  @Inject
  public DataTabPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  protected void onBind() {
    getDisplay().saveChangesEnabled(true);

    tableListPresenter.bind();
    getDisplay().setTableSelector(tableListPresenter.getDisplay());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    tableListPresenter.unbind();
  }

  @Override
  public void revealDisplay() {
    tableListPresenter.getTables().clear();
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

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addSaveChangesClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {

      }
    }));
    super.registerHandler(getDisplay().addSaveChangesClickHandler(new SaveChangesClickHandler()));
  }

  class ViewConfigurationRequiredHandler implements ViewConfigurationRequiredEvent.Handler {

    public void onViewConfigurationRequired(ViewConfigurationRequiredEvent event) {
      eventBus.fireEvent(new WorkbenchChangeEvent(DataTabPresenter.this, false, false));
    }
  }

  class SaveChangesClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
    }
  }

  @Override
  public boolean isChanged() {
    return true;
  }

}