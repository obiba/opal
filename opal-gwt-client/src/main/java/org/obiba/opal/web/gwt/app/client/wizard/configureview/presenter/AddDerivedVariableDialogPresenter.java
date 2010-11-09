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

import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

public class AddDerivedVariableDialogPresenter extends WidgetPresenter<AddDerivedVariableDialogPresenter.Display> {

  private ViewDto viewDto;

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addAddVariableClickHandler(ClickHandler handler);

  }

  @Inject
  public AddDerivedVariableDialogPresenter(final Display display, final EventBus eventBus) {
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
    addHandlers();
  }

  private void addHandlers() {
    super.registerHandler(getDisplay().addAddVariableClickHandler(new AddVariableClickHandler()));
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
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

  private class CancelClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getDisplay().hideDialog();
    }

  }

  private class AddVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {

    }

  }
}
