package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.github.gwtbootstrap.client.ui.event.HiddenHandler;
import com.google.web.bindery.event.shared.EventBus;

public abstract class ModalPopupViewWithUiHandlers<C extends ModalUiHandlers> extends ModalViewImpl
    implements com.gwtplatform.mvp.client.HasUiHandlers<C> {
  private C uiHandlers;

  protected ModalPopupViewWithUiHandlers(EventBus eventBus) {
    super(eventBus);
  }

  protected C getUiHandlers() {
    return uiHandlers;
  }

  @Override
  public void setUiHandlers(C uiHandlers) {
    this.uiHandlers = uiHandlers;
    asModal().addHiddenHandler(new ModalHiddenHandler());
  }

  private class ModalHiddenHandler implements HiddenHandler {

    @Override
    public void onHidden(HiddenEvent hiddenEvent) {
      getUiHandlers().onModalHidden();
    }
  }

}


