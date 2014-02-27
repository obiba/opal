package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.gwt.app.client.support.UnhandledResponseEventMessageBuilder;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.github.gwtbootstrap.client.ui.event.HiddenHandler;
import com.github.gwtbootstrap.client.ui.event.ShownEvent;
import com.github.gwtbootstrap.client.ui.event.ShownHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;

public abstract class ModalPopupViewWithUiHandlers<C extends ModalUiHandlers> extends ModalViewImpl
    implements HasUiHandlers<C> {

  private C uiHandlers;

  private HandlerRegistration handlerRegistration;

  protected ModalPopupViewWithUiHandlers(EventBus eventBus) {
    super(eventBus);
    registerUserMessageEventHandler();
  }

  protected C getUiHandlers() {
    return uiHandlers;
  }

  @Override
  public void setUiHandlers(C uiHandlers) {
    this.uiHandlers = uiHandlers;
    asModal().addShownHandler(new ModalShownHandler());
    asModal().addHiddenHandler(new ModalHiddenHandler());
  }

  private void registerUserMessageEventHandler() {
    handlerRegistration = getEventBus()
        .addHandler(UnhandledResponseEvent.getType(), new UnhandledResponseEvent.Handler() {
          @Override
          public void onUnhandledResponse(UnhandledResponseEvent event) {
            if(event.isConsumed() || !asModal().isVisible()) return;
            event.setConsumed(true);
            asModal().addAlert(UnhandledResponseEventMessageBuilder.get(event).build(), AlertType.ERROR);
          }
        });
  }

  private class ModalHiddenHandler implements HiddenHandler {

    @Override
    public void onHidden(HiddenEvent hiddenEvent) {
      if(handlerRegistration != null) handlerRegistration.removeHandler();
      getUiHandlers().onModalHidden();
    }
  }

  private class ModalShownHandler implements ShownHandler {

    @Override
    public void onShown(ShownEvent shownEvent) {
      getUiHandlers().onModalShown();
    }
  }

}


