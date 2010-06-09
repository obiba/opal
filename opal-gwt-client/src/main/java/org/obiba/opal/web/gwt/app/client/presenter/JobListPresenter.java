/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.CommandStateDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

/**
 *
 */
public class JobListPresenter extends WidgetPresenter<JobListPresenter.Display> {

  public interface Display extends WidgetDisplay {

    SelectionModel<CommandStateDto> getTableSelection();

    void renderRows(JsArray<CommandStateDto> rows);

    void clear();

    HasFieldUpdater<CommandStateDto, String> getIdColumn();
  }

  //
  // Constructors
  //

  @Inject
  public JobListPresenter(Display display, EventBus eventBus, final JobDetailsPresenter jobDetailsPresenter) {
    super(display, eventBus);

    getDisplay().getIdColumn().setFieldUpdater(new FieldUpdater<CommandStateDto, String>() {
      public void update(int index, CommandStateDto object, String value) {
        jobDetailsPresenter.getDisplay().showDialog(object);
      }
    });
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
    updateTable();
  }

  @Override
  public void revealDisplay() {
    updateTable();
  }

  //
  // Methods
  //

  private void updateTable() {
    ResourceRequestBuilderFactory.<JsArray<CommandStateDto>> newBuilder().forResource("/shell/commands").get().withCallback(new ResourceCallback<JsArray<CommandStateDto>>() {
      @Override
      public void onResource(Response response, JsArray<CommandStateDto> resource) {
        getDisplay().renderRows(resource);
      }

    }).send();
  }
}
