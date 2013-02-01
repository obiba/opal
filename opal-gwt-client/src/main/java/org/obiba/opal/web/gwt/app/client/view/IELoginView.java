package org.obiba.opal.web.gwt.app.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public class IELoginView extends LoginView {

  private static final IELoginViewUiBinder uiBinder = GWT.create(IELoginViewUiBinder.class);

  private final Widget panel;

  public IELoginView() {
    panel = uiBinder.createAndBindUi(this);
  }

  @Override
  public HasClickHandlers getSignIn() {
    return new HasClickHandlers() {

      @Override
      public void fireEvent(GwtEvent<?> event) {

      }

      @Override
      public HandlerRegistration addClickHandler(ClickHandler handler) {

        return null;
      }
    };
  }

  @Override
  public Widget asWidget() {
    return panel;
  }

  @Override
  public HasValue<String> getPassword() {
    return new DummyHasValue();
  }

  @Override
  public HasValue<String> getUserName() {
    return new DummyHasValue();
  }

  @Override
  public void focusOnUserName() {

  }

  @Override
  public void showErrorMessageAndClearPassword() {

  }

  @Override
  public void clear() {
  }

  @Override
  public HasKeyUpHandlers getPasswordTextBox() {
    return new DummyHasKeyUpHandlers();
  }

  @Override
  public HasKeyUpHandlers getUserNameTextBox() {
    return new DummyHasKeyUpHandlers();
  }

  private static final class DummyHasValue implements HasValue<String> {
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

      return null;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {

    }

    @Override
    public String getValue() {

      return null;
    }

    @Override
    public void setValue(String value) {

    }

    @Override
    public void setValue(String value, boolean fireEvents) {

    }
  }

  private static final class DummyHasKeyUpHandlers implements HasKeyUpHandlers {
    @Override
    public void fireEvent(GwtEvent<?> event) {

    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {

      return null;
    }
  }

  @UiTemplate("IELoginView.ui.xml")
  interface IELoginViewUiBinder extends UiBinder<Widget, IELoginView> {}

}
