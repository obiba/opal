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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

/**
 *
 */
public class AttributeDialogPresenter extends WidgetPresenter<AttributeDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasCloseHandlers<DialogBox> getDialog();

    void showDialog();

    void hideDialog();

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    HandlerRegistration addNameDropdownRadioChoiceHandler(ClickHandler handler);

    HandlerRegistration addNameFieldRadioChoiceHandler(ClickHandler handler);

    void setLabelsEnabled(boolean enabled);

    void setAttributeNameEnabled(boolean enabled);

    void selectNameDropdownRadioChoice();

    HasText getAttributeName();

  }

  @Inject
  public AttributeDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {
    // TODO Auto-generated method stub

  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    addRadioButtonNameEventHandlers();
    resetForm();
  }

  private void resetForm() {
    getDisplay().setLabelsEnabled(true);
    getDisplay().setAttributeNameEnabled(false);
    getDisplay().selectNameDropdownRadioChoice();
    getDisplay().getAttributeName().setText("");
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getCancelButton().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    // Hiding the Attribute dialog will generate a CloseEvent.
    super.registerHandler(getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    }));

  }

  private void addRadioButtonNameEventHandlers() {
    super.registerHandler(getDisplay().addNameDropdownRadioChoiceHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getDisplay().setAttributeNameEnabled(false);
        getDisplay().setLabelsEnabled(true);
      }
    }));

    super.registerHandler(getDisplay().addNameFieldRadioChoiceHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getDisplay().setAttributeNameEnabled(true);
        getDisplay().setLabelsEnabled(false);
      }
    }));

  }

  @Override
  protected void onUnbind() {

  }

  @Override
  public Place getPlace() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
    // TODO Auto-generated method stub

  }

}
