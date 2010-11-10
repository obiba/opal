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

import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.VariableAddRequiredEvent;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class AddDerivedVariableDialogPresenter extends WidgetPresenter<AddDerivedVariableDialogPresenter.Display> {

  private ViewDto viewDto;

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addAddVariableClickHandler(ClickHandler handler);

    HandlerRegistration addNewVariableClickHandler(ClickHandler handler);

    HandlerRegistration addCopyFromVariableClickHandler(ClickHandler handler);

    void setEnabledCopyFromVariableName(boolean enabled);

    void setEnabledNewVariableName(boolean enabled);

    HasValue<Boolean> getCopyFromVariable();

    HasValue<Boolean> getNewVariable();

    HasText getNewVariableName();

    HasText getCopyFromVariableName();

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
    super.registerHandler(getDisplay().addCopyFromVariableClickHandler(new CopyFromVariableClickHandler()));
    super.registerHandler(getDisplay().addNewVariableClickHandler(new NewVariableClickHandler()));
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

  private String getVariableName() {
    if(getDisplay().getCopyFromVariable().getValue()) {
      return getDisplay().getCopyFromVariableName().getText();
    } else if(getDisplay().getNewVariable().getValue()) {
      return getDisplay().getNewVariableName().getText();
    }
    return null;
  }

  private boolean validate() {
    return true;
  }

  private class CancelClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getDisplay().hideDialog();
    }

  }

  private class AddVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      if(validate()) {
        getDisplay().hideDialog();
        eventBus.fireEvent(new VariableAddRequiredEvent(getVariableName()));
      }
    }
  }

  private class NewVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getDisplay().setEnabledNewVariableName(true);
      getDisplay().setEnabledCopyFromVariableName(false);
      getDisplay().getCopyFromVariableName().setText("");
    }

  }

  private class CopyFromVariableClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getDisplay().setEnabledNewVariableName(false);
      getDisplay().setEnabledCopyFromVariableName(true);
      getDisplay().getNewVariableName().setText("");
    }

  }

}
