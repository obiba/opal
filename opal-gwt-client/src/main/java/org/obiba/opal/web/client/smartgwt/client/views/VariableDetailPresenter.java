/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.client.smartgwt.client.views;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.client.smartgwt.client.views.event.VariableChangedEvent;
import org.obiba.opal.web.client.smartgwt.client.views.event.VariableChangedHandler;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.widgets.form.DynamicForm;

/**
 *
 */
public class VariableDetailPresenter extends WidgetPresenter<VariableDetailPresenter.Display> {

  public interface Display extends WidgetDisplay {
    DynamicForm getVariableForm();
  }

  public VariableDetailPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    eventBus.addHandler(VariableChangedEvent.getType(), new VariableChangedHandler() {
      @Override
      public void onVariableChanged(VariableChangedEvent event) {
        DataSource ds = DataSource.getOrCreateRef(event.getVariable().getAttributeAsJavaScriptObject("ds"));
        getDisplay().getVariableForm().setDataSource(ds);
      }
    });
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
  }

}
