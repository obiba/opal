/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.job.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.model.client.opal.CommandStateDto;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

/**
 *
 */
public class JobDetailsPresenter extends WidgetPresenter<JobDetailsPresenter.Display> {

  public interface Display extends WidgetDisplay {
    HasCloseHandlers<PopupPanel> getDialogBox();

    void showDialog(CommandStateDto commandStateDto);

    void hideDialog();
  }

  //
  // Constructors
  //

  @Inject
  public JobDetailsPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
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
  }

  @Override
  public void revealDisplay() {
  }

  //
  // Methods
  //

  protected void addEventHandlers() {
    getDisplay().getDialogBox().addCloseHandler(new CloseHandler<PopupPanel>() {
      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        unbind();
      }
    });
  }
}
